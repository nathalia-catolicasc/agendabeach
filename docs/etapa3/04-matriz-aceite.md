# 4. Matriz de aceite RN/RF/RNF

**Registro:** Murilo Watanabe (PO). **Conferência:** Miguel (backend), Vinícius (frontend).
**Validação no fluxo:** Nathalia (QA). **Base:** commit `1baf6c3`.

Das 11 manutenções propostas, **3 foram aplicadas por completo, 7 ficaram parciais e 1 não foi feita**.
Em todas as parciais, o backend foi corrigido e o frontend continuou com a regra antiga.

Legenda da situação de aceite:

- ✅ **Aceito** — regra correta nas duas camadas e com teste automatizado.
- 🟡 **Parcial** — regra correta no backend; frontend divergente ou sem teste.
- ❌ **Recusado** — regra não implementada ou sem nenhuma evidência.

## Regras de negócio

| Req. | Backend                                                                                                         | Frontend                                                                                                                         | Aceite | Bloqueio                 |
| ---- | --------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- | ------ | ------------------------ |
| RN01 | `validateSlot` exige 1h cheia entre 06:00 e 23:00 — `BookingService.java:213-224`; grade de 17 horários — `BookingService.java:181` | Grade começa às 07:00 — `BookingPage.tsx:18`, `BookingModal.tsx:49`; oferece 1h30 e 2h — `BookingModal.tsx:43-47`               | 🟡     | R3, D1                   |
| RN02 | R$ 60/R$ 100, pico a partir das 17h, fim de semana e feriado — `PricingService.java:50-68`                      | R$ 100/120 por tipo de quadra, pico 18h–22h ×1,2 — `pricing.ts:9-12`                                                             | 🟡     | R5, D1, D6 (feriado móvel) |
| RN03 | `EXCLUDE USING GIST` sobre `tsrange` — `V1__initial_schema.sql:66-73`; locks de quadra — `BookingService.java:56` | Não se aplica (garantia do servidor)                                                                                             | ✅     | —                        |
| RN04 | 100% acima de 24h; 50% de crédito entre 24h e 2h — `BookingService.java:113-120`                               | Textos e cálculo de 24h/12h — `pricing.ts:58-81`                                                                                 | 🟡     | R5, D1                   |
| RN05 | Limite de 3 reservas futuras com lock do usuário — `BookingService.java:50-55`; CPF validado e único            | Cadastro pede CPF; não há tela para usuário antigo informar o CPF (`api/index.ts` não expõe `PUT /users/{id}`)                   | 🟡     | R4, D3                   |

## Requisitos funcionais

| Req. | Situação observada                                                                                                                    | Aceite | Bloqueio |
| ---- | ------------------------------------------------------------------------------------------------------------------------------------- | ------ | -------- |
| RF01 | `GET /bookings/availability` existe, mas a tela usa `GET /bookings` (`BookingPage.tsx:38`) e `GET /bookings/search` (`BookingModal.tsx:140`), ambos restritos a ADMIN (`BookingService.java:144,156,166`). Para CLIENT a grade não carrega | ❌     | R1, R2, D2 |
| RF02 | Reserva cria corretamente pela API; a tela permite durações inválidas e não envia `useCredit`                                         | 🟡     | R3, D3   |
| RF03 | Cancelamento correto no backend (`POST /bookings/{id}/cancel`); política exibida ao cliente está errada                               | 🟡     | R5       |
| RF04 | CRUD e status de quadras funcionam; `PUT /pricing` e feriados existem (`PricingController.java:35-60`) mas não há tela no painel      | 🟡     | D3       |
| RF05 | Histórico via `GET /users/{id}/bookings` com SCHEDULED/CANCELLED/COMPLETED — `MyReservationsPage.tsx:5,48-49`                         | ✅     | —        |

## Requisitos não funcionais

