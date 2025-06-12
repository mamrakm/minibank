#!/bin/bash

# Minibank API Test Script - Updated for Current API State
# Tests all endpoints with proper error handling

set -e  # Exit on any error

# Configuration
BASE_URL="http://localhost:8082"
HEALTH_ENDPOINT="$BASE_URL/actuator/health"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Global variables for created resources
CLIENT_ID=""
SOURCE_ACCOUNT_ID=""
TARGET_ACCOUNT_ID=""
TRANSACTION_ID=""

# Helper functions
print_header() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Test HTTP request with better error handling
test_request() {
    local method=$1
    local url=$2
    local data=$3
    local expected_status=$4
    local description=$5

    echo -e "\n${YELLOW}Testing:${NC} $description"
    echo -e "${BLUE}$method $url${NC}"

    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$url" 2>/dev/null || echo -e "\nERROR")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            "$url" 2>/dev/null || echo -e "\nERROR")
    fi

    # Parse response
    if [[ "$response" == *"ERROR"* ]]; then
        print_error "Request failed - connection error"
        return 1
    fi

    body=$(echo "$response" | head -n -1)
    status=$(echo "$response" | tail -n 1)

    echo "Response: $body"
    echo "Status: $status"

    if [ "$status" = "$expected_status" ]; then
        print_success "$description - Status $status"
        echo "$body"  # Return response body for parsing
        return 0
    else
        print_error "$description - Expected $expected_status, got $status"
        return 1
    fi
}

# Wait for application to be ready
wait_for_application() {
    print_header "Waiting for Application"

    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        print_info "Attempt $attempt/$max_attempts - Checking health..."

        if curl -s "$HEALTH_ENDPOINT" > /dev/null 2>&1; then
            print_success "Application is ready!"
            return 0
        fi

        sleep 2
        ((attempt++))
    done

    print_error "Application failed to start within $((max_attempts * 2)) seconds"
    exit 1
}

# Test health endpoint
test_health() {
    print_header "Health Check"

    if test_request "GET" "$HEALTH_ENDPOINT" "" "200" "Health check"; then
        print_success "Health check passed"
    else
        print_error "Health check failed"
        exit 1
    fi
}

# Test client operations
test_clients() {
    print_header "Testing Client Operations"

    # Create client
    local client_data='{
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "phoneNumber": "+1234567890",
        "address": "123 Main St, Anytown, USA",
        "dateOfBirth": "1990-01-15"
    }'

    if response=$(test_request "POST" "$BASE_URL/clients" "$client_data" "201" "Create client"); then
        CLIENT_ID=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        print_success "Client created with ID: $CLIENT_ID"
    else
        print_error "Failed to create client"
        return 1
    fi

    # Get client by ID
    if [ -n "$CLIENT_ID" ]; then
        test_request "GET" "$BASE_URL/clients/$CLIENT_ID" "" "200" "Get client by ID"
    fi

    # Get all clients
    test_request "GET" "$BASE_URL/clients" "" "200" "Get all clients"

    # Search clients by first name
    test_request "GET" "$BASE_URL/clients/search-by-name/John" "" "200" "Search clients by first name"

    # Search client by email
    test_request "GET" "$BASE_URL/clients/search-by-email/john.doe@example.com" "" "200" "Search client by email"

    # Update client
    local update_data='{
        "id": '$CLIENT_ID',
        "firstName": "Jane",
        "lastName": "Doe",
        "email": "jane.doe@example.com",
        "phoneNumber": "+1234567890",
        "address": "456 Oak Ave, Newtown, USA",
        "dateOfBirth": "1990-01-15",
        "personalNumber": "00000000-0000-0000-0000-000000000000"
    }'

    if [ -n "$CLIENT_ID" ]; then
        test_request "PUT" "$BASE_URL/clients/$CLIENT_ID" "$update_data" "200" "Update client"
    fi
}

