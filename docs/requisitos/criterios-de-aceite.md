# Critérios de aceite – AgendaBeach

**Responsável:** Murilo Watanabe (PO e Revisor de Requisitos). **Versão:** 2.0 (etapa 3).
**Fonte das regras:** `docs/Sistema de Agendamento de Quadras de Beach Tennis.pdf`.

Cada critério é escrito no formato **Dado / Quando / Então** e deve ser verificável por um teste
automatizado ou por um roteiro de QA com evidência. Um requisito só recebe aceite quando **todos** os seus
critérios passam **na API e na interface**.

> **Regra de ouro (lição da etapa 3):** o valor oficial é sempre o que a API devolve. A interface exibe preço,
> horários e política de cancelamento vindos da API; não recalcula regras de negócio.

Horários são o horário local do estabelecimento (`America/Sao_Paulo`).

---

## Regras de negócio

### RN01 – Funcionamento e slots de horário

| ID       | Dado                                    | Quando                                         | Então                                                                 |
| -------- | --------------------------------------- | ---------------------------------------------- | --------------------------------------------------------------------- |
| CA-RN01.1 | uma quadra ativa e uma data futura     | o cliente abre a grade                         | a grade mostra **17** horários, de 06:00–07:00 até 22:00–23:00        |
| CA-RN01.2 | o modal de reserva aberto              | o cliente escolhe a duração                    | só existe a opção **1 hora** (sem 1h30 ou 2h)                         |
| CA-RN01.3 | uma chamada direta à API               | `POST /bookings` com início 05:00, 22:30 ou duração ≠ 1h | a API responde **400**                                     |
| CA-RN01.4 | um horário no passado                  | o cliente tenta reservar                       | a API responde **400** e a interface não oferece o horário            |

### RN02 – Preço por categoria de horário

| ID        | Dado                                               | Quando                   | Então                         |
| --------- | -------------------------------------------------- | ------------------------ | ----------------------------- |
| CA-RN02.1 | terça-feira, 10:00, quadra aberta ou coberta       | o cliente vê o horário   | o preço exibido é **R$ 60,00** |
| CA-RN02.2 | terça-feira, 16:00                                 | o cliente vê o horário   | **R$ 60,00**                  |
| CA-RN02.3 | terça-feira, 17:00                                 | o cliente vê o horário   | **R$ 100,00**                 |
| CA-RN02.4 | sábado ou domingo, qualquer horário                | o cliente vê o horário   | **R$ 100,00**                 |
| CA-RN02.5 | feriado nacional fixo (ex.: 15/11) em dia útil, 10:00 | o cliente vê o horário | **R$ 100,00**                 |
| CA-RN02.6 | feriado móvel (ex.: Sexta-feira Santa, 26/03/2027), 10:00 | o cliente vê o horário | **R$ 100,00**             |
| CA-RN02.7 | qualquer horário                                   | o cliente confirma       | o valor cobrado (`courtPrice`) é **igual** ao valor exibido |
| CA-RN02.8 | o ADMIN altera a tabela por `PUT /pricing`         | um cliente faz nova reserva | o novo valor vale só para reservas novas; as antigas mantêm o preço |

O tipo de quadra (coberta/aberta) **não** altera a tarifa.

### RN03 – Prevenção de choque de horários

| ID        | Dado                                        | Quando                                  | Então                                                       |
| --------- | ------------------------------------------- | --------------------------------------- | ----------------------------------------------------------- |
| CA-RN03.1 | uma reserva SCHEDULED na quadra 1 às 10:00  | outro cliente tenta a mesma quadra e hora | a API responde **409** e a grade mostra o horário ocupado |
| CA-RN03.2 | a mesma reserva                             | alguém tenta a quadra 2 às 10:00        | a reserva é criada                                          |
| CA-RN03.3 | uma reserva CANCELLED às 10:00              | outro cliente reserva o horário         | a reserva é criada                                          |

### RN04 – Política de cancelamento e reembolso

| ID        | Dado                                         | Quando              | Então                                                                       |
| --------- | -------------------------------------------- | ------------------- | --------------------------------------------------------------------------- |
| CA-RN04.1 | faltam **mais de 24h** para o início         | o cliente cancela   | reembolso de **100%** da parcela paga sem crédito; crédito usado é devolvido |
| CA-RN04.2 | faltam **entre 24h e 2h** (inclusive)        | o cliente cancela   | reembolso **zero** e **crédito de 50%** do preço total                      |
| CA-RN04.3 | faltam **menos de 2h**                       | o cliente cancela   | nem reembolso nem crédito                                                   |
| CA-RN04.4 | a reserva já começou, foi cancelada ou concluída | o cliente tenta cancelar | a API responde **409** e a interface não oferece a ação             |
| CA-RN04.5 | qualquer faixa acima                         | antes de confirmar  | a interface mostra o percentual e o tipo (**reembolso** ou **crédito**) que a API vai aplicar |

### RN05 – Limite de agendamentos em aberto

| ID        | Dado                                              | Quando                       | Então                                                       |
| --------- | ------------------------------------------------- | ---------------------------- | ----------------------------------------------------------- |
| CA-RN05.1 | um cliente com 3 reservas SCHEDULED futuras       | tenta a 4ª                   | a API responde **409** e a interface explica o limite em português |
| CA-RN05.2 | o mesmo cliente cancela uma delas                 | tenta de novo                | a reserva é criada                                          |
| CA-RN05.3 | duas requisições simultâneas da 3ª e 4ª reservas em quadras diferentes | processadas | só uma é aceita                                 |
| CA-RN05.4 | um usuário antigo **sem CPF**                     | abre a tela de reserva       | a interface pede o CPF antes de reservar                    |
| CA-RN05.5 | o usuário antigo informa um CPF válido            | tenta reservar               | a reserva é criada                                          |
| CA-RN05.6 | um CPF já usado por outra conta ou inválido       | o usuário tenta cadastrá-lo  | a API responde **409**/**400** com mensagem em português    |

---

## Requisitos funcionais

| ID        | Requisito | Critério                                                                                                                                       |
| --------- | --------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| CA-RF01.1 | RF01      | Com sessão **CLIENT**, a tela de reservas carrega quadras e calendário sem chamar `GET /bookings` nem `GET /bookings/search`                     |
| CA-RF01.2 | RF01      | O modal usa `GET /bookings/availability` e marca como ocupados os horários com `available = false`                                              |
| CA-RF01.3 | RF01      | A grade não mostra nome, e-mail nem ID de outros clientes                                                                                       |
| CA-RF02.1 | RF02      | Cliente autenticado seleciona quadra, data e horário livre e confirma; a reserva aparece em "Minhas reservas" com o preço cobrado              |
| CA-RF02.2 | RF02      | Com saldo de crédito, a opção "usar crédito" envia `useCredit = true` e a tela mostra `creditUsed` e `amountDue`                                |
| CA-RF03.1 | RF03      | O cancelamento segue CA-RN04.1 a CA-RN04.5 e o saldo de crédito é atualizado na tela                                                            |
| CA-RF04.1 | RF04      | O ADMIN cadastra, edita e inativa quadras; quadra inativa não aparece para reserva                                                              |
| CA-RF04.2 | RF04      | O ADMIN altera preço normal e de pico e cadastra/remove feriados pelo painel                                                                    |
| CA-RF04.3 | RF04      | Um CLIENT recebe **403** em qualquer escrita de quadras, preços ou feriados                                                                     |
| CA-RF05.1 | RF05      | O cliente vê suas reservas passadas e futuras com status Confirmado, Cancelado ou Concluído, e só as suas                                     |

## Requisitos não funcionais

| ID         | Requisito | Critério                                                                                                            | Forma de prova                     |
| ---------- | --------- | ------------------------------------------------------------------------------------------------------------------- | ---------------------------------- |
| CA-RNF01.1 | RNF01     | p95 de `GET /bookings/availability` **abaixo de 2 s** com **50 usuários virtuais** por 1 minuto                     | k6 no CI, falha acima do limite    |
| CA-RNF02.1 | RNF02     | Duas reservas simultâneas da mesma vaga: exatamente uma vence; dois cancelamentos simultâneos geram crédito uma vez | Teste de integração com PostgreSQL |
| CA-RNF03.1 | RNF03     | Fluxos de reserva e cancelamento concluídos em largura de **360 px** e **768 px** sem rolagem horizontal            | Roteiro de QA com prints, navegador e dispositivo registrados |
| CA-RNF04.1 | RNF04     | Todo endpoint novo ou alterado aparece em `/v3/api-docs`; camadas Controller → Service → Repository respeitadas     | Teste de OpenAPI + code review     |

---

## Rastreabilidade

Situação na base `1baf6c3`. "—" indica teste a criar (ver seção 6.3 do relatório da etapa 3).

| Critério                | Teste automatizado                                                    | Situação      |
| ----------------------- | --------------------------------------------------------------------- | ------------- |
| CA-RN01.3, CA-RN01.4    | `BookingRulesTest.rejectsInvalidHoursAndDurations`                    | Existe        |
| CA-RN01.1               | `AgendaBeachApplicationTests.pricingHolidaysHoursQuotaAndAvailability` | Existe (API) |
| CA-RN01.1, CA-RN01.2    | Teste de tela do `BookingModal` com sessão CLIENT                     | —             |
| CA-RN02.1 – CA-RN02.5   | `PricingServiceTest`                                                  | Existe (API)  |
| CA-RN02.1 – CA-RN02.4   | `frontend/src/pricing.rules.test.ts`                                  | — (falha hoje em 2 de 3) |
| CA-RN02.6               | Teste unitário de feriado móvel                                       | —             |
| CA-RN03.1               | `AgendaBeachApplicationTests.concurrentCancellationCreditsOnceAndDatabaseRejectsOverlaps` | Existe |
| CA-RN04.1 – CA-RN04.3   | `BookingServiceTest`, `AgendaBeachApplicationTests.cancellationPersistsCreditsAndAllowsFutureUseWithoutDuplicateCredit` | Existe (API) |
| CA-RN04.5               | `frontend/src/pricing.rules.test.ts`                                  | — (falha hoje em 1 de 3) |
| CA-RN05.1, CA-RN05.3    | `AgendaBeachApplicationTests.simultaneousSameSlotHasOneWinnerAndQuotaSerializesAcrossCourts` | Existe |
| CA-RN05.4, CA-RN05.5    | `AgendaBeachApplicationTests.updateCannotBypassRulesAndLegacyUsersCanRegisterCpf` (API) + teste de tela | API existe; tela — |
| CA-RN05.6               | `CpfValidatorTest`, `cpf.test.ts`, `AgendaBeachApplicationTests.cpfAndOpenApi` | Existe |
| CA-RF01.1, CA-RF01.2    | Teste de tela da `BookingPage` com sessão CLIENT                      | —             |
| CA-RF02.2               | Teste de tela de uso de crédito                                       | —             |
| CA-RF04.3               | `AgendaBeachApplicationTests.secureIdentityOwnershipRolesAndTokenExpiry` | Existe       |
| CA-RNF01.1              | Script k6                                                             | —             |
| CA-RNF02.1              | `AgendaBeachApplicationTests` (mesma vaga e cancelamento simultâneo)  | Existe        |
| CA-RNF04.1              | `AgendaBeachApplicationTests.cpfAndOpenApi`                           | Existe        |
