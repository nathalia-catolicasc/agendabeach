## O que muda

<!-- Uma mudança lógica por PR. Descreva o porquê, não só o quê. -->

Closes #<n-issue>

## Impacto nas camadas

- **Backend:** <!-- arquivos/endpoints alterados ou "nenhum" -->
- **Frontend:** <!-- telas que consomem o que mudou ou "nenhum" -->
- **Permissões:** <!-- endpoint novo/alterado: qual perfil pode chamar? -->

## Critérios de aceite cobertos

<!-- IDs de docs/requisitos/criterios-de-aceite.md, ex.: CA-RN02.1, CA-RF01.1 -->

## Checklist (DoD – Acordo de Engenharia v2)

- [ ] Branch no formato `<tipo>/<n-issue>/<descricao>` e commits em Conventional Commits (ADR-001)
- [ ] Teste automatizado para cada critério de aceite tocado
- [ ] Fluxo validado com sessão **CLIENT** e **ADMIN**
- [ ] Frontend não recalcula regra de negócio (preço, horário, cancelamento, limite)
- [ ] Swagger/OpenAPI atualizado para endpoints novos ou alterados
- [ ] Documentação atualizada quando o comportamento visível muda
- [ ] CI verde e SonarCloud sem issues críticas
- [ ] Revisado por pelo menos 1 pessoa que não é o autor
