# 1. Redefinição dos perfis

**Responsável:** Murilo Watanabe (PO). **Apoio:** toda a equipe.

Na etapa 3 todos os integrantes trocaram de papel. O foco mudou de construir para auditar e integrar o
que voltou, e cada pessoa revisa uma parte do sistema que não era sua na etapa 1.

| Integrante              | Perfil na etapa 1                        | Perfil na etapa 3                     | Responsabilidades nesta etapa                                                                                                                                                          |
| ----------------------- | ---------------------------------------- | ------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Nathalia Berri          | Product Owner e Engenheira de Requisitos | QA de Regressão                       | Executar as suítes e os fluxos da etapa 1 com os perfis CLIENT e ADMIN; mapear regressões; escrever os testes dos novos cenários e fechar cada fase do plano                          |
| Gabriel Albani          | QA                                       | DevOps                                | Auditar branches, commits, PRs e revisões; rodar no CI integração com PostgreSQL, lint e Sonar; cuidar de Docker, Flyway, proteção de branch e teste de carga (k6)                   |
| Miguel Augusto Guedes   | Desenvolvedor Frontend                   | Desenvolvedor Backend                 | Conferir no backend cada manutenção proposta; levantar duplicações e o contrato de permissões da API; executar as refatorações de backend                                              |
| Vinícius Henrique da Silva | Desenvolvedor Backend                 | Desenvolvedor Frontend                | Analisar o impacto das mudanças de API no frontend; alinhar telas de reserva, cancelamento, perfil e administração às novas regras; remover do frontend as regras duplicadas do backend |
| Murilo Watanabe         | DevOps                                   | Product Owner e Revisor de Requisitos | Conferir a documentação anterior contra a especificação original; avaliar as propostas e validar o aceite de RN01–RN05, RF01–RF05 e RNF01–RNF04; priorizar o backlog de débitos e registrar decisões no acordo de engenharia |

## Por que essa distribuição

- O problema principal é o **desalinhamento entre frontend e backend**. Vinícius conhece o contrato da
  API (foi backend na etapa 1) e agora corrige as telas que consomem essa API. Miguel conhece as telas
  que dependem de cada endpoint e sabe o que o backend precisa expor.
- Nathalia escreveu os critérios de aceite da etapa 1; como QA de Regressão, transforma esses critérios
  em testes e identifica rapidamente o que deixou de funcionar.
- Gabriel conhece as suítes de teste (QA na etapa 1); como DevOps, coloca essas suítes no CI.
- Murilo montou o pipeline e o processo na etapa 1; como PO e Revisor de Requisitos, confere se a
  documentação, as propostas e o processo da equipe anterior respeitaram o acordo de 07/08/2026.
- A rotação evita que alguém audite o próprio trabalho. Foi justamente a falta de visão do frontend
  durante a correção do backend que causou as regressões R1 e R2.

## Matriz RACI das entregas da etapa

R = responsável, A = aprova o aceite, C = consultado, I = informado.

| Entrega                                    | Murilo | Nathalia | Gabriel | Miguel | Vinícius |
| ------------------------------------------ | ------ | -------- | ------- | ------ | -------- |
| Perfis da etapa                            | R/A    | C        | C       | C      | C        |
| Análise da documentação anterior           | R/A    | C        | I       | I      | I        |
| Análise das propostas                      | R/A    | I        | I       | C      | C        |
| Verificação das manutenções                | A      | C        | I       | R      | R        |
| Padrões, duplicação e regressões           | C      | R        | R       | R      | R        |
| Testes dos novos cenários                  | A      | R        | C       | C      | C        |
| Backlog de débitos e plano de refatoração  | R/A    | C        | C       | C      | C        |
| Acordo de engenharia e critérios de aceite | R/A    | C        | C       | I      | I        |
