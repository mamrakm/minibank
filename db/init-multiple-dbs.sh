#!/bin/bash
set -e

# Create multiple databases
function create_database() {
    local database=$1
    echo "Creating database '$database'"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        CREATE DATABASE "$database";
EOSQL
}

# Create schemas in specific databases
function create_schema() {
    local database=$1
    local schema=$2
    echo "Creating schema '$schema' in database '$database'"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$database" <<-EOSQL
        CREATE SCHEMA IF NOT EXISTS "$schema";
EOSQL
}

# Create databases
if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
    echo "Creating multiple databases: $POSTGRES_MULTIPLE_DATABASES"
    for db in $(echo $POSTGRES_MULTIPLE_DATABASES | tr ',' ' '); do
        create_database "$db"
    done
    
    # Create specific schemas
    create_schema "minibank-database" "bank"
    create_schema "keycloak-database" "keycloak"
    
    echo "All databases and schemas created successfully!"
fi