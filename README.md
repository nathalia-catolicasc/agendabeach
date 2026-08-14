# 🏖️ AgendaBeach

Plataforma para agendamento de quadras de **Beach Tennis**. Permite que clientes reservem horários com facilidade e que administradores gerenciem quadras, tarifas e ocupação.

Projeto acadêmico da disciplina de Engenharia de Software — Centro Universitário Católica SC.

---

## 📋 Sumário

- [Equipe](#-equipe)
- [Stack de Tecnologias](#-stack-de-tecnologias)
- [Como Rodar o Projeto](#-como-rodar-o-projeto)
- [Estratégia de Branches](#-estratégia-de-branches)
- [Padronização de Commits](#-padronização-de-commits)
- [Fluxo de Trabalho (Workflow)](#-fluxo-de-trabalho-workflow)
- [Guia de Estilo e Padrões de Código](#-guia-de-estilo-e-padrões-de-código)
- [Definition of Ready (DoR)](#-definition-of-ready-dor)
- [Definition of Done (DoD)](#-definition-of-done-dod)
- [Gestão de Débito Técnico](#-gestão-de-débito-técnico)
- [Estratégia de Testes](#-estratégia-de-testes)
- [CI/CD e Qualidade de Código](#-cicd-e-qualidade-de-código)

---

## 👥 Equipe

| Papel | Integrante |
|---|---|
| Product Owner (PO) | Nathalia Aline Berri Silva |
| Engenheira de Requisitos | Nathalia Aline Berri Silva |
| Quality Assurance (QA) | Gabriel Albani De Souza |
| Desenvolvedor Frontend | Murilo Enzo Watanabe |
| Desenvolvedor Backend | Vinícius Henrique Da Silva |
| DevOps | Miguel Augusto Guedes |

---

## 🛠 Stack de Tecnologias

- **Frontend:** React.js
- **Backend:** Java + Spring Boot
- **Banco de Dados:** PostgreSQL
- **Controle de Versão:** GitHub
- **Qualidade de Código:** ESLint, Prettier, SonarQube
- **Documentação de API:** Swagger / OpenAPI
- **Gestão de Backlog:** Jira

---

## 🚀 Como Rodar o Projeto

> ⚠️ Seção a ser preenchida conforme o backend e o frontend forem inicializados.

```bash
# Clonar o repositório
git clone https://github.com/MuriloWatanabe/AgendaBeach.git
cd AgendaBeach

# Backend (Spring Boot)
cd backend
./mvnw spring-boot:run

# Frontend (React)
cd frontend
npm install
npm run dev
```

---

## 🌿 Estratégia de Branches

Adotamos um modelo baseado no **Git Flow simplificado**:

| Branch | Finalidade |
|---|---|
| `main` (ou `master`) | Código **estável e pronto para produção/entrega**. Só recebe merge da `dev` em pontos de release. |
| `dev` | Código em **integração** para a entrega atual. Todas as features são mergeadas aqui primeiro. |
| `feat/<n-tarefa>/<nome-da-funcionalidade>` | Branches de nova funcionalidade. Ex.: `feat/12/cadastro-quadra` |
| `feat/<nome-da-funcionalidade>` | Branches de nova funcionalidade. Ex.: `feat/cadastro-quadra` |
| `fix/<n-tarefa>/<descricao>` | Branches de correção de bug. Ex.: `fix/27/reserva-duplicada` |

### Regras
- **Nunca commitar direto em `main` ou `dev`.** Sempre via Pull Request.
- Toda branch de trabalho parte da `dev` atualizada.
- Após o merge, a branch de feature é deletada.

---

## 📝 Padronização de Commits

Prefixos obrigatórios na mensagem de commit:

| Prefixo | Uso |
|---|---|
| `feat:` | Nova funcionalidade |
| `bug:` | Correção de bug |
| `refactor:` | Alteração/manutenção preventiva (sem mudança de comportamento) |
| `docs:` | Alterações na documentação |

**Exemplos:**
```
feat: adiciona endpoint de cadastro de quadras
bug: corrige cálculo de tarifa em horário de pico
refactor: extrai serviço de validação de reserva
docs: atualiza README com fluxo de branches
```

**Boas práticas:**
- Mensagem no imperativo e em minúsculas após o prefixo.
- Mensagens objetivas (≤ 72 caracteres na primeira linha).
- Um commit = uma mudança lógica.

---

## 🔁 Fluxo de Trabalho (Workflow)

1. **Puxar tarefa do Jira** que atenda ao [DoR](#-definition-of-ready-dor).
2. **Atualizar a `dev` local:**
   ```bash
   git checkout dev
   git pull origin dev
   ```
3. **Criar a branch de trabalho** a partir da `dev`:
   ```bash
   git checkout -b feat/<n-tarefa>/<nome-da-funcionalidade>
   ```
4. **Desenvolver** seguindo o [Guia de Estilo](#-guia-de-estilo-e-padrões-de-código) e commitar seguindo o padrão de commits.
5. **Subir a branch** e abrir Pull Request para a `dev`:
   ```bash
   git push origin feat/<n-tarefa>/<nome-da-funcionalidade>
   ```
6. **Abrir Pull Request** com:
   - Título no padrão do commit (`feat: ...`).
   - Descrição com o número da tarefa no Jira, o que foi feito e como testar.
   - Marcadores de labels (feat, bug, refactor, docs).
7. **Code Review obrigatório** por pelo menos **1 outro dev/QA** antes do merge.
8. **Validação de QA** conforme [DoD](#-definition-of-done-dod).
9. **Merge na `dev`** somente após aprovação, CI verde e sem conflitos.
10. Branch de trabalho **deletada após o merge**.

### Regras de Pull Request
- Nenhum PR é aprovado se houver **erros críticos** no ESLint, Prettier ou SonarQube.
- PRs devem estar atualizados com a `dev` antes do merge (rebase ou merge da `dev`).
- Descrição clara é obrigatória — PR sem contexto será solicitado ajuste.

---

## 🎨 Guia de Estilo e Padrões de Código

### Convenções gerais
- **Idioma do código:** Inglês (variáveis, funções, classes, comentários técnicos).
- **Nomenclatura de variáveis/funções:** `nmExemplo` (camelCase com prefixo semântico quando aplicável).
- **Nomenclatura de arquivos:** `MeuArquivo.extensão` (PascalCase para componentes/classes).

### Boas práticas de manutenibilidade
- ❌ Evitar duplicação de código — criar **funções e módulos reutilizáveis**.
- ✅ Funções/métodos com **responsabilidade única** (SRP).
- ❌ Evitar aninhamento desnecessário (early returns quando possível).
- ❌ Evitar uso desnecessário de memória (ex.: variáveis criadas para uso único).
- ✅ **Code Review obrigatório** em todo PR.

---

## ✅ Definition of Ready (DoR)

Uma tarefa só pode ser puxada para desenvolvimento se cumprir **100%** dos itens abaixo:

- [ ] **História de Usuário padronizada:** *"Como [cliente/administrador], eu quero [ação] para que [benefício]"*.
- [ ] **Critérios de Aceite** descritos sem ambiguidade (ex.: "Não permitir cancelamento com menos de 2h de antecedência").
- [ ] **Regras de Negócio e Exceções mapeadas** (horário de pico, tarifas, limites por CPF).
- [ ] **Dependências mapeadas** (modelagem de banco, rotas de API).
- [ ] **Protótipo aprovado pelo PO** (para tarefas com interface).

---

## 🏁 Definition of Done (DoD)

Uma funcionalidade só é considerada **Concluída** se atender rigorosamente a:

- [ ] **Padrão de código** conforme o guia de estilo (React.js frontend / Java Spring Boot backend).
- [ ] **Code Review aprovado** por ≥ 1 dev/QA via Pull Request.
- [ ] **Validação de QA** nos ambientes de teste — sem bugs de severidade alta ou crítica em aberto.
- [ ] **Código mesclado na `dev`** sem conflitos de versionamento.
- [ ] **Documentação atualizada** — endpoints criados/alterados no Swagger/OpenAPI.

---

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

---

## 🧪 Estratégia de Testes

### Cobertura e Níveis
- Foco primário dos testes automatizados: **regras de negócio (backend/domain)**.
- Antes de cada entrega oficial, o QA executa **roteiro de testes** cobrindo o fluxo principal e regras de negócio críticas.

### Registro de Bugs
Todo bug identificado durante os testes deve ser documentado com:
- **Passos esperados**
- **Passos obtidos**
- **Severidade**

> Bugs de **alta severidade bloqueiam** o encerramento das tarefas.

---

## 🔄 CI/CD e Qualidade de Código

- **Ferramentas configuradas:** ESLint, Prettier e SonarQube.
- **Regra de integração:** Nenhum Pull Request será aprovado se a verificação automática acusar **erros críticos**.
- Toda merge na `dev` dispara verificação de qualidade.
- Merges na `main` são feitos apenas em pontos de release, com toda a suíte de verificações aprovada.

---

## 📌 Compromisso da Equipe

Todos os membros da equipe leram, concordam e se comprometem a seguir as diretrizes deste documento para garantir a **qualidade, manutenibilidade e entrega sustentável** do software.

- [x] Product Owner (PO) — Nathalia
- [x] Engenheira de Requisitos — Nathalia
- [x] Quality Assurance (QA) — Gabriel
- [x] Desenvolvedor Frontend — Murilo
- [x] Desenvolvedor Backend — Vinícius
- [x] DevOps — Miguel

---

*Documento baseado no Acordo de Manutenibilidade e Engenharia de Software elaborado em 07/08/2026.*
