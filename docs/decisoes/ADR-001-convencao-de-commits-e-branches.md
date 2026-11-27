# ADR-001 – Convenção de commits e de branches

| Campo        | Valor                                                       |
| ------------ | ----------------------------------------------------------- |
| Status       | Aceito pelo PO — ratificação da equipe na revisão do PR     |
| Data         | 27/11/2026                                                  |
| Decisor      | Murilo Watanabe (PO e Revisor de Requisitos)                |
| Ratificam    | Gabriel Albani (DevOps), Nathalia Berri (QA), Miguel Augusto Guedes, Vinícius Henrique da Silva |
| Substitui    | Item 6.2 do Acordo de Manutenibilidade de 07/08/2026        |
| Débito ligado | D9                                                         |

## Contexto

O acordo de 07/08/2026 definiu quatro prefixos de commit: `feat`, `bug`, `refactor` e `docs`, e branches
no formato `feat/<n-tarefa>/<nome>` ou `fix/<n-tarefa>/<descrição>`.

No ciclo de 25/09 a 30/10/2026, a equipe anterior passou a usar **Conventional Commits** (`fix`, `chore`,
`test`, `ci`…) e reescreveu o README nesse sentido, sem registrar a decisão nem pedir aprovação. Ao
mesmo tempo, nenhuma branch do ciclo trouxe número de tarefa (`feat/backend-requisitos-revisados`,
`fix/registration-auth`, `Fix/services-tests`) e o commit `d6f522d` misturou cinco mudanças em 49
arquivos.

O problema real não foi a troca de prefixos, e sim a **perda de rastreabilidade**: não há como ligar
uma mudança à tarefa que a pediu.

## Decisão

1. **Commits seguem Conventional Commits 1.0**, formalizando o que já está no histórico:

   ```
   <tipo>(<escopo opcional>): <descrição no imperativo, minúscula, sem ponto final>

   <corpo opcional: o porquê da mudança>

   Refs #<n-issue>
   ```

   | Tipo       | Uso                                                   | Equivalente no acordo de 07/08 |
   | ---------- | ----------------------------------------------------- | ------------------------------ |
   | `feat`     | Nova funcionalidade                                   | `feat`                         |
   | `fix`      | Correção de bug                                       | `bug`                          |
   | `refactor` | Manutenção preventiva sem mudança de comportamento    | `refactor`                     |
   | `docs`     | Documentação                                          | `docs`                         |
   | `test`     | Criação ou ajuste de testes                           | —                              |
   | `chore`    | Dependências, configurações, templates                | —                              |
   | `ci`       | Pipeline de CI/CD                                     | —                              |
   | `build`    | Docker, Maven, npm                                    | —                              |
   | `style`    | Formatação sem mudança de lógica                      | —                              |

   O prefixo `bug` deixa de ser usado; `fix` o substitui.

2. **Branches mantêm o número da tarefa**, que passa a ser obrigatório para todos os tipos:

   ```
   <tipo>/<n-issue>/<descricao-em-kebab-case>
   ```

   Exemplos: `fix/31/grade-usa-availability`, `docs/40/criterios-de-aceite`, `refactor/45/unifica-regra-de-pico`.
   Tudo em minúsculas (`Fix/...` não é aceito).

3. **Um commit = uma mudança lógica.** Um commit que precisa de "e" na descrição deve ser dividido.
   Mudanças de regra de negócio devem vir no mesmo PR que o ajuste da tela que a exibe.

4. **Todo PR precisa de pelo menos 1 aprovação de outra pessoa** e CI verde antes do merge. O autor não
   mescla o próprio PR sem aprovação registrada. A proteção de branch em `main` e `dev` torna isso
   obrigatório (Fase 3 do plano, responsável: Gabriel).

## Consequências

- **Positivas:** o histórico existente continua válido; a convenção é conhecida pelo mercado e por
  ferramentas (changelog, *semantic release*); o número da Issue na branch e no rodapé restaura a
  rastreabilidade exigida pelo acordo.
- **Negativas:** mais tipos para lembrar; exige que toda tarefa nasça como Issue antes da branch.
- **Ações:** README atualizado; [Acordo de Engenharia v2](../acordo-de-engenharia.md) atualizado; modelo de
  Pull Request com checklist desta decisão em `.github/PULL_REQUEST_TEMPLATE.md`.

## Alternativas consideradas

| Alternativa                                         | Motivo da recusa                                                      |
| --------------------------------------------------- | --------------------------------------------------------------------- |
| Voltar aos quatro prefixos do acordo de 07/08       | Obriga a reescrever o README e não cobre testes, CI e configuração    |
| Aceitar Conventional Commits sem número de tarefa   | Mantém o problema de rastreabilidade que motivou o D9                 |
