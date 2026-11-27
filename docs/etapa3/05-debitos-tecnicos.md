# 7. Débitos técnicos e plano de refatoração

**Responsável pela priorização:** Murilo Watanabe (PO). **Apoio:** toda a equipe.

O maior débito acumulado é o **frontend desalinhado do backend**. Resolvido isso, o sistema fica
próximo de produção com três fases curtas de trabalho.

## Critério de priorização

Segue o item 2.2 do acordo de engenharia: débitos que causam **inconsistência na reserva de quadras**,
**falha na autenticação** ou **degradação do tempo de resposta da API de horários** têm prioridade
total. Dentro de cada nível, vem antes o que bloqueia o perfil CLIENT.

| Prioridade | Significado                                                       |
| ---------- | ----------------------------------------------------------------- |
| Crítica    | Bloqueia produção ou um fluxo do cliente. Entra no ciclo atual    |
| Alta       | Requisito sem entrega completa ou sem prova. Entra no próximo ciclo |
| Média      | Risco de manutenção ou segurança sem efeito visível hoje          |
| Baixa      | Higiene de código                                                 |

## Backlog de débitos

| #   | Débito                                                                                                                   | Localização                                                                 | Origem                 | Prioridade | Dono              | Fase |
| --- | ------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------- | ---------------------- | ---------- | ----------------- | ---- |
| D1  | Regras de preço, horário e estorno duplicadas no frontend e divergentes do backend                                       | `frontend/src/pricing.ts`, `BookingModal.tsx:43-49`, `BookingPage.tsx:18`   | Etapa 1, agravado      | Crítica    | Vinícius          | 1    |
| D2  | Frontend depende de endpoints restritos a ADMIN (`GET /bookings`, `GET /bookings/search`)                                | `BookingPage.tsx:38`, `BookingModal.tsx:140`                                | Este ciclo             | Crítica    | Vinícius          | 1    |
| D3  | Funcionalidades só no backend: saldo e uso de crédito, preços e feriados no painel, CPF para usuário antigo              | `AdminPage.tsx`, `BookingModal.tsx`, `api/index.ts`                         | Este ciclo             | Alta       | Vinícius          | 2    |
| D4  | Esquema do banco em dois arquivos idênticos (`db/init.sql` e Flyway V1)                                                  | `docker-compose.yml:11`, `db/init.sql`, `V1__initial_schema.sql`            | Este ciclo             | Alta       | Gabriel           | 3    |
| D5  | Sem teste de carga (RNF01) e testes de frontend só de autenticação                                                       | `frontend/src/*.test.ts`                                                    | Etapas 1 e 2           | Alta       | Nathalia, Gabriel | 3    |
| D6  | Feriados nacionais fixos no código, sem feriados móveis; regra de pico repetida em `price()` e `dayPrices()`             | `PricingService.java:17,50-68`                                              | Este ciclo             | Média      | Miguel            | 2    |
| D7  | `RuntimeException` vira HTTP 500 em `CourtService`; mensagens da API em inglês exibidas ao usuário                       | `CourtService.java:45,54,71`, `BookingService.java:55,224`                  | Etapa 1 e este ciclo   | Média      | Miguel            | 2    |
| D8  | Tokens expirados nunca são apagados de `auth_tokens`; token no `localStorage`; login sem limite de tentativas            | `TokenService.java`, `session.ts:27-44`                                     | Este ciclo             | Média      | Miguel            | 3    |
| D9  | Processo fora do acordo: branches sem número de tarefa, commits grandes, PRs sem revisão registrada                       | Histórico do repositório (`d6f522d`, PRs #5–#10)                            | Este ciclo             | Média      | Gabriel, Murilo   | 3    |
| D10 | Pasta `enum` com pacote `enums`; rota duplicada `/bookings/api/bookings/{id}/cancel`; métodos sem uso em `AuthService`; `canTryAdmin` igual a `isAdmin` | `com/bookings/enum/`, `BookingController.java:66`, `AuthService.java` | Etapa 1 e este ciclo   | Baixa      | Miguel            | 3    |
| D11 | **Novo (revisão do PO):** o serviço `backend-unit-tests` só roda `UserServiceTest` e `BookingServiceTest`; `BookingRulesTest`, `PricingServiceTest` e `CpfValidatorTest` não rodam no CI | `docker-compose.yml:38` | Este ciclo | Alta | Gabriel | 1 |

> **Sobre o D11:** as regras RN01, RN02 e RN04 do backend estão cobertas justamente pelos testes que o CI
> não executa. Uma regressão nessas regras passaria com o pipeline verde. Entra na Fase 1 porque é uma
> mudança de uma linha e protege tudo o que a fase vai mexer.

## Plano de refatoração

Cada fase só termina quando **Nathalia confirma os testes da fase** e **Murilo dá o aceite** na interface.

### Fase 1 – Destravar o cliente (D1, D2, D11) — bloqueia produção

- [ ] Vinícius troca `listBookings` e `searchBookings` por `GET /bookings/availability` na `BookingPage` e
      no `BookingModal`.
- [ ] Vinícius remove as durações de 1h30 e 2h e gera a grade de 06:00 a 22:00 a partir da resposta da API.
- [ ] Vinícius exibe o preço vindo de `AvailabilityDTO.price` e remove o cálculo de `estimatePrice`.
- [x] Miguel expõe a política de cancelamento (percentual e tipo) no retorno da reserva.
- [ ] Gabriel inclui todos os testes unitários no serviço `backend-unit-tests` (D11).
- [ ] Nathalia fecha a fase quando `pricing.rules.test.ts` e os testes de tela com sessão CLIENT passarem.
- [ ] **Aceite do PO:** RN01, RN02, RN04 e RF01 validados na interface com sessão CLIENT.

### Fase 2 – Completar as funcionalidades (D3, D6, D7)

- [ ] Vinícius cria a tela de perfil com CPF, o saldo de crédito e a opção "usar crédito" na reserva.
- [ ] Vinícius adiciona preços e feriados ao painel administrativo.
- [x] Miguel unifica `price()` e `dayPrices()` em um único método e calcula feriados móveis a partir da Páscoa.
- [x] Miguel troca `RuntimeException` por `ResponseStatusException(404)` e padroniza as mensagens em português.
- [ ] Nathalia cobre feriado móvel, uso de crédito e usuário antigo com CPF.
- [ ] **Aceite do PO:** RN05, RF02, RF03 e RF04 validados na interface.

### Fase 3 – Pronto para produção (D4, D5, D8, D9, D10)

- [ ] Gabriel remove o `db/init.sql` do Docker Compose e deixa o Flyway como única fonte do esquema.
- [ ] Gabriel inclui um teste k6 de `GET /bookings/availability` no CI, com falha se o p95 passar de 2 s.
- [ ] Gabriel ativa proteção de branch em `main` e `dev` exigindo 1 aprovação e CI verde.
- [x] Miguel agenda a limpeza diária de tokens expirados e adiciona limite de tentativas no login.
- [x] Miguel renomeia a pasta `enum`, remove a rota duplicada, os métodos sem uso e o `canTryAdmin`.
- [ ] Vinícius move `toMinutes` e `toTimeLabel` para um utilitário comum.
- [x] Murilo registra a decisão sobre a convenção de commits ([ADR-001](../decisoes/ADR-001-convencao-de-commits-e-branches.md)),
      atualiza o [acordo de engenharia](../acordo-de-engenharia.md) e os [critérios de aceite](../requisitos/criterios-de-aceite.md).
- [ ] Nathalia executa a regressão completa (CLIENT e ADMIN) antes do merge em `main`.
- [ ] **Aceite do PO:** RNF01 medido, RNF03 com evidência e placar da [matriz](04-matriz-aceite.md) 100% ✅.

## Critério de pronto para produção

1. Zero regressões abertas (R1–R5).
2. Todas as suítes verdes no CI, incluindo os 5 testes unitários do backend (D11).
3. RNF01 medido sob carga (p95 < 2 s com 50 usuários).
4. Todo RN e RF com pelo menos um teste automatizado associado na
   [tabela de rastreabilidade](../requisitos/criterios-de-aceite.md#rastreabilidade).

## Registro dos débitos

Cada linha desta tabela vira uma Issue com o modelo **Débito técnico**
(`.github/ISSUE_TEMPLATE/debito-tecnico.md`) e a label `débito-técnico`. O número da Issue entra no nome
da branch que paga o débito, conforme o [ADR-001](../decisoes/ADR-001-convencao-de-commits-e-branches.md).
