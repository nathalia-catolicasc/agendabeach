-- Dados mínimos para o teste de carga (perf/availability.js).
-- Só o ADMIN cadastra quadras pela API, então a quadra entra direto no banco.
-- Rode depois que o backend subir (o Flyway precisa ter criado as tabelas):
--   docker compose exec -T db psql -U postgres -d agendabeach -v ON_ERROR_STOP=1 < perf/seed.sql
INSERT INTO courts (name, type, status)
VALUES ('Quadra k6', 'OPEN', 'ACTIVE')
ON CONFLICT (name) DO NOTHING;
