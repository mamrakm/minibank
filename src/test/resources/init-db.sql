-- Ensure the "bank" schema exists
CREATE SCHEMA IF NOT EXISTS bank;

-- ===========================================
-- Create "client" table with an auto-incrementing ID
-- ===========================================
CREATE SEQUENCE IF NOT EXISTS bank.client_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS bank.client
(
    id         BIGINT PRIMARY KEY DEFAULT nextval('bank.client_id_seq'),
    first_name VARCHAR(255)        NOT NULL,
    last_name  VARCHAR(255)        NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    phone      VARCHAR(255),
    address    VARCHAR(255)
);

-- ===========================================
-- Create "account" table with an auto-incrementing ID
-- ===========================================
CREATE SEQUENCE IF NOT EXISTS bank.account_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS bank.account
(
    id           BIGINT PRIMARY KEY DEFAULT nextval('bank.account_id_seq'),
    account_name VARCHAR(255) NOT NULL,
    client_id    BIGINT       NOT NULL,
    balance      DECIMAL      NOT NULL,
    account_type SMALLINT     NOT NULL,
    CONSTRAINT fk_account_client FOREIGN KEY (client_id) REFERENCES bank.client (id) ON DELETE CASCADE
);

-- ===========================================
-- Create "transaction" table with an auto-incrementing ID
-- ===========================================
CREATE SEQUENCE IF NOT EXISTS bank.transaction_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS bank.transaction
(
    id                BIGINT PRIMARY KEY DEFAULT nextval('bank.transaction_id_seq'),
    source_account_id BIGINT          NOT NULL,
    target_account_id BIGINT          NOT NULL,
    amount            DECIMAL(19, 2)   NOT NULL,
    timestamp         TIMESTAMP        NOT NULL,
    status            VARCHAR(20)      NOT NULL,
    reference         VARCHAR(255),
    CONSTRAINT fk_transaction_source_account FOREIGN KEY (source_account_id) REFERENCES bank.account (id),
    CONSTRAINT fk_transaction_target_account FOREIGN KEY (target_account_id) REFERENCES bank.account (id)
);

-- ===========================================
-- Ensure indexes for faster lookup
-- ===========================================
CREATE INDEX IF NOT EXISTS idx_client_email ON bank.client (email);
CREATE INDEX IF NOT EXISTS idx_account_client_id ON bank.account (client_id);
CREATE INDEX IF NOT EXISTS idx_transaction_source_account ON bank.transaction (source_account_id);
CREATE INDEX IF NOT EXISTS idx_transaction_target_account ON bank.transaction (target_account_id);
CREATE INDEX IF NOT EXISTS idx_transaction_timestamp ON bank.transaction (timestamp);