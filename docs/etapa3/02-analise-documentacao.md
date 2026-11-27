# 2. Análise da documentação da equipe anterior

**Responsável:** Murilo Watanabe (PO e Revisor de Requisitos). **Apoio:** Nathalia Berri (QA).

**Veredito:** o diagnóstico está correto — cada apontamento foi conferido contra o código da etapa 1 e
todos procedem. As falhas são de **forma e de rastreabilidade**, não de conteúdo.

## Artefatos recebidos

| Artefato                                                              | Local                                              | Tamanho              |
| --------------------------------------------------------------------- | -------------------------------------------------- | -------------------- |
| Relatório *Análise de Requisitos e Manutenção do Sistema AgendaBeach* | `docs/Atualizacao Engenharia de Requisitos.pdf`    | 13 páginas           |
| README do backend                                                     | `backend/README.md`                                | 116 linhas (novo)    |
| Revisão do README da raiz                                             | `README.md`                                        | revisão              |

Referências usadas na conferência:

- Especificação original: `docs/Sistema de Agendamento de Quadras de Beach Tennis.pdf`
- Acordo de engenharia de 07/08/2026: `docs/Acordo de Manutenibilidade e Engenharia de So.pdf`

## Pontos fortes

- Cada RN, RF e RNF tem situação e justificativa em tabela, o que facilita o aceite.
- O diagnóstico bate com o código entregue na etapa 1: início às 07:00, durações de 1h/1h30/2h,
  preços de R$ 100/120 com pico 18h–22h, reembolso em 24h/12h e identificação pelo header `X-User-Id`.
- Separa medidas corretivas, preventivas e de segurança e prioriza em Crítica/Alta/Média.
- O `backend/README.md` explica a migração Flyway, os usuários antigos sem CPF, a autenticação Bearer
  e o Swagger.

## Pontos a melhorar

| #    | Problema                                                                                     | Onde                         | Impacto                                                         | Ação pedida                                                             |
| ---- | -------------------------------------------------------------------------------------------- | ---------------------------- | --------------------------------------------------------------- | ----------------------------------------------------------------------- |
| DOC1 | Tabela de prioridades duplicada, com resíduos de LaTeX (`hline`)                             | Relatório, cap. 8            | Leitura confusa; parece versão não revisada                     | Remover a duplicata e os resíduos                                       |
| DOC2 | Capítulo 10 "Referências" está no sumário mas não tem conteúdo                               | Relatório, p. 13             | Documento incompleto                                            | Citar especificação, acordo e repositório                               |
| DOC3 | Nenhum apontamento cita arquivo, classe ou linha                                             | Relatório, cap. 4            | Difícil reproduzir a análise                                    | Usar a [matriz de aceite](04-matriz-aceite.md), que traz `arquivo:linha` |
| DOC4 | RNF03 marcado como "Aplicado" sem evidência (print, dispositivo testado)                     | Relatório, cap. 4.3          | Aceite sem prova                                                | Registrar dispositivo, navegador e largura testados                     |
| DOC5 | Não menciona que as regras também estão duplicadas no frontend (`frontend/src/pricing.ts`)  | Relatório, cap. 5.2          | Levou à correção só no backend (ver D1)                         | Incluir o frontend no escopo de cada corretiva                          |
| DOC6 | Afirma que "nenhum arquivo do frontend foi alterado"                                         | `backend/README.md`, linha 3 | Desatualizado após `553a2b8`, que alterou cadastro e sessão     | Corrigido nesta etapa                                                   |
| DOC7 | Convenção de commits trocada de `feat/bug/refactor/docs` para Conventional Commits sem registro de decisão | `README.md`  | Mudança no acordo de engenharia sem aprovação da equipe de origem | Decisão registrada em [ADR-001](../decisoes/ADR-001-convencao-de-commits-e-branches.md) |
| DOC8 | RNF01 dado como "não comprovado", mas `AgendaBeachApplicationTests` já mede 5 requisições locais abaixo de 2 s | Relatório, cap. 4.3 × `backend/README.md` | Os dois documentos se contradizem | Tratado na [matriz de aceite](04-matriz-aceite.md#requisitos-não-funcionais) como "parcial" |
| DOC9 | README da raiz desatualizado: URL de clone do fork, porta 5432 que não é mais publicada, frontend "sem testes", Jira citado como ferramenta | `README.md` | Quem segue o README não consegue reproduzir o ambiente | Corrigido nesta etapa |

## Conclusão do PO

A documentação recebida é **aceita com ressalvas**. O conteúdo técnico é aproveitado integralmente; as
ressalvas DOC6, DOC7, DOC8 e DOC9 são corrigidas pela equipe de origem nesta etapa, e DOC1–DOC5 ficam
como recomendação para a próxima revisão do relatório.
