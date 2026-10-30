# 🏖️ AgendaBeach

Plataforma para agendamento de quadras de **Beach Tennis**. Permite que clientes reservem horários com facilidade e que administradores gerenciem quadras, tarifas e ocupação.

> Documento oficial de padronização técnica e fluxo de trabalho do projeto.
> Disciplina: **Manutenção e Melhoria de Software**

## 👥 Equipe

| Papel                    | Integrante                 |
| ------------------------ | -------------------------- |
| Product Owner (PO)       | Rubem Gustavo Krüger       |
| Engenheira de Requisitos | Lucas Camilo Moraes        |
| Quality Assurance (QA)   | Rubem Gustavo Krüger       |
| Desenvolvedor Frontend   | Eduardo de Oliveira        |
| Desenvolvedor Backend    | Silvio José De Ávila Filho |
| DevOps                   | Vitor Keller               |

## 🛠 Stack de Tecnologias

- **Frontend:** React 19 + TypeScript, Vite, Mantine
- **Backend:** Java 21 + Spring Boot 4.1.1 (Spring Web, Spring Data JPA, Spring Security)
- **Banco de Dados:** PostgreSQL 16
- **Containerização:** Docker + Docker Compose
- **Controle de Versão:** GitHub
- **Qualidade de Código:** ESLint, Prettier, SonarCloud
- **CI/CD:** GitHub Actions
- **Gestão de Tarefas:** GitHub Issues + GitHub Projects

## 🚀 Como Rodar o Projeto

### Pré-requisito único: Docker

Com **Docker** e **Docker Compose** instalados, não é necessário instalar Java, Node ou PostgreSQL manualmente na sua máquina.

```bash
git clone https://github.com/avilasj/agendabeach.git
cd AgendaBeach

docker compose up -d --build
```

| Serviço        | Endereço              |
| -------------- | --------------------- |
| Frontend       | http://localhost:5173 |
| Backend (API)  | http://localhost:8080 |
| Banco de dados | localhost:5432        |

```bash
docker compose down       # para tudo, mantém os dados do banco
docker compose down -v    # para tudo e apaga o volume do banco (reset total)
```

