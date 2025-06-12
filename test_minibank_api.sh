#!/bin/bash

# Minibank API Test Script
# Tests all endpoints with proper error handling

set -e  # Exit on any error (remove this if you want tests to continue on failure)

# Configuration
BASE_URL="http://localhost:8082"
HEALTH_ENDPOINT="$BASE_URL/actuator/health"
API_BASE="$BASE_URL/api/v1"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Global variables for created resources
CLIENT_ID=""
ACCOUNT_ID=""
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

    if response=$(test_request "POST" "$API_BASE/clients" "$client_data" "201" "Create client"); then
        CLIENT_ID=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        print_success "Client created with ID: $CLIENT_ID"
    else
        print_error "Failed to create client"
        return 1
    fi

    # Get client by ID
    if [ -n "$CLIENT_ID" ]; then
        test_request "GET" "$API_BASE/clients/$CLIENT_ID" "" "200" "Get client by ID"
    fi

    # Get all clients
    test_request "GET" "$API_BASE/clients" "" "200" "Get all clients"

    # Update client
    local update_data='{
        "firstName": "Jane",
        "lastName": "Doe",
        "email": "jane.doe@example.com",
        "phoneNumber": "+1234567890",
        "address": "456 Oak Ave, Newtown, USA",
        "dateOfBirth": "1990-01-15"
    }'

    if [ -n "$CLIENT_ID" ]; then
        test_request "PUT" "$API_BASE/clients/$CLIENT_ID" "$update_data" "200" "Update client"
    fi
}

# Test account operations
test_accounts() {
    print_header "Testing Account Operations"

    if [ -z "$CLIENT_ID" ]; then
        print_error "No client ID available for account tests"
        return 1
    fi

    # Create account
    local account_data='{
        "accountName": "Main Checking Account",
        "clientId": '$CLIENT_ID',
        "balance": 1000.50,
        "accountType": "CHECKING"
    }'

    if response=$(test_request "POST" "$API_BASE/accounts" "$account_data" "201" "Create account"); then
        ACCOUNT_ID=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        print_success "Account created with ID: $ACCOUNT_ID"
    else
        print_error "Failed to create account"
        return 1
    fi

    # Get account by ID
    if [ -n "$ACCOUNT_ID" ]; then
        test_request "GET" "$API_BASE/accounts/$ACCOUNT_ID" "" "200" "Get account by ID"
    fi

    # Get all accounts
    test_request "GET" "$API_BASE/accounts" "" "200" "Get all accounts"

    # Get accounts by client ID
    test_request "GET" "$API_BASE/accounts/client/$CLIENT_ID" "" "200" "Get accounts by client ID"

    # Update account
    local update_account_data='{
        "accountName": "Updated Checking Account",
        "clientId": '$CLIENT_ID',
        "balance": 2000.75,
        "accountType": "SAVINGS"
    }'

    if [ -n "$ACCOUNT_ID" ]; then
        test_request "PUT" "$API_BASE/accounts/$ACCOUNT_ID" "$update_account_data" "200" "Update account"
    fi
}

# Test transaction operations
test_transactions() {
    print_header "Testing Transaction Operations"

    if [ -z "$ACCOUNT_ID" ]; then
        print_error "No account ID available for transaction tests"
        return 1
    fi

    # Create a second account for transfers
    local second_account_data='{
        "accountName": "Savings Account",
        "clientId": '$CLIENT_ID',
        "balance": 500.00,
        "accountType": "SAVINGS"
    }'

    local second_account_id=""
    if response=$(test_request "POST" "$API_BASE/accounts" "$second_account_data" "201" "Create second account for transfers"); then
        second_account_id=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        print_success "Second account created with ID: $second_account_id"
    fi

    # Create transaction
    local transaction_data='{
        "sourceAccountId": '$ACCOUNT_ID',
        "targetAccountId": '$second_account_id',
        "amount": 250.00,
        "currency": "USD"
    }'

    if response=$(test_request "POST" "$API_BASE/transactions" "$transaction_data" "201" "Create transaction"); then
        TRANSACTION_ID=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        print_success "Transaction created with ID: $TRANSACTION_ID"
    else
        print_error "Failed to create transaction"
    fi

    # Get transaction by ID
    if [ -n "$TRANSACTION_ID" ]; then
        test_request "GET" "$API_BASE/transactions/$TRANSACTION_ID" "" "200" "Get transaction by ID"
    fi

    # Get all transactions
    test_request "GET" "$API_BASE/transactions" "" "200" "Get all transactions"

    # Get transactions by account
    test_request "GET" "$API_BASE/transactions/account/$ACCOUNT_ID" "" "200" "Get transactions by account ID"
}

# Test error cases
test_error_cases() {
    print_header "Testing Error Cases"

    # Test 404 errors
    test_request "GET" "$API_BASE/clients/99999" "" "404" "Get non-existent client (should return 404)" || true
    test_request "GET" "$API_BASE/accounts/99999" "" "404" "Get non-existent account (should return 404)" || true
    test_request "GET" "$API_BASE/transactions/99999" "" "404" "Get non-existent transaction (should return 404)" || true

    # Test invalid data
    local invalid_client='{
        "firstName": "",
        "email": "invalid-email"
    }'
    test_request "POST" "$API_BASE/clients" "$invalid_client" "400" "Create client with invalid data (should return 400)" || true
}

# Cleanup created resources
cleanup() {
    print_header "Cleanup"

    # Delete transaction (if created)
    if [ -n "$TRANSACTION_ID" ]; then
        test_request "DELETE" "$API_BASE/transactions/$TRANSACTION_ID" "" "204" "Delete transaction" || true
    fi

    # Delete account (if created)
    if [ -n "$ACCOUNT_ID" ]; then
        test_request "DELETE" "$API_BASE/accounts/$ACCOUNT_ID" "" "204" "Delete account" || true
    fi

    # Delete client (if created)
    if [ -n "$CLIENT_ID" ]; then
        test_request "DELETE" "$API_BASE/clients/$CLIENT_ID" "" "204" "Delete client" || true
    fi
}

# Main test execution
main() {
    print_header "Minibank API Testing Started"
    print_info "Base URL: $BASE_URL"
    print_info "API Base: $API_BASE"

    # Test sequence
    wait_for_application
    test_health
    test_clients
    test_accounts
    test_transactions
    test_error_cases
    cleanup

    print_header "All Tests Completed Successfully! 🎉"
}

# Handle script interruption
trap cleanup EXIT

# Run main function
main "$@"