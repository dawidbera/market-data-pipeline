-- TimescaleDB Schema
CREATE TABLE IF NOT EXISTS ticks (
    id BIGSERIAL,
    symbol VARCHAR(10) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    volume BIGINT NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id, timestamp)
);

CREATE TABLE IF NOT EXISTS candles (
    id BIGSERIAL,
    symbol VARCHAR(10) NOT NULL,
    open DOUBLE PRECISION NOT NULL,
    high DOUBLE PRECISION NOT NULL,
    low DOUBLE PRECISION NOT NULL,
    close DOUBLE PRECISION NOT NULL,
    volume BIGINT NOT NULL,
    window_start TIMESTAMPTZ NOT NULL,
    window_end TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id, window_start)
);

CREATE TABLE IF NOT EXISTS alerts (
    id BIGSERIAL,
    symbol VARCHAR(10) NOT NULL,
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id, timestamp)
);

-- Convert to Hypertables
SELECT create_hypertable('ticks', 'timestamp', if_not_exists => TRUE);
SELECT create_hypertable('candles', 'window_start', if_not_exists => TRUE);
SELECT create_hypertable('alerts', 'timestamp', if_not_exists => TRUE);

-- Retention Policies
-- Ticks: 7 days
SELECT add_retention_policy('ticks', INTERVAL '7 days', if_not_exists => TRUE);
-- Candles: 30 days
SELECT add_retention_policy('candles', INTERVAL '30 days', if_not_exists => TRUE);
