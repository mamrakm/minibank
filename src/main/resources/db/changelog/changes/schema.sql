-- liquibase formatted sql

-- changeset miroslav.mamrak:1718327092661-1
CREATE SEQUENCE IF NOT EXISTS account_seq START WITH 1 INCREMENT BY 50;

-- changeset miroslav.mamrak:1718327092661-2
CREATE SEQUENCE IF NOT EXISTS customer_seq START WITH 1 INCREMENT BY 50;

-- changeset miroslav.mamrak:1718327092661-3
CREATE TABLE account
(
    account_id  BIGINT NOT NULL,
    balance     BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    CONSTRAINT pk_account PRIMARY KEY (account_id)
);

-- changeset miroslav.mamrak:1718327092661-4
CREATE TABLE customer
(
    customer_id BIGINT       NOT NULL,
    first_name  VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    last_name   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_customer PRIMARY KEY (customer_id)
);

-- changeset miroslav.mamrak:1718327092661-5
ALTER TABLE account
    ADD CONSTRAINT FK_ACCOUNT_ON_CUSTOMER FOREIGN KEY (customer_id) REFERENCES customer (customer_id);

