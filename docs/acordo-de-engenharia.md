# Acordo de Manutenibilidade e Engenharia de Software – v2

**Projeto:** AgendaBeach – Agendamento de Quadras de Beach Tennis
**Versão 1:** 07/08/2026 (`docs/Acordo de Manutenibilidade e Engenharia de So.pdf`)
**Versão 2:** 27/11/2026 – revisão da etapa 3
**Responsável pela revisão:** Murilo Watanabe (PO e Revisor de Requisitos)

Este documento **substitui a v1** onde houver diferença. As mudanças estão listadas no
[histórico de alterações](#histórico-de-alterações) e cada uma remete à evidência que a motivou.

## 1. Equipe e papéis (etapa 3)

| Papel                                 | Integrante                 |
| ------------------------------------- | -------------------------- |
| Product Owner e Revisor de Requisitos | Murilo Enzo Watanabe       |
| QA de Regressão                       | Nathalia Aline Berri Silva |
| DevOps                                | Gabriel Albani De Souza    |
| Desenvolvedor Backend                 | Miguel Augusto Guedes      |
| Desenvolvedor Frontend                | Vinícius Henrique Da Silva |

Detalhes e justificativa: [docs/etapa3/01-perfis.md](etapa3/01-perfis.md).

## 2. Processo e gestão de débito técnico

### 2.1 Registro

Todo atalho técnico, regra simplificada ou refatoração pendente vira uma **Issue no GitHub** com o modelo
**Débito técnico** e a label `débito-técnico` (a v1 citava o Jira, que não foi adotado). A Issue contém:

- justificativa da decisão;
- localização no código (`arquivo:linha`);
- impacto potencial na manutenibilidade;
- prioridade (Crítica, Alta, Média, Baixa) definida pelo PO.

### 2.2 Priorização e pagamento

- **15% do tempo útil de cada ciclo** vai para refatoração e pagamento de débitos.
- **Prioridade total**, no ciclo imediatamente seguinte, para débitos que causem inconsistência na
  reserva, falha de autenticação ou degradação do tempo de resposta da API de horários.
- **Novo:** débito que faz o frontend divergir de uma regra de negócio do backend é sempre **Crítico**.

Backlog atual: [docs/etapa3/05-debitos-tecnicos.md](etapa3/05-debitos-tecnicos.md).

## 3. Requisitos e aceite

- As regras RN01–RN05, RF01–RF05 e RNF01–RNF04 da especificação original continuam valendo.
- Cada requisito tem critérios **Dado / Quando / Então** em
  [docs/requisitos/criterios-de-aceite.md](requisitos/criterios-de-aceite.md), mantidos pelo PO.
- **Regra de fonte única:** regras de negócio (preço, horário, cancelamento, limite) existem **só no
  backend**. O frontend exibe o que a API devolve e não recalcula.
- O aceite do PO é feito **na interface**, com os perfis **CLIENT e ADMIN**. Aceite só pela API não vale.

## 4. Critérios de qualidade: DoR e DoD

### 4.1 Definition of Ready (DoR)

Uma tarefa só é puxada para desenvolvimento se cumprir 100% dos itens:

- [ ] História de usuário: *"Como [cliente/administrador], eu quero [ação] para que [benefício]"*.
- [ ] Critérios de aceite sem ambiguidade, com ID (`CA-RN02.3`) quando ligados a um requisito.
- [ ] Regras de negócio e exceções mapeadas.
- [ ] Dependências mapeadas (tabelas, FKs, rotas da API).
- [ ] **Novo – Impacto nas camadas:** lista dos arquivos de backend que mudam, das telas de frontend que
      consomem o que muda e do teste que prova a regra nas duas camadas.
- [ ] **Novo – Contrato de permissão:** para endpoint novo ou alterado, qual perfil pode chamá-lo e
      quais telas o usam.
- [ ] Protótipo aprovado pelo PO (tarefas com interface).

### 4.2 Definition of Done (DoD)

- [ ] Código conforme o guia de estilo (seção 5).
- [ ] Pull Request aprovado por **pelo menos 1 pessoa que não seja o autor**.
- [ ] Validação de QA com os perfis **CLIENT e ADMIN**, sem bug de severidade alta ou crítica em aberto.
- [ ] **Novo:** teste automatizado para cada critério de aceite tocado pela tarefa.
- [ ] Mesclado na `dev` sem conflitos e com CI verde.
- [ ] Endpoints criados ou alterados documentados no Swagger/OpenAPI.
- [ ] **Novo:** README ou documentação da etapa atualizados quando a tarefa muda comportamento visível.

## 5. Guia de estilo e padrões de código

- Frontend React + TypeScript; backend Java + Spring Boot; banco PostgreSQL; controle de versão GitHub.
- Código em inglês; arquivos em `PascalCase` para componentes e classes.
- Variáveis e funções em `camelCase` com nomes que expliquem a intenção. **Novo:** proibidos nomes de uma
  letra fora de laços curtos (ex.: `b`, `p`) e nomes totalmente qualificados no meio do código
  (`java.util.Map`, `com.users.service.AuthService`) — use `import`.
- Camadas Controller → Service → Repository, com DTOs na borda.
- Evitar duplicação, manter responsabilidade única e evitar aninhamento desnecessário.
- **Novo:** mensagens de erro exibidas ao usuário em português.
- ESLint, Prettier e `tsc` sem erros; SonarCloud sem issues críticas.

## 6. Versionamento e CI/CD

### 6.1 Branches

- `main`: código estável, pronto para entrega. Recebe merge só da `dev`.
- `dev`: integração da entrega atual.
- Branches de trabalho: `<tipo>/<n-issue>/<descricao-em-kebab-case>`, sempre em minúsculas.

### 6.2 Commits

**Conventional Commits**, com `fix` no lugar de `bug` e o número da Issue no rodapé. Decisão completa e
tabela de tipos em [ADR-001](decisoes/ADR-001-convencao-de-commits-e-branches.md).

### 6.3 Verificação automática

- Nenhum PR é mesclado com job vermelho no CI ou issue crítica no SonarCloud.
- **Todas** as suítes de teste do backend e do frontend rodam no CI.
- Proteção de branch em `main` e `dev`: 1 aprovação e CI verde obrigatórios.
- **Novo:** teste de carga (k6) de `GET /bookings/availability` com limite de p95 < 2 s (RNF01).

### 6.4 Decisões

Qualquer mudança neste acordo é registrada antes como ADR em [docs/decisoes/](decisoes/) e aprovada no PR
que a introduz.

## 7. Compromisso da equipe

Todos os membros leem, revisam o PR que publica esta versão e se comprometem a seguir estas diretrizes.

- [ ] Product Owner e Revisor de Requisitos – Murilo Enzo Watanabe
- [ ] QA de Regressão – Nathalia Aline Berri Silva
- [ ] DevOps – Gabriel Albani De Souza
- [ ] Desenvolvedor Backend – Miguel Augusto Guedes
- [ ] Desenvolvedor Frontend – Vinícius Henrique Da Silva

## Histórico de alterações

| Versão | Data       | Item     | Mudança                                                               | Motivo / evidência                                  |
| ------ | ---------- | -------- | --------------------------------------------------------------------- | --------------------------------------------------- |
| 1.0    | 07/08/2026 | —        | Versão inicial                                                        | —                                                   |
| 2.0    | 27/11/2026 | 1        | Papéis da etapa 3                                                     | Rotação de perfis                                   |
| 2.0    | 27/11/2026 | 2.1      | Jira → GitHub Issues                                                  | O time usa GitHub Issues e Projects                 |
| 2.0    | 27/11/2026 | 2.2      | Divergência frontend × backend é débito Crítico                       | Regressões R1, R2 e R5                              |
| 2.0    | 27/11/2026 | 3        | Regra de fonte única e aceite na interface                            | `pricing.ts` divergente do `PricingService`          |
| 2.0    | 27/11/2026 | 4.1      | DoR com impacto nas camadas e contrato de permissão                   | `GET /bookings` virou ADMIN e quebrou a tela do CLIENT |
| 2.0    | 27/11/2026 | 4.2      | DoD exige revisor diferente do autor, teste por critério e QA com CLIENT e ADMIN | PRs #5–#10 mesclados pelo próprio autor     |
| 2.0    | 27/11/2026 | 5        | Regras de legibilidade e mensagens em português                       | Seção 5.1 do relatório da etapa 3; D7               |
| 2.0    | 27/11/2026 | 6.1, 6.2 | Conventional Commits e número da Issue obrigatório na branch          | ADR-001                                             |
| 2.0    | 27/11/2026 | 6.3      | Todas as suítes no CI, proteção de branch e k6                        | D5, D9, D11                                         |