| Req.  | Situação observada                                                                                                                                                                                     | Aceite | Bloqueio |
| ----- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------ | -------- |
| RNF01 | Há verificação funcional de 5 requisições locais sequenciais abaixo de 2 s — `AgendaBeachApplicationTests.java:278-290`. Não há teste sob carga nem medição de p95 (**correção**: o relatório anterior dava "nenhuma evidência"; o README do backend já reconhece que não é garantia sob carga) | 🟡     | D5       |
| RNF02 | Testes de integração com PostgreSQL real: mesma vaga com um vencedor e cancelamento simultâneo — `AgendaBeachApplicationTests.java:333,398`                                                             | ✅     | —        |
| RNF03 | CSS com `@media` em 10 folhas de estilo; **sem registro de dispositivo testado** (ressalva DOC4)                                                                                                       | 🟡     | Evidência pendente (Nathalia) |
| RNF04 | Camadas Controller/Service/Repository/DTO/Entity; springdoc em `/swagger-ui` e `/v3/api-docs` (`pom.xml:80-81`)                                                                                         | ✅     | —        |

## Manutenções propostas × aplicação

| Manutenção                    | Backend                                                                          | Frontend                                          | Situação     | Evidência                              |
| ----------------------------- | -------------------------------------------------------------------------------- | ------------------------------------------------- | ------------ | -------------------------------------- |
| Autenticação e autorização    | Token Bearer opaco (SHA-256, validade 8h — `TokenService.java:40`); dono e ADMIN | Sessão guarda token e perfil vindo do login       | Aplicado     | `d6f522d`, `553a2b8`                   |
| Testes de concorrência (RNF02) | Integração com PostgreSQL real                                                   | —                                                 | Aplicado     | `AgendaBeachApplicationTests`          |
| Swagger/OpenAPI               | springdoc em `/swagger-ui` e `/v3/api-docs`                                      | —                                                 | Aplicado     | `d6f522d`, `d3cbd3d`                   |
| RN01 – horário e duração      | 1h cheia entre 06:00 e 23:00                                                     | Grade às 07:00, oferece 1h30 e 2h                 | Parcial      | `BookingModal.tsx:43-49`               |
| RN02 – preços                 | R$ 60/R$ 100, pico a partir das 17h                                              | R$ 100/120, pico 18h–22h ×1,2                     | Parcial      | `pricing.ts:9-12`                      |
| RN04 – cancelamento           | 100% > 24h; 50% crédito entre 24h e 2h                                           | Informa 50% de estorno entre 12h e 24h            | Parcial      | `pricing.ts:58-81`                     |
| RN05 – CPF e limite de 3      | CPF validado e único; limite com lock                                            | Sem tela de CPF para usuário antigo               | Parcial      | `553a2b8`                              |
| Crédito de cancelamento       | Saldo no usuário, `useCredit`, `GET /users/me/credits`                           | Saldo não aparece e não pode ser usado            | Parcial      | `BookingService.java:68`, `UserController.java:53` |
| Tabela de preços administrável | `PUT /pricing` e feriados (ADMIN)                                               | Painel sem tela de preços                         | Parcial      | `PricingController.java:35-60`         |
| Testes de frontend            | —                                                                                | 21 testes, só de autenticação e CPF               | Parcial      | `auth.test.ts`, `cpf.test.ts`          |
| Teste de desempenho (RNF01)   | Só verificação funcional local                                                   | —                                                 | Não aplicado | —                                      |

## Placar de aceite

| Grupo | ✅ Aceito | 🟡 Parcial | ❌ Recusado |
| ----- | -------- | --------- | ---------- |
| RN    | 1        | 4         | 0          |
| RF    | 1        | 3         | 1          |
| RNF   | 2        | 2         | 0          |
| **Total (14)** | **4** | **9** | **1** |

**Decisão do PO:** o sistema **não recebe aceite para produção**. O aceite de RN01, RN02, RN04 e RF01 é
reavaliado ao fim da Fase 1 do [plano de refatoração](05-debitos-tecnicos.md#plano-de-refatoração), na
interface, com sessão CLIENT, seguindo os [critérios de aceite](../requisitos/criterios-de-aceite.md).
