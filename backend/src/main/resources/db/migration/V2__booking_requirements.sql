ALTER TABLE users ADD COLUMN cpf VARCHAR(11) UNIQUE;
ALTER TABLE users ADD COLUMN credit_balance NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (credit_balance >= 0);
ALTER TABLE bookings ADD COLUMN credit_used NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (credit_used >= 0 AND credit_used <= court_price);
CREATE TABLE pricing_policy (
 id BIGINT PRIMARY KEY CHECK (id = 1),
 regular_price NUMERIC(10,2) NOT NULL CHECK (regular_price >= 0),
 peak_price NUMERIC(10,2) NOT NULL CHECK (peak_price >= 0)
);
INSERT INTO pricing_policy VALUES (1, 60.00, 100.00);
CREATE TABLE holidays (date DATE PRIMARY KEY, description VARCHAR(150) NOT NULL);
CREATE TABLE auth_tokens (
 token_hash VARCHAR(64) PRIMARY KEY,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 expires_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_auth_tokens_expiry ON auth_tokens(expires_at);
CREATE INDEX idx_booking_future_user ON bookings(user_id, start_time) WHERE status = 'SCHEDULED';
ALTER TABLE bookings ADD COLUMN refund_amount NUMERIC(10,2);
ALTER TABLE bookings ADD COLUMN cancellation_credit NUMERIC(10,2);