> Rodar sem Docker (Java 21 + Node 20 + PostgreSQL nativos) ainda é possível para quem preferir, mas deixou de ser o fluxo recomendado do time — veja a seção [Variáveis de Ambiente](#-variáveis-de-ambiente).

## 🔑 Variáveis de Ambiente

**Via Docker Compose (recomendado):** nada a configurar — as credenciais já estão definidas em `docker-compose.yml` para o ambiente local.

**Rodando o backend nativamente** (`./mvnw spring-boot:run`), o projeto usa a biblioteca `springboot4-dotenv` (já no `pom.xml`) para carregar um arquivo `.env` automaticamente. Crie `backend/.env` a partir do exemplo abaixo (esse arquivo já está no `.gitignore` — nunca commitar credenciais reais):

```
# backend/.env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/agendabeach
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

## 🌿 Estratégia de Branches

Modelo adotado: **GitHub Flow com branch de integração (`dev`)** — um Git Flow simplificado.

| Branch                                    | Finalidade                                                                                           |
| ----------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| `main`                                    | Código estável, sempre pronto para produção/entrega. Só recebe merge de `dev`, em pontos de release. |
| `dev`                                     | Branch de integração contínua. Toda feature/correção chega aqui primeiro, via PR.                    |
| `feat/<n-issue>/<nome-da-funcionalidade>` | Nova funcionalidade. Ex.: `feat/12/cadastro-quadra`                                                  |
| `fix/<n-issue>/<descricao>`               | Correção de bug. Ex.: `fix/27/reserva-duplicada`                                                     |

### Regras

- Nunca commitar direto em `main` ou `dev` — sempre via Pull Request.
- Toda branch nasce a partir da `dev` atualizada.
- Branch é deletada após o merge.
- `main` só avança em pontos de release, com a suíte de CI 100% verde.

> Se o time quiser simplificar ainda mais no futuro, o **GitHub Flow puro** (só `main` + branches de feature, sem `dev`) também é uma opção válida para um projeto deste porte — vale uma conversa de time antes de mudar.

## 📝 Padronização de Commits

Adotamos **Conventional Commits** ([conventionalcommits.org](https://www.conventionalcommits.org)):

```
<tipo>(<escopo opcional>): <descrição>
```

| Tipo       | Uso                                                          |
| ---------- | ------------------------------------------------------------ |
| `feat`     | Nova funcionalidade                                          |
| `fix`      | Correção de bug                                              |
| `refactor` | Alteração de código sem mudança de comportamento             |
| `docs`     | Documentação                                                 |
| `test`     | Criação ou ajuste de testes                                  |
| `style`    | Formatação/espaçamento — sem mudança de lógica               |
| `chore`    | Manutenção (deps, configs) sem impacto em código de produção |
| `ci`       | Mudanças no pipeline de CI/CD                                |
| `build`    | Mudanças no processo de build (Docker, Maven, npm)           |

**Exemplos:**

```
feat(bookings): adiciona endpoint de cadastro de quadras
fix(users): corrige hash de senha no cadastro
refactor(bookings): extrai regra de validação de horário
docs: atualiza README com fluxo de branches
ci: separa testes unitários e de integração no pipeline
```

**Boas práticas:**

- Descrição no imperativo, minúscula, sem ponto final.
- Primeira linha com até 72 caracteres.
- Um commit = uma mudança lógica.
- Mudança que quebra compatibilidade: rodapé `BREAKING CHANGE: <explicação>`.

## 🔁 Fluxo de Trabalho

Sem uma ferramenta paga de gestão, o fluxo do time roda 100% dentro do GitHub:

- **GitHub Issues** — cada tarefa (funcionalidade, bug, débito técnico) vira uma Issue, com labels (`feat`, `bug`, `refactor`, `docs`, `débito-técnico`) e, quando fizer sentido, um Milestone para a entrega/sprint atual.
- **GitHub Projects** (quadro Kanban) — colunas sugeridas: `Backlog` → `A Fazer` → `Em Andamento` → `Em Revisão` → `Concluído`. O GitHub move o card automaticamente quando o PR vinculado é aberto/mesclado.

### Passo a passo

1. Criar (ou pegar) uma **Issue** que atenda ao [DoR](#-definition-of-ready-dor).
2. Atualizar a `dev` local: `git checkout dev && git pull origin dev`.
3. Criar a branch a partir da `dev`: `git checkout -b feat/<n-issue>/<nome-da-funcionalidade>`.
4. Desenvolver seguindo o [Guia de Estilo](#-guia-de-estilo-e-padrões-de-código), commitando em Conventional Commits.
5. Abrir o Pull Request para `dev`, referenciando a Issue no corpo (`Closes #12`) — fecha a Issue automaticamente ao mesclar.
6. **Code Review obrigatório** de ao menos 1 outro dev/QA.
7. Validação de QA conforme o [DoD](#-definition-of-done-dod).
8. Merge na `dev` só com CI verde (compilação, testes unitários, testes de integração, build das imagens e SonarCloud) e sem conflitos.
9. Branch deletada após o merge; card movido para `Concluído` no Project.

## 🎨 Guia de Estilo e Padrões de Código

### Convenções gerais

- Idioma do código: inglês (variáveis, métodos, classes, comentários técnicos).
- Formatação automática: **Prettier** no frontend (`npm run lint` + `prettier --check` já rodam no CI); no backend, indentação de 4 espaços — evitar misturar tabs e espaços no mesmo arquivo.
- Nomenclatura de arquivos: `PascalCase` para componentes/classes (`BookingModal.tsx`, `UserService.java`).

### Clean Code — regras práticas

- **Responsabilidade única (SRP):** um método/classe faz uma coisa só. Se o nome precisa de "e" (`salvarEEnviarEmail`), provavelmente são duas responsabilidades.
- **Nomes que explicam a intenção:** evitar abreviações obscuras — `courtId`, não `cId`.
- **Early return:** preferir retornar cedo a aninhar `if`s.
- **DRY:** lógica repetida em 2+ lugares vira método/hook/util reutilizável.
- **Sem números/strings mágicos:** extrair para constantes nomeadas.
- **Entidades JPA:** só mapeamento e getters/setters — regra de negócio fica no `service`, nunca na entidade ou no controller.
- **DTOs na borda:** controllers recebem/retornam DTOs, nunca entidades diretamente (padrão já seguido no projeto — manter).
- **Comentários:** só quando o _porquê_ não é óbvio pelo código.
- Code Review obrigatório em todo PR — é o principal guarda-corpo de qualidade, mais até que ferramentas automáticas.

## ✅ Definition of Ready (DoR)

Uma tarefa só pode ser puxada para desenvolvimento se cumprir **100%** dos itens abaixo:

- [ ] **História de Usuário padronizada:** _"Como [cliente/administrador], eu quero [ação] para que [benefício]"_.
- [ ] **Critérios de Aceite** descritos sem ambiguidade (ex.: "Não permitir cancelamento com menos de 2h de antecedência").
- [ ] **Regras de Negócio e Exceções mapeadas** (horário de pico, tarifas, limites por CPF).
- [ ] **Dependências mapeadas** (modelagem de banco, rotas de API).
- [ ] **Protótipo aprovado pelo PO** (para tarefas com interface).

## 🏁 Definition of Done (DoD)

Uma funcionalidade só é considerada **Concluída** se atender rigorosamente a:

- [ ] **Padrão de código** conforme o guia de estilo (React.js frontend / Java Spring Boot backend).
- [ ] **Code Review aprovado** por ≥ 1 dev/QA via Pull Request.
- [ ] **Validação de QA** nos ambientes de teste — sem bugs de severidade alta ou crítica em aberto.
- [ ] **Código mesclado na `dev`** sem conflitos de versionamento.
- [ ] **Documentação atualizada** — endpoints criados/alterados no Swagger/OpenAPI.

## 🔧 Gestão de Débito Técnico

### Registro

Todo atalho técnico, regra simplificada temporariamente ou pendência de refatoração deve ser registrado como **card no backlog do Jira com a tag `Débito Técnico`**, contendo:

- Justificativa da decisão.
- Localização no código.
- Impacto potencial na manutenibilidade.

### Priorização e Pagamento

- **15% do tempo útil de cada ciclo** dedicado à refatoração e pagamento de débitos.
- **Prioridade total** para débitos que causem:
  - Inconsistência na reserva de quadras.
  - Falha na autenticação de usuários.
  - Degradação do tempo de resposta da API de horários.

## 🧪 Estratégia de Testes

### Backend (JUnit 5 + Mockito)

Localização: `backend/src/test/java`. Dois tipos de teste convivem no projeto:

| Tipo       | Classes                                 | Precisa de banco?                     |
| ---------- | --------------------------------------- | ------------------------------------- |
| Unitário   | `UserServiceTest`, `BookingServiceTest` | Não (mocks via Mockito)               |
| Integração | `AgendaBeachApplicationTests`           | Sim (sobe o contexto Spring completo) |

**Via Docker (recomendado):**

```bash
docker compose up -d db
docker compose run --rm --build backend-unit-tests
docker compose run --rm --build backend-integration-tests
```

**Nativamente** (com Java 21 instalado):

```bash
cd backend
./mvnw test
```

### Frontend

O projeto ainda não tem suíte de testes automatizados (unitários/E2E) no frontend — hoje as garantias de qualidade são **ESLint**, **Prettier** e o **build do TypeScript**, que já rodam no CI a cada PR:

```bash
cd frontend
npm run lint
npx prettier --check "src/**/*.{ts,tsx,css}"
npm run build
```

> Introduzir Vitest + Testing Library para componentes e regras de negócio do frontend (cálculo de tarifas, validação de formulários) é uma melhoria recomendada — fica como sugestão para o time avaliar.

### Registro de Bugs

Todo bug identificado durante os testes deve ser documentado com:

- **Passos esperados**
- **Passos obtidos**
- **Severidade**

> Bugs de **alta severidade bloqueiam** o encerramento das tarefas.

## 🔄 CI/CD e Qualidade de Código

Pipeline 100% baseado em **Docker**, definido em `.github/workflows/ci.yml`, disparado a cada `push` ou Pull Request para `main`/`dev`. Cada etapa é um job isolado, para localizar rapidamente onde algo quebrou:

| Job                         | O que faz                                                                    | Depende de        |
| --------------------------- | ---------------------------------------------------------------------------- | ----------------- |
| `backend-compile`           | Compila o backend (`mvnw compile`) — falha rápido em erro de sintaxe/tipo    | —                 |
| `backend-unit-tests`        | Roda os testes unitários (Mockito, sem banco) em container                   | `backend-compile` |
| `backend-integration-tests` | Sobe um PostgreSQL real via Docker Compose e roda o teste de contexto Spring | `backend-compile` |
| `backend-image`             | Builda a imagem final de produção do backend                                 | testes acima      |
| `frontend`                  | Builda a imagem do frontend — o build já roda ESLint, Prettier e `tsc`       | —                 |
| `sonarqube`                 | Compila o backend nativamente e envia a análise ao SonarCloud                | testes + frontend |

**Regra de qualidade:** nenhum PR é aprovado com jobs vermelhos, ou com _issues_ críticas apontadas pelo SonarCloud (bugs, vulnerabilidades, code smells graves, duplicação acima do limite do Quality Gate). O SonarCloud comenta diretamente no PR com o resultado.

## 🗂 Estrutura de Pastas

```
AgendaBeach/
├── .github/
│   └── workflows/
│       └── ci.yml                  # Pipeline de CI/CD (GitHub Actions)
├── backend/                        # API REST — Java 21 + Spring Boot 4.1.1
│   ├── src/
│   │   ├── main/java/com/
│   │   │   ├── bookings/           # Domínio de quadras e reservas
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── enum/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── users/              # Domínio de usuários e autenticação
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── enums/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── config/             # Configurações (Spring Security etc.)
│   │   │   └── AgendaBeachApplication.java
│   │   ├── main/resources/
│   │   │   └── application.properties
│   │   └── test/java/com/          # Testes (JUnit 5 + Mockito)
│   ├── Dockerfile
│   ├── pom.xml
│   └── mvnw / mvnw.cmd
├── frontend/                        # SPA — React + TypeScript + Vite + Mantine
│   ├── src/
│   │   ├── api/                     # Cliente HTTP e tipos da API
│   │   ├── components/              # Um componente por pasta (Componente.tsx + .css + index.ts)
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── db/
│   └── init.sql                     # Schema do PostgreSQL (tabelas, enums, constraints)
├── docker-compose.yml
└── README.md
```
