# 3. Análise das manutenções e melhorias propostas

**Responsável:** Murilo Watanabe (PO). **Apoio:** Miguel (avaliação backend) e Vinícius (avaliação
frontend).

**Veredito:** concordamos com as 11 manutenções propostas e com a prioridade dada à autenticação e às
regras RN01, RN02 e RN04. A lacuna é que **nenhuma proposta trata do frontend**, que repete essas
mesmas regras.

## Avaliação de cada proposta

| #   | Proposta da equipe anterior                    | Tipo       | Prioridade dada | Prioridade do PO | Avaliação                                                                                 |
| --- | ---------------------------------------------- | ---------- | --------------- | ---------------- | ----------------------------------------------------------------------------------------- |
| P1  | Autenticação real e autorização por perfil     | Segurança  | Crítica         | Crítica          | Procede. O `X-User-Id` permitia agir como outro usuário                                   |
| P2  | RN01 – horário 06:00–23:00 e duração fixa de 1h | Corretiva | Crítica         | Crítica          | Procede. Deveria incluir a tela de reserva, que oferece 1h30 e 2h                         |
| P3  | RN02 – valores e regra de pico                 | Corretiva  | Crítica         | Crítica          | Procede. Deveria incluir o `pricing.ts` do frontend                                       |
| P4  | RN04 – política de cancelamento e crédito      | Corretiva  | Crítica         | Crítica          | Procede. Deveria incluir os textos de estorno exibidos ao cliente                         |
| P5  | RN05 – CPF e limite de 3 reservas futuras      | Corretiva  | Alta            | Alta             | Procede. Faltou prever como usuários antigos cadastram o CPF pela interface               |
| P6  | Crédito de cancelamento                        | Corretiva  | Alta            | Alta             | Procede. Faltou prever onde o cliente vê e usa o crédito                                  |
| P7  | Tabela de preços administrável                 | Melhoria   | Alta            | Alta             | Procede. Faltou a tela no painel administrativo                                           |
| P8  | Testes de concorrência (RNF02)                 | Preventiva | Alta            | Alta             | Procede                                                                                   |
| P9  | Swagger/OpenAPI                                | Melhoria   | Média           | Média            | Procede                                                                                   |
| P10 | Testes de frontend                             | Preventiva | Média           | **Alta**         | Procede. Pelo impacto real (R1, R2 e R5 passaram sem detecção), sobe para Alta            |
| P11 | Teste de desempenho (RNF01)                    | Preventiva | Média           | Média            | Procede                                                                                   |

As medidas preventivas (critérios de aceite testáveis, regras no backend, testes unitários e de
integração, code review e CI) estão alinhadas ao acordo de manutenibilidade de 07/08/2026.

## Itens que deveriam ter sido identificados e não foram

| #   | Item não identificado                                                                 | Consequência                                         | Vira débito |
| --- | ------------------------------------------------------------------------------------- | ---------------------------------------------------- | ----------- |
| N1  | Duplicação das regras de preço, horário e estorno entre backend e frontend           | Valor exibido ≠ valor cobrado (R5)                   | D1          |
| N2  | Endpoints usados pelo frontend que mudariam de permissão (`GET /bookings`, `GET /bookings/search`) | Tela de reservas quebrada para CLIENT (R1, R2) | D2          |
| N3  | Feriados móveis (Sexta-feira Santa, Corpus Christi) na RN02                           | Preço de dia útil cobrado em feriado                 | D6          |
| N4  | Mensagens de erro da API em inglês exibidas diretamente ao usuário                    | Experiência ruim e inconsistente                     | D7          |

## Regra para as próximas propostas

Para evitar a repetição de N1 e N2, toda proposta de manutenção passa a ter, antes da aprovação do PO,
a seção **"Impacto nas camadas"** com três respostas obrigatórias:

1. Quais arquivos do **backend** mudam?
2. Quais telas do **frontend** consomem o que muda (endpoint, campo, regra)?
3. Qual **teste automatizado** prova a regra nas duas camadas?

A regra está registrada no [Acordo de Engenharia v2](../acordo-de-engenharia.md#4-critérios-de-qualidade-dor-e-dod)
como item do DoR.
