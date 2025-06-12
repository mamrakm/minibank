-- Simple database initialization script for bank schema
-- Creates only tables with proper columns

-- Create bank schema
CREATE SCHEMA IF NOT EXISTS bank;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Client table
CREATE TABLE bank.client
(
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(255)                   NOT NULL,
    last_name       VARCHAR(255)                   NOT NULL,
    email           VARCHAR(255)                   NOT NULL UNIQUE,
    phone_number    VARCHAR(255),
    address         VARCHAR(255),
    date_of_birth   DATE,
    personal_number UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE
);

-- Account table
CREATE TABLE bank.account
(
    id           BIGSERIAL PRIMARY KEY,
    account_name VARCHAR(255)                  NOT NULL,
    client_id    BIGINT                        NOT NULL,
    balance      DECIMAL(19, 4) DEFAULT 0.0000 NOT NULL,
    account_type INTEGER                       NOT NULL,
    currency     INTEGER                       NOT NULL,
    CONSTRAINT fk_account_client
        FOREIGN KEY (client_id) REFERENCES bank.client (id) ON DELETE CASCADE
);

-- Transaction table - Fixed currency column to be INTEGER for ordinal storage
CREATE TABLE bank.transaction
(
    id                BIGSERIAL PRIMARY KEY,
    source_account_id BIGINT         NOT NULL,
    target_account_id BIGINT         NOT NULL,
    amount            DECIMAL(19, 4) NOT NULL CHECK (amount > 0),
    currency          INTEGER        NOT NULL, -- Changed from VARCHAR(3) to INTEGER for ordinal
    timestamp         TIMESTAMP      NOT NULL,
    status            INTEGER        NOT NULL,
    reference         VARCHAR(255),
    CONSTRAINT fk_transaction_source
        FOREIGN KEY (source_account_id) REFERENCES bank.account (id),
    CONSTRAINT fk_transaction_target
        FOREIGN KEY (target_account_id) REFERENCES bank.account (id),
    CONSTRAINT chk_different_accounts
        CHECK (source_account_id != target_account_id)
);