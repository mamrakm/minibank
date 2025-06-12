-- Consolidated Minibank Database Schema
-- Creates complete schema for ordinal-based enum storage

-- Create bank schema
CREATE SCHEMA IF NOT EXISTS bank;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Client table
CREATE TABLE IF NOT EXISTS bank.client (
                                           id              BIGSERIAL PRIMARY KEY,
                                           first_name      VARCHAR(255) NOT NULL,
                                           last_name       VARCHAR(255) NOT NULL,
                                           email           VARCHAR(255) NOT NULL UNIQUE,
                                           phone_number    VARCHAR(255),
                                           address         VARCHAR(255),
                                           date_of_birth   DATE,
                                           personal_number UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE
);

-- Account table
CREATE TABLE IF NOT EXISTS bank.account (
                                            id           BIGSERIAL PRIMARY KEY,
                                            account_name VARCHAR(255) NOT NULL,
                                            client_id    BIGINT NOT NULL,
                                            balance      DECIMAL(19, 4) DEFAULT 0.0000 NOT NULL,
                                            account_type INTEGER NOT NULL,
                                            currency     INTEGER NOT NULL,
                                            CONSTRAINT fk_account_client
                                                FOREIGN KEY (client_id) REFERENCES bank.client (id) ON DELETE CASCADE,
                                            CONSTRAINT chk_account_balance_positive
                                                CHECK (balance >= 0),
                                            CONSTRAINT chk_account_balance_limit
                                                CHECK (balance <= 999999999999999.9999),
                                            CONSTRAINT chk_account_type_valid
                                                CHECK (account_type >= 0 AND account_type < 10),
                                            CONSTRAINT chk_currency_valid
                                                CHECK (currency >= 0 AND currency < 10)
);

-- Transaction table
CREATE TABLE IF NOT EXISTS bank.transaction (
                                                id                BIGSERIAL PRIMARY KEY,
                                                source_account_id BIGINT NOT NULL,
                                                target_account_id BIGINT NOT NULL,
                                                amount            DECIMAL(19, 4) NOT NULL,
                                                currency          INTEGER NOT NULL,
                                                timestamp         TIMESTAMP NOT NULL,
                                                status            INTEGER NOT NULL,
                                                reference         VARCHAR(255),
                                                CONSTRAINT fk_transaction_source
                                                    FOREIGN KEY (source_account_id) REFERENCES bank.account (id),
                                                CONSTRAINT fk_transaction_target
                                                    FOREIGN KEY (target_account_id) REFERENCES bank.account (id),
                                                CONSTRAINT chk_different_accounts
                                                    CHECK (source_account_id != target_account_id),
                                                CONSTRAINT chk_transaction_amount_positive
                                                    CHECK (amount > 0),
                                                CONSTRAINT chk_transaction_amount_limit
                                                    CHECK (amount <= 999999999999999.9999),
                                                CONSTRAINT chk_transaction_amount_minimum
                                                    CHECK (amount >= 0.0001),
                                                CONSTRAINT chk_transaction_currency_valid
                                                    CHECK (currency >= 0 AND currency < 10),
                                                CONSTRAINT chk_transaction_status_valid
                                                    CHECK (status >= 0 AND status < 10)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_client_email ON bank.client (email);
CREATE INDEX IF NOT EXISTS idx_client_personal_number ON bank.client (personal_number);
CREATE INDEX IF NOT EXISTS idx_client_date_of_birth ON bank.client (date_of_birth);

CREATE INDEX IF NOT EXISTS idx_account_client_id ON bank.account (client_id);
CREATE INDEX IF NOT EXISTS idx_account_currency ON bank.account (currency);
CREATE INDEX IF NOT EXISTS idx_account_type ON bank.account (account_type);

CREATE INDEX IF NOT EXISTS idx_transaction_source_account ON bank.transaction (source_account_id);
CREATE INDEX IF NOT EXISTS idx_transaction_target_account ON bank.transaction (target_account_id);
CREATE INDEX IF NOT EXISTS idx_transaction_timestamp ON bank.transaction (timestamp);
CREATE INDEX IF NOT EXISTS idx_transaction_currency ON bank.transaction (currency);
CREATE INDEX IF NOT EXISTS idx_transaction_status ON bank.transaction (status);
CREATE INDEX IF NOT EXISTS idx_transaction_amount ON bank.transaction (amount);

-- Comments for documentation
COMMENT ON SCHEMA bank IS 'Banking system schema with ordinal-based enum storage';

COMMENT ON TABLE bank.client IS 'Bank customers with personal information';
COMMENT ON COLUMN bank.client.personal_number IS 'Unique UUID identifier for customer';

COMMENT ON TABLE bank.account IS 'Bank accounts with ordinal-based type and currency storage';
COMMENT ON COLUMN bank.account.balance IS 'Account balance (4 decimal precision, 0 to 999,999,999,999,999.9999)';
COMMENT ON COLUMN bank.account.account_type IS 'Account type ordinal (0=CHECKING, 1=CURRENT, 2=SAVINGS, etc.)';
COMMENT ON COLUMN bank.account.currency IS 'Currency ordinal (0=USD, 1=EUR, 2=GBP)';

COMMENT ON TABLE bank.transaction IS 'Money transfers with ordinal-based currency and status';
COMMENT ON COLUMN bank.transaction.amount IS 'Transaction amount (4 decimal precision, 0.0001 to 999,999,999,999,999.9999)';
COMMENT ON COLUMN bank.transaction.currency IS 'Currency ordinal (0=USD, 1=EUR, 2=GBP)';
COMMENT ON COLUMN bank.transaction.status IS 'Status ordinal (0=PENDING, 1=COMPLETED, 2=FAILED)';
COMMENT ON COLUMN bank.transaction.reference IS 'Transaction description or reference';