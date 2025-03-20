-- liquibase formatted sql

-- changeset miroslav.mamrak:1739256123000-1
CREATE SEQUENCE IF NOT EXISTS bank.transaction_id_seq START WITH 1 INCREMENT BY 1;

-- changeset miroslav.mamrak:1739256123000-2
CREATE TABLE IF NOT EXISTS bank.transaction (
                                                id BIGINT NOT NULL DEFAULT nextval('bank.transaction_id_seq'),
                                                source_account_id BIGINT NOT NULL,
                                                target_account_id BIGINT NOT NULL,
                                                amount DECIMAL(19, 2) NOT NULL,
                                                currency VARCHAR(3) NOT NULL,
                                                timestamp TIMESTAMP NOT NULL,
                                                status VARCHAR(20) NOT NULL,
                                                reference VARCHAR(255),
                                                CONSTRAINT pk_transaction PRIMARY KEY (id),
                                                CONSTRAINT fk_transaction_source_account FOREIGN KEY (source_account_id) REFERENCES bank.account (id),
                                                CONSTRAINT fk_transaction_target_account FOREIGN KEY (target_account_id) REFERENCES bank.account (id)
);

-- changeset miroslav.mamrak:1739256123000-3
CREATE INDEX IF NOT EXISTS idx_transaction_source_account ON bank.transaction (source_account_id);
CREATE INDEX IF NOT EXISTS idx_transaction_target_account ON bank.transaction (target_account_id);
CREATE INDEX IF NOT EXISTS idx_transaction_timestamp ON bank.transaction (timestamp);
CREATE INDEX IF NOT EXISTS idx_transaction_currency ON bank.transaction (currency);

-- changeset miroslav.mamrak:1739256123000-4
COMMENT ON TABLE bank.transaction IS 'Stores transaction records for money transfers between accounts';
COMMENT ON COLUMN bank.transaction.id IS 'Unique identifier for the transaction';
COMMENT ON COLUMN bank.transaction.source_account_id IS 'ID of the account from which money is transferred';
COMMENT ON COLUMN bank.transaction.target_account_id IS 'ID of the account to which money is transferred';
COMMENT ON COLUMN bank.transaction.amount IS 'Amount of money transferred';
COMMENT ON COLUMN bank.transaction.currency IS 'Currency of the transaction';
COMMENT ON COLUMN bank.transaction.timestamp IS 'Date and time when the transaction occurred';
COMMENT ON COLUMN bank.transaction.status IS 'Status of the transaction (PENDING, COMPLETED, FAILED)';
COMMENT ON COLUMN bank.transaction.reference IS 'Reference or description for the transaction';