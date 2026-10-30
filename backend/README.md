# Backend AgendaBeach

Java 21, Spring Boot 4.1.1 e PostgreSQL 16. Estas mudanças atendem às regras dos documentos de requisitos somente no backend. Nenhum arquivo do frontend foi alterado.

## Executar

Na raiz do repositório, inicie apenas o banco com `docker compose up -d db`. Em `backend/`, configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` conforme o ambiente e execute:

```sh
bash mvnw spring-boot:run
```

O healthcheck é `GET /actuator/health`. A documentação interativa fica em `/swagger-ui/index.html` e o contrato OpenAPI em `/v3/api-docs`. Esses três endpoints não exigem autenticação. Também é possível executar somente banco e API com `docker compose up -d --build db backend` na raiz.

A imagem de produção executa a API como `app:app` (UID/GID 10001), sem privilégios de root. O JAR permanece de propriedade de root, disponível apenas para leitura pelo usuário da aplicação. A porta 8080 e o healthcheck permanecem iguais.

## Migração de banco

Flyway executa as migrações em `src/main/resources/db/migration/` antes da validação Hibernate. Bancos vazios recebem V1 e V2. Bancos existentes criados pelo `db/init.sql` são reconhecidos como V1 e recebem V2, sem recriar tabelas nem apagar reservas. O usuário do banco precisa poder criar extensão `btree_gist`, tabelas e índices, ou a extensão deve ser provisionada antes pelo administrador.

Faça backup antes de atualizar uma instalação com dados reais. Não execute manualmente `db/init.sql` sobre um banco existente nem altere migrações já aplicadas.

Usuários antigos permanecem com CPF nulo até cadastrá-lo por `PUT /users/{id}`. Não podem fazer novas reservas sem CPF válido. O CPF é único, normalizado para 11 dígitos, validado pelos dígitos verificadores e não pode ser trocado após o cadastro. O CPF não é exposto nas respostas públicas de usuário. Reservas antigas e seus preços são preservados; alterações de horário devem obedecer às novas regras.

## Autenticação e autorização

`POST /users` recebe `name`, `email`, `password` (8–72 caracteres) e `cpf` (11 dígitos ou formato `000.000.000-00`). Novos usuários sempre recebem perfil CLIENT.

`POST /users/login` recebe `email` e `password` e retorna:

```json
{
  "id": 1,
  "name": "Cliente",
  "email": "cliente@example.com",
  "profile": "CLIENT",
  "accessToken": "<token retornado pelo login>",
  "tokenType": "Bearer",
  "expiresAt": "2026-10-21T18:00:00"
}
```

Envie `Authorization: Bearer <accessToken>` nas requisições protegidas. Os tokens são aleatórios, válidos por oito horas e armazenados somente como SHA-256 no banco. `POST /users/logout` revoga o token atual. Não há autenticação por cookie, sessão ou `X-User-Id`. Use HTTPS na implantação.

O proprietário e o administrador podem consultar/alterar o usuário e suas reservas. Listagem geral de usuários, reservas e buscas que expõem usuários são restritas ao administrador. Alterações em quadras, tarifas e feriados também exigem ADMIN. A criação de administradores permanece uma operação controlada da instalação, sem endpoint público de promoção.

**Compatibilidade:** o frontend integra cadastro com CPF e login Bearer, usa o perfil retornado pela API e revoga o token ao sair. Sessões antigas sem token exigem novo login. Os fluxos de grade, preços e reservas ainda precisam de integração com os novos contratos, pois a interface pode gerar durações e valores incompatíveis e consultar listagens administrativas. Não há autenticação por `X-User-Id`. O proxy do frontend também não encaminha `/pricing`, `/v3/api-docs` ou `/swagger-ui`; esses recursos estão disponíveis diretamente na API.

## Reservas e disponibilidade

| Operação | Endpoint | Permissão |
| --- | --- | --- |
| Grade sem dados pessoais | `GET /bookings/availability?date=2026-10-23&courtId=1` | Autenticado |
| Criar reserva | `POST /bookings` | Próprio usuário; ADMIN pode indicar outro usuário |
| Histórico do usuário | `GET /users/{id}/bookings` | Proprietário ou ADMIN |
| Consultar/alterar/cancelar | `GET/PUT /bookings/{id}`, `POST /bookings/{id}/cancel` | Proprietário ou ADMIN |
| Saldo de crédito | `GET /users/me/credits` | Próprio usuário |
| Tarifas | `GET /pricing`, `PUT /pricing` | Autenticado / ADMIN |
| Feriados adicionais | `GET/PUT /pricing/holidays`, `DELETE /pricing/holidays/{date}` | Autenticado / ADMIN nas escritas |

Criação de reserva:

```json
{
  "courtId": 1,
  "startTime": "2026-10-23T06:00:00",
  "endTime": "2026-10-23T07:00:00",
  "useCredit": true
}
```

`userId` é opcional; quando omitido, vem da identidade autenticada. Um cliente não pode indicar outro usuário. `useCredit` é opcional e assume falso; quando verdadeiro, abate o menor valor entre saldo e preço. A resposta inclui `courtPrice` (preço total), `creditUsed` e `amountDue` (parcela restante).

- RN01: exatamente uma hora, com início em hora cheia entre 06:00 e 22:00, encerrando até 23:00, em quadra ativa e no futuro. A grade contém 17 intervalos, sem nomes ou IDs de clientes.
- RN02: R$ 60,00 em dias úteis, das 06:00 até antes das 17:00; R$ 100,00 a partir das 17:00, nos finais de semana e feriados. O tipo de quadra não altera a tarifa. `PUT /pricing` recebe `regularPrice` e `peakPrice`; alterações afetam apenas novas reservas.
- Feriados nacionais de data fixa são reconhecidos automaticamente (01/01, 21/04, 01/05, 07/09, 12/10, 02/11, 15/11, 20/11 e 25/12). Cadastre feriados móveis, estaduais e municipais conforme o calendário do estabelecimento com `PUT /pricing/holidays`, por exemplo `{"date":"2027-03-26","description":"Sexta-feira Santa"}`. O backend não depende de serviço externo de calendário.
- RN03/RNF02: transações e locks de quadra protegem a reserva; a exclusão GiST/TSRANGE do PostgreSQL continua como última proteção contra sobreposição.
- RN05: no máximo três reservas SCHEDULED com início futuro por CPF. CPF único e lock de usuário protegem esse limite mesmo em solicitações simultâneas para quadras diferentes. Reservas canceladas ou passadas não consomem o limite.
- Datas da API representam o horário local do estabelecimento, `America/Sao_Paulo`. O cálculo usa um `Clock` injetável, sem depender do fuso do sistema operacional.

`PUT /bookings/{id}` valida os mesmos horários e conflitos, não transfere proprietário nem reativa reservas encerradas. Para impedir contorno das regras financeiras, só permite remarcação com mais de 24 horas de antecedência, sem crédito consumido e mantendo o mesmo preço. Nos demais casos, cancele e faça uma nova reserva. `DELETE /bookings/{id}` aplica o cancelamento, preservando histórico e regras financeiras.

## Cancelamento e crédito

| Antecedência exata | Reembolso | Crédito |
| --- | --- | --- |
| Maior que 24 horas | 100% da parcela sem crédito | Devolução do crédito utilizado |
| De 2 até 24 horas, inclusive | Zero | 50% do preço total |
| Menor que 2 horas, antes do início | Zero | Zero |

Após o início, ou quando já cancelada/concluída, a reserva não pode ser cancelada novamente. A resposta separa `refundAmount` e `creditAmount`. Valores e momento do cancelamento ficam registrados na reserva e o saldo é persistido no usuário. Cancelamento e saldo mudam na mesma transação, com locks que evitam crédito duplicado ou perda de saldo. Valores são arredondados para centavos.

O projeto não possui integração com meios de pagamento. `refundAmount` registra o valor de reembolso devido; não executa transferência bancária ou estorno em operadora. O crédito interno já pode ser utilizado em reservas futuras.

Erros de entrada retornam 400; autenticação ausente/inválida, 401; falta de permissão, 403; recurso inexistente na reserva, 404; conflitos de horário, CPF, limite ou estado, 409.

## Testes

Com Java 21 e PostgreSQL disponíveis, em `backend/`:

```sh
bash mvnw test
bash mvnw package
```

Sem banco, execute somente os testes unitários:

```sh
bash mvnw -Dtest=UserServiceTest,BookingServiceTest,BookingRulesTest,PricingServiceTest,CpfValidatorTest test
```

A integração usa o PostgreSQL configurado nas variáveis `SPRING_DATASOURCE_*`, com defaults locais de desenvolvimento. Ela cria e limpa **somente o schema reservado `agendabeach_backend_test`** e um schema temporário `legacy_upgrade_test`; use banco de desenvolvimento/testes com permissão de criação de schemas. Não execute essa suíte contra produção. As tabelas normais de desenvolvimento no schema `public` ficam preservadas.

A suíte cobre validação de CPF, horários e duração, preços, feriados, limites exatos de 24h/2h, reuso de créditos, permissões, revogação/expiração de token, atualização, migração de instalação antiga, concorrência de reserva/quota/cancelamento e atualização de perfil sem perda de créditos, além da restrição de exclusão do PostgreSQL. A consulta de disponibilidade é verificada abaixo de dois segundos em cinco requisições locais; isso é uma verificação funcional de desempenho, não uma garantia sob carga de produção.

Em `frontend/`, `npm test` executa os testes de CPF, contratos de cadastro/login, sessão e envio de tokens. O build Docker do frontend também executa essa suíte, além de lint, Prettier e compilação.