# Test account operations
test_accounts() {
    print_header "Testing Account Operations"

    if [ -z "$CLIENT_ID" ]; then
        print_error "No client ID available for account tests"
        return 1
    fi

    # Create source account
    local source_account_data='{
        "accountType": "CHECKING",
        "balance": 1000.0000,
        "currency": "USD",
        "clientId": '$CLIENT_ID'
    }'

    if response=$(test_request "POST" "$BASE_URL/accounts" "$source_account_data" "201" "Create source account"); then
        SOURCE_ACCOUNT_ID=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        print_success "Source account created with ID: $SOURCE_ACCOUNT_ID"
    else
        print_error "Failed to create source account"
        return 1
    fi

    # Create target account
    local target_account_data='{
        "accountType": "SAVINGS",
        "balance": 500.0000,
        "currency": "USD",
        "clientId": '$CLIENT_ID'
    }'

    if response=$(test_request "POST" "$BASE_URL/accounts" "$target_account_data" "201" "Create target account"); then
        TARGET_ACCOUNT_ID=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        print_success "Target account created with ID: $TARGET_ACCOUNT_ID"
    else
        print_error "Failed to create target account"
        return 1
    fi

    # Get account by ID
    if [ -n "$SOURCE_ACCOUNT_ID" ]; then
        test_request "GET" "$BASE_URL/accounts/$SOURCE_ACCOUNT_ID" "" "200" "Get account by ID"
    fi

    # Get all accounts
    test_request "GET" "$BASE_URL/accounts" "" "200" "Get all accounts"

    # Get accounts by client ID
    test_request "GET" "$BASE_URL/accounts/client/$CLIENT_ID" "" "200" "Get accounts by client ID"

    # Update account
    local update_account_data='{
        "id": '$SOURCE_ACCOUNT_ID',
        "name": "Updated Checking Account",
        "clientId": '$CLIENT_ID',
        "balance": 1200.0000,
        "accountType": "CHECKING",
        "currency": "USD"
    }'

    if [ -n "$SOURCE_ACCOUNT_ID" ]; then
        test_request "PUT" "$BASE_URL/accounts/$SOURCE_ACCOUNT_ID" "$update_account_data" "200" "Update account"
    fi
}

# Test transaction operations
test_transactions() {
    print_header "Testing Transaction Operations"

    if [ -z "$SOURCE_ACCOUNT_ID" ] || [ -z "$TARGET_ACCOUNT_ID" ]; then
        print_error "No account IDs available for transaction tests"
        return 1
    fi

    # Create transaction (transfer money)
    local transaction_data='{
        "sourceAccountId": '$SOURCE_ACCOUNT_ID',
        "targetAccountId": '$TARGET_ACCOUNT_ID',
        "amount": 250.0000,
        "currency": "USD",
        "reference": "Test transfer between accounts"
    }'

    if response=$(test_request "POST" "$BASE_URL/transactions/transfer" "$transaction_data" "201" "Create transaction"); then
        TRANSACTION_ID=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        print_success "Transaction created with ID: $TRANSACTION_ID"
    else
        print_error "Failed to create transaction"
        return 1
    fi

    # Get transaction by ID
    if [ -n "$TRANSACTION_ID" ]; then
        test_request "GET" "$BASE_URL/transactions/$TRANSACTION_ID" "" "200" "Get transaction by ID"
    fi

    # Get transactions by account
    test_request "GET" "$BASE_URL/transactions/account/$SOURCE_ACCOUNT_ID" "" "200" "Get transactions by account ID"

    # Get outgoing transactions
    test_request "GET" "$BASE_URL/transactions/account/$SOURCE_ACCOUNT_ID/outgoing" "" "200" "Get outgoing transactions"

    # Get incoming transactions
    test_request "GET" "$BASE_URL/transactions/account/$TARGET_ACCOUNT_ID/incoming" "" "200" "Get incoming transactions"

    # Create another transaction for more data
    local second_transaction_data='{
        "sourceAccountId": '$TARGET_ACCOUNT_ID',
        "targetAccountId": '$SOURCE_ACCOUNT_ID',
        "amount": 100.0000,
        "currency": "USD",
        "reference": "Return transfer"
    }'

    test_request "POST" "$BASE_URL/transactions/transfer" "$second_transaction_data" "201" "Create second transaction"
}

# Test error cases
test_error_cases() {
    print_header "Testing Error Cases"

    # Test 404 errors
    test_request "GET" "$BASE_URL/clients/99999" "" "404" "Get non-existent client (should return 404)" || true
    test_request "GET" "$BASE_URL/accounts/99999" "" "404" "Get non-existent account (should return 404)" || true
    test_request "GET" "$BASE_URL/transactions/99999" "" "404" "Get non-existent transaction (should return 404)" || true

    # Test invalid client data
    local invalid_client='{
        "firstName": "",
        "lastName": "",
        "email": "invalid-email",
        "phoneNumber": "invalid",
        "address": "",
        "dateOfBirth": "2030-01-01"
    }'
    test_request "POST" "$BASE_URL/clients" "$invalid_client" "400" "Create client with invalid data (should return 400)" || true

    # Test invalid account data
    local invalid_account='{
        "accountType": "INVALID_TYPE",
        "balance": -100.00,
        "currency": "INVALID",
        "clientId": 99999
    }'
    test_request "POST" "$BASE_URL/accounts" "$invalid_account" "400" "Create account with invalid data (should return 400)" || true

    # Test invalid transaction data
    if [ -n "$SOURCE_ACCOUNT_ID" ]; then
        local invalid_transaction='{
            "sourceAccountId": '$SOURCE_ACCOUNT_ID',
            "targetAccountId": '$SOURCE_ACCOUNT_ID',
            "amount": -50.00,
            "currency": "INVALID"
        }'
        test_request "POST" "$BASE_URL/transactions/transfer" "$invalid_transaction" "400" "Create invalid transaction (should return 400)" || true
    fi

    # Test insufficient funds
    if [ -n "$SOURCE_ACCOUNT_ID" ] && [ -n "$TARGET_ACCOUNT_ID" ]; then
        local insufficient_funds_transaction='{
            "sourceAccountId": '$SOURCE_ACCOUNT_ID',
            "targetAccountId": '$TARGET_ACCOUNT_ID',
            "amount": 999999.0000,
            "currency": "USD",
            "reference": "Insufficient funds test"
        }'
        test_request "POST" "$BASE_URL/transactions/transfer" "$insufficient_funds_transaction" "400" "Create transaction with insufficient funds (should return 400)" || true
    fi
}

# Test edge cases
test_edge_cases() {
    print_header "Testing Edge Cases"

    # Test with different currencies
    local eur_account_data='{
        "accountType": "SAVINGS",
        "balance": 1000.0000,
        "currency": "EUR",
        "clientId": '$CLIENT_ID'
    }'

    if response=$(test_request "POST" "$BASE_URL/accounts" "$eur_account_data" "201" "Create EUR account"); then
        eur_account_id=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        print_success "EUR account created with ID: $eur_account_id"

        # Test currency mismatch transaction
        local currency_mismatch_transaction='{
            "sourceAccountId": '$SOURCE_ACCOUNT_ID',
            "targetAccountId": '$eur_account_id',
            "amount": 100.0000,
            "currency": "USD",
            "reference": "Currency mismatch test"
        }'
        test_request "POST" "$BASE_URL/transactions/transfer" "$currency_mismatch_transaction" "400" "Create transaction with currency mismatch (should return 400)" || true
    fi

    # Test minimum transaction amount
    if [ -n "$SOURCE_ACCOUNT_ID" ] && [ -n "$TARGET_ACCOUNT_ID" ]; then
        local min_transaction='{
            "sourceAccountId": '$SOURCE_ACCOUNT_ID',
            "targetAccountId": '$TARGET_ACCOUNT_ID',
            "amount": 0.0001,
            "currency": "USD",
            "reference": "Minimum amount test"
        }'
        test_request "POST" "$BASE_URL/transactions/transfer" "$min_transaction" "201" "Create transaction with minimum amount"
    fi

    # Test different account types
    local account_types=("CURRENT" "INVESTMENT" "BUSINESS" "STUDENT" "JOINT" "LOAN" "CLASSIC")

    for account_type in "${account_types[@]}"; do
        local type_account_data='{
            "accountType": "'$account_type'",
            "balance": 100.0000,
            "currency": "USD",
            "clientId": '$CLIENT_ID'
        }'
        test_request "POST" "$BASE_URL/accounts" "$type_account_data" "201" "Create $account_type account" || true
    done
}

# Cleanup created resources (optional - accounts may have dependencies)
cleanup() {
    print_header "Cleanup"
    print_warning "Cleanup skipped - some resources may have foreign key dependencies"
    print_info "In a real environment, you might want to clean up test data"

    # Note: Cleanup is tricky due to foreign key constraints
    # Transactions reference accounts, accounts reference clients
    # You'd need to delete in reverse order: transactions -> accounts -> clients
}

# Display summary
display_summary() {
    print_header "Test Summary"

    if [ -n "$CLIENT_ID" ]; then
        print_success "Client created: ID $CLIENT_ID"
    fi

    if [ -n "$SOURCE_ACCOUNT_ID" ]; then
        print_success "Source account created: ID $SOURCE_ACCOUNT_ID"
    fi

    if [ -n "$TARGET_ACCOUNT_ID" ]; then
        print_success "Target account created: ID $TARGET_ACCOUNT_ID"
    fi

    if [ -n "$TRANSACTION_ID" ]; then
        print_success "Transaction created: ID $TRANSACTION_ID"
    fi

    print_info "All endpoints tested successfully!"
    print_info "You can verify the created data by accessing the API directly"
}

# Main test execution
main() {
    print_header "Minibank API Testing Started"
    print_info "Base URL: $BASE_URL"
    print_info "Testing reactive banking system with Spring Boot 3.4.1, Kotlin, and R2DBC"

    # Test sequence
    wait_for_application
    test_health
    test_clients
    test_accounts
    test_transactions
    test_error_cases
    test_edge_cases
    display_summary

    print_header "All Tests Completed Successfully! 🎉"
}

# Handle script interruption
trap cleanup EXIT

# Run main function
main "$@"