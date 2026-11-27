// Teste de carga do RNF01 (critério CA-RNF01.1):
// p95 de GET /bookings/availability abaixo de 2 s com 50 usuários virtuais por 1 minuto.
//
// Uso local (com o Compose no ar e a quadra de perf/seed.sql cadastrada):
//   k6 run perf/availability.js
//   k6 run -e BASE_URL=http://outro-host:8080 perf/availability.js
import http from 'k6/http'
import { check, sleep } from 'k6'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080'
const SLOTS_POR_DIA = 17 // RN01: 06:00 a 23:00, blocos de 1h

export const options = {
  scenarios: {
    grade_de_horarios: {
      executor: 'constant-vus',
      vus: 50,
      duration: '1m',
    },
  },
  thresholds: {
    // Se qualquer limite for ultrapassado, o k6 termina com erro e o job do CI falha.
    'http_req_duration{endpoint:availability}': ['p(95)<2000'],
    'http_req_failed{endpoint:availability}': ['rate<0.01'],
    'checks{endpoint:availability}': ['rate>0.99'],
  },
}

// Gera um CPF válido (o backend valida os dígitos verificadores e exige CPF único).
function gerarCpf() {
  const d = Array.from({ length: 9 }, () => Math.floor(Math.random() * 10))
  for (let tamanho = 9; tamanho <= 10; tamanho++) {
    let soma = 0
    for (let i = 0; i < tamanho; i++) soma += d[i] * (tamanho + 1 - i)
    const digito = (soma * 10) % 11
    d.push(digito === 10 ? 0 : digito)
  }
  return d.join('')
}

// Executa uma vez antes da carga: cria um cliente, faz login e descobre a quadra.
// Um erro aqui interrompe o teste (e o job do CI) antes de gerar carga.
export function setup() {
  const json = { headers: { 'Content-Type': 'application/json' } }
  const email = `k6-${Date.now()}@agendabeach.test`
  const senha = 'senha-k6-123'

  const cadastro = http.post(
    `${BASE_URL}/users`,
    JSON.stringify({ name: 'Cliente k6', email, password: senha, cpf: gerarCpf() }),
    json,
  )
  if (cadastro.status >= 300) throw new Error(`cadastro falhou: HTTP ${cadastro.status} ${cadastro.body}`)

  const login = http.post(`${BASE_URL}/users/login`, JSON.stringify({ email, password: senha }), json)
  if (login.status >= 300) throw new Error(`login falhou: HTTP ${login.status} ${login.body}`)
  const token = login.json('accessToken')

  const quadras = http.get(`${BASE_URL}/courts`, { headers: { Authorization: `Bearer ${token}` } })
  const ativa = (quadras.json() || []).find((q) => q.status === 'ACTIVE')
  if (!ativa) throw new Error('nenhuma quadra ativa: rode perf/seed.sql antes do teste')

  return { token, courtId: ativa.id }
}

// Cada usuário virtual consulta a grade de um dia aleatório dos próximos 14 dias.
export default function (dados) {
  const dia = new Date(Date.now() + Math.floor(Math.random() * 14 + 1) * 86_400_000)
  const data = dia.toISOString().slice(0, 10)

  const resposta = http.get(`${BASE_URL}/bookings/availability?date=${data}&courtId=${dados.courtId}`, {
    headers: { Authorization: `Bearer ${dados.token}` },
    tags: { endpoint: 'availability' },
  })

  check(
    resposta,
    {
      'status 200': (r) => r.status === 200,
      'grade com 17 horários (RN01)': (r) => {
        try {
          const corpo = r.json()
          return Array.isArray(corpo) && corpo.length === SLOTS_POR_DIA
        } catch {
          return false // corpo não é JSON (ex.: página de erro)
        }
      },
    },
    { endpoint: 'availability' },
  )

  sleep(1) // tempo de "pensar" do usuário entre uma consulta e outra
}
