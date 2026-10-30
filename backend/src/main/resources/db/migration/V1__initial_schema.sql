CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TYPE court_type AS ENUM (
    'COVERED',
    'OPEN'
);

CREATE TYPE court_status AS ENUM (
    'ACTIVE',
    'INACTIVE'
);

CREATE TYPE booking_status AS ENUM (
    'SCHEDULED',
    'CANCELLED',
    'COMPLETED'
);

CREATE TYPE profile_type AS ENUM (
    'CLIENT',
    'ADMIN'
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    profile profile_type NOT NULL DEFAULT 'CLIENT'
);

CREATE TABLE courts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    type court_type NOT NULL,
    status court_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    court_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    court_price NUMERIC(10, 2) NOT NULL
        CHECK (court_price >= 0),
    status booking_status NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,

    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_booking_court
        FOREIGN KEY (court_id)
        REFERENCES courts(id),

    CONSTRAINT ck_booking_time
        CHECK (end_time > start_time)
);

ALTER TABLE bookings
    ADD COLUMN booking_period TSRANGE
    GENERATED ALWAYS AS (
        tsrange(start_time, end_time, '[)')
    ) STORED;

ALTER TABLE bookings
    ADD CONSTRAINT uq_booking_court_period
    EXCLUDE USING GIST (
        court_id WITH =,
        booking_period WITH &&
    )
    WHERE (status = 'SCHEDULED');

CREATE INDEX idx_bookings_user
    ON bookings(user_id);

CREATE INDEX idx_bookings_court
    ON bookings(court_id);

CREATE INDEX idx_bookings_start_time
    ON bookings(start_time);

CREATE INDEX idx_bookings_status
    ON bookings(status);