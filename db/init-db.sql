-- Create schema 'bank' if it doesn't already exist
CREATE SCHEMA IF NOT EXISTS bank;

-- Create the 'client' table to store client details
CREATE TABLE bank.client (
                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- Unique ID for each client
                             first_name VARCHAR(255) NOT NULL,  -- First name of the client
                             last_name VARCHAR(255) NOT NULL,   -- Last name of the client
                             email VARCHAR(255) UNIQUE NOT NULL, -- Email must be unique and not null
                             phone VARCHAR(255) NOT NULL,       -- Phone number of the client
                             address VARCHAR(255) NOT NULL      -- Address of the client
);

-- Create the 'account' table to store client accounts
CREATE TABLE bank.account (
                              account_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- Unique ID for each account
                              account_name VARCHAR(255) NOT NULL, -- Name of the account
                              client_id BIGINT NOT NULL,          -- Foreign key linking account to a client
                              balance DECIMAL(19,2) NOT NULL,     -- Account balance (stores up to 19 digits with 2 decimal places)
                              account_type SMALLINT NOT NULL,     -- Account type (e.g., savings, checking)

    -- Foreign key constraint to enforce relationship with 'client' table
                              CONSTRAINT fk_account_on_client FOREIGN KEY (client_id)
                                  REFERENCES bank.client (id) ON DELETE CASCADE
);
