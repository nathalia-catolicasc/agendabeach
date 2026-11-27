# Etapa 3 – Retorno e Integração do AgendaBeach

Documentação de trabalho da equipe de origem sobre o ciclo de manutenção feito pela equipe anterior
entre 25/09/2026 e 30/10/2026.

| Item               | Valor                                                                           |
| ------------------ | ------------------------------------------------------------------------------- |
| Base comparada     | `e31e4e9` (18/09/2026, entrega da equipe de origem) × `1baf6c3` (30/10/2026)    |
| Relatório completo | `Relatorio_Etapa3_AgendaBeach.pdf` (entregue à disciplina)                      |
| Responsável        | Murilo Watanabe – Product Owner e Revisor de Requisitos                         |

## Documentos desta pasta

| #   | Documento                                              | Seção do relatório | Responsável principal |
| --- | ------------------------------------------------------ | ------------------ | --------------------- |
| 1   | [Redefinição dos perfis](01-perfis.md)                 | 1                  | Murilo (PO)           |
| 2   | [Análise da documentação anterior](02-analise-documentacao.md) | 2          | Murilo (PO)           |
| 3   | [Análise das propostas de manutenção](03-analise-propostas.md) | 3          | Murilo (PO)           |
| 4   | [Matriz de aceite RN/RF/RNF](04-matriz-aceite.md)      | 4                  | Murilo (registro)     |
| 5   | [Débitos técnicos e plano de refatoração](05-debitos-tecnicos.md) | 7       | Murilo (priorização)  |

Documentos relacionados fora desta pasta:

- [Critérios de aceite testáveis](../requisitos/criterios-de-aceite.md)
- [Acordo de Engenharia v2](../acordo-de-engenharia.md)
- [Registro de decisões (ADR)](../decisoes/)

## Resumo para quem chega agora

O backend voltou fiel às regras RN01–RN05, com autenticação Bearer, Swagger e testes de concorrência.
O frontend não acompanhou: **um cliente comum não consegue abrir a tela de reservas** (R1) nem ver os
horários ocupados (R2), e valores e estornos exibidos divergem dos cobrados (R5). A Fase 1 do
[plano de refatoração](05-debitos-tecnicos.md#plano-de-refatoração) resolve isso e precede qualquer
melhoria.
