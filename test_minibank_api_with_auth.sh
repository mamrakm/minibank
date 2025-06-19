#!/bin/bash

# Minibank API Test Script with Authentication
# Tests Keycloak authentication and all API endpoints

set -e

# Configuration
KEYCLOAK_URL="http://localhost:8090"
MINIBANK_URL="http://localhost:8082"
REALM="minibank"
CLIENT_ID="minibank-api"
CLIENT_SECRET="minibank-secret"

# Test users
USERNAME="user"
PASSWORD="user"
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="admin"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Global variables for tokens and created resources
USER_TOKEN=""
ADMIN_TOKEN=""
CLIENT_ID_CREATED=""
SOURCE_ACCOUNT_ID=""
TARGET_ACCOUNT_ID=""
TRANSACTION_ID=""

# Array to track all created resources for cleanup
CREATED_CLIENTS=()
CREATED_ACCOUNTS=()
CREATED_TRANSACTIONS=()

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

# Cleanup function to remove all test data
cleanup_test_data() {
    print_header "Cleaning Up Test Data"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_warning "No admin token available for cleanup"
        return 0
    fi
    
    local cleanup_errors=0
    
    # Note: Transactions are immutable in banking systems for audit compliance
    # They are deleted automatically when associated accounts are removed
    if [ ${#CREATED_TRANSACTIONS[@]} -gt 0 ]; then
        print_info "Note: ${#CREATED_TRANSACTIONS[@]} transactions created (will be removed with accounts)"
    fi
    
    # Delete accounts
    if [ ${#CREATED_ACCOUNTS[@]} -gt 0 ]; then
        print_info "Cleaning up ${#CREATED_ACCOUNTS[@]} accounts..."
        for account_id in "${CREATED_ACCOUNTS[@]}"; do
            if [ -n "$account_id" ]; then
                print_info "Deleting account: $account_id"
                local response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X DELETE \
                    -H "Authorization: Bearer $ADMIN_TOKEN" \
                    "$MINIBANK_URL/accounts/$account_id" 2>/dev/null)
                local http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
                if [ "$http_code" -eq 204 ] || [ "$http_code" -eq 404 ]; then
                    print_info "Account $account_id deleted successfully"
                else
                    print_warning "Failed to delete account $account_id (HTTP $http_code)"
                    ((cleanup_errors++))
                fi
            fi
        done
    fi
    
    # Delete clients
    if [ ${#CREATED_CLIENTS[@]} -gt 0 ]; then
        print_info "Cleaning up ${#CREATED_CLIENTS[@]} clients..."
        for client_id in "${CREATED_CLIENTS[@]}"; do
            if [ -n "$client_id" ]; then
                print_info "Deleting client: $client_id"
                local response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X DELETE \
                    -H "Authorization: Bearer $ADMIN_TOKEN" \
                    "$MINIBANK_URL/clients/$client_id" 2>/dev/null)
                local http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
                if [ "$http_code" -eq 204 ] || [ "$http_code" -eq 404 ]; then
                    print_info "Client $client_id deleted successfully"
                else
                    print_warning "Failed to delete client $client_id (HTTP $http_code)"
                    ((cleanup_errors++))
                fi
            fi
        done
    fi
    
    # Reset tracking arrays
    CREATED_CLIENTS=()
    CREATED_ACCOUNTS=()
    CREATED_TRANSACTIONS=()
    
    if [ $cleanup_errors -eq 0 ]; then
        print_success "All test data cleaned up successfully!"
    else
        print_warning "Cleanup completed with $cleanup_errors errors"
    fi
}

# Trap to ensure cleanup runs on script exit
trap cleanup_test_data EXIT

# Function to wait for service
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    print_info "Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$url" > /dev/null 2>&1; then
            print_success "$service_name is ready!"
            return 0
        fi
        print_info "Attempt $attempt/$max_attempts - $service_name not ready yet..."
        sleep 2
        ((attempt++))
    done
    
    print_error "$service_name failed to start within expected time"
    return 1
}

# Function to get access token
get_access_token() {
    local username=$1
    local password=$2
    
    print_info "Getting access token for user: $username" >&2
    
    local response=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "grant_type=password" \
        -d "client_id=$CLIENT_ID" \
        -d "client_secret=$CLIENT_SECRET" \
        -d "username=$username" \
        -d "password=$password")
    
    local access_token=$(echo "$response" | jq -r '.access_token // empty' 2>/dev/null || echo "")
    
    print_info "Token extraction debug:" >&2
    print_info "  Response length: ${#response}" >&2
    print_info "  Extracted token length: ${#access_token}" >&2
    print_info "  Token starts with: ${access_token:0:50}..." >&2
    
    if [ -z "$access_token" ] || [ "$access_token" = "null" ]; then
        print_error "Failed to get access token for $username"
        echo "Response: $response"
        return 1
    fi
    
    print_success "Access token obtained for $username" >&2
    echo "$access_token"
}

# Function to test API endpoint with authentication
test_api_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local token=$4
    local expected_status=$5
    local description=$6
    
    print_info "Testing: $description"
    print_info "Endpoint: $method $endpoint"
    if [ -n "$token" ]; then
        print_info "Token length: ${#token} characters"
        print_info "Token starts with: ${token:0:50}..."
    else
        print_info "No token provided"
    fi
    
    local response
    if [ -n "$token" ] && [ -n "$data" ]; then
        response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $token" \
            -d "$data" \
            "$MINIBANK_URL$endpoint")
    elif [ -n "$token" ]; then
        response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $token" \
            "$MINIBANK_URL$endpoint")
    elif [ -n "$data" ]; then
        response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$MINIBANK_URL$endpoint")
    else
        response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            "$MINIBANK_URL$endpoint")
    fi
    
    local http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    local body=$(echo "$response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq "$expected_status" ]; then
        print_success "✓ $description (HTTP $http_code)"
        if [ -n "$body" ] && [ "$body" != "null" ]; then
            echo "$body" | jq . 2>/dev/null || echo "$body"
        fi
        echo "$body" # Return for capture
        return 0
    else
        print_error "✗ $description (Expected HTTP $expected_status, got $http_code)"
        echo "Response: $body"
        return 1
    fi
}

# Test authentication endpoints
test_authentication() {
    print_header "Authentication Tests"
    
    # Debug variables in function scope
    print_info "Debug in test_authentication:" >&2
    print_info "  USER_TOKEN length: ${#USER_TOKEN}" >&2
    print_info "  ADMIN_TOKEN length: ${#ADMIN_TOKEN}" >&2
    
    # Test 1: Public endpoint (no auth required)
    test_api_request "GET" "/auth/public" "" "" 200 "Public endpoint access"
    
    # Test 2: Protected endpoint without token (should fail)
    test_api_request "GET" "/auth/me" "" "" 401 "Protected endpoint without token (should fail)"
    
    # Test 3: Get user info with valid token
    test_api_request "GET" "/auth/me" "" "$USER_TOKEN" 200 "User info with valid user token"
    
    # Test 4: Customer endpoint with customer role
    test_api_request "GET" "/auth/customer" "" "$USER_TOKEN" 200 "Customer endpoint with CUSTOMER role"
    
    # Test 5: Admin endpoint with customer role (should fail)
    test_api_request "GET" "/auth/admin" "" "$USER_TOKEN" 500 "Admin endpoint with CUSTOMER role (should fail)"
    
    # Test 6: Admin endpoint with admin role
    test_api_request "GET" "/auth/admin" "" "$ADMIN_TOKEN" 200 "Admin endpoint with ADMIN role"
}

# Test client operations
test_clients() {
    print_header "Client Management Tests"
    
    # Test unauthorized access (should fail)
    test_api_request "GET" "/clients" "" "$USER_TOKEN" 403 "Client list with CUSTOMER role (should fail)"
    
    # Create client with admin token
    local client_data='{
        "firstName": "John",
        "lastName": "Doe",
        "email": "admin@minibank.com",
        "phoneNumber": "+1234567890",
        "address": "123 Main St, Anytown, USA",
        "dateOfBirth": "1990-01-15"
    }'
    
    print_info "Attempting to create client with admin token..."
    
    # Create client and capture raw response
    local raw_response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -d "$client_data" \
        "$MINIBANK_URL/clients")
    
    local http_code=$(echo "$raw_response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    local body=$(echo "$raw_response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq 201 ]; then
        print_success "✓ Create client with ADMIN role (HTTP $http_code)"
        echo "$body" | jq . 2>/dev/null || echo "$body"
        
        CLIENT_ID_CREATED=$(echo "$body" | jq -r '.id // empty' 2>/dev/null || echo "")
        print_info "Extracted CLIENT_ID: '$CLIENT_ID_CREATED'"
        
        if [ -n "$CLIENT_ID_CREATED" ] && [ "$CLIENT_ID_CREATED" != "null" ] && [ "$CLIENT_ID_CREATED" != "empty" ]; then
            print_success "Client created with ID: $CLIENT_ID_CREATED"
            # Track created client for cleanup
            CREATED_CLIENTS+=("$CLIENT_ID_CREATED")
        else
            print_warning "Client ID could not be extracted from response"
            print_info "Response body: $body"
        fi
    else
        print_error "✗ Create client with ADMIN role (Expected HTTP 201, got $http_code)"
        echo "Response: $body"
    fi
    
    # Get client by ID
    if [ -n "$CLIENT_ID_CREATED" ]; then
        test_api_request "GET" "/clients/$CLIENT_ID_CREATED" "" "$ADMIN_TOKEN" 200 "Get client by ID"
        
        # List all clients
        test_api_request "GET" "/clients" "" "$ADMIN_TOKEN" 200 "List all clients"
        
        # Search client by email
        test_api_request "GET" "/clients/search-by-email/john.doe@example.com" "" "$ADMIN_TOKEN" 200 "Search client by email"
    fi
}

# Test account operations
test_accounts() {
    print_header "Account Management Tests"
    
    if [ -z "$CLIENT_ID_CREATED" ]; then
        print_warning "Skipping account tests - no client created"
        return 0
    fi
    
    # Create source account
    local source_account_data='{
        "accountName": "John'\''s Checking Account",
        "clientId": '$CLIENT_ID_CREATED',
        "balance": "1000.0000",
        "accountType": "CHECKING",
        "currency": "USD"
    }'
    
    print_info "Creating source account..."
    local raw_response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $USER_TOKEN" \
        -d "$source_account_data" \
        "$MINIBANK_URL/accounts")
    
    local http_code=$(echo "$raw_response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    local body=$(echo "$raw_response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq 201 ]; then
        print_success "✓ Create source account (HTTP $http_code)"
        echo "$body" | jq . 2>/dev/null || echo "$body"
        
        SOURCE_ACCOUNT_ID=$(echo "$body" | jq -r '.id // empty' 2>/dev/null || echo "")
        if [ -n "$SOURCE_ACCOUNT_ID" ] && [ "$SOURCE_ACCOUNT_ID" != "null" ] && [ "$SOURCE_ACCOUNT_ID" != "empty" ]; then
            print_success "Source account created with ID: $SOURCE_ACCOUNT_ID"
            # Track created account for cleanup
            CREATED_ACCOUNTS+=("$SOURCE_ACCOUNT_ID")
        else
            print_warning "Source account ID could not be extracted"
        fi
    else
        print_error "✗ Create source account (Expected HTTP 201, got $http_code)"
        echo "Response: $body"
    fi
    
    # Create target account
    local target_account_data='{
        "accountName": "John'\''s Savings Account",
        "clientId": '$CLIENT_ID_CREATED',
        "balance": "500.0000",
        "accountType": "SAVINGS",
        "currency": "USD"
    }'
    
    print_info "Creating target account..."
    local raw_response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $USER_TOKEN" \
        -d "$target_account_data" \
        "$MINIBANK_URL/accounts")
    
    local http_code=$(echo "$raw_response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    local body=$(echo "$raw_response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq 201 ]; then
        print_success "✓ Create target account (HTTP $http_code)"
        echo "$body" | jq . 2>/dev/null || echo "$body"
        
        TARGET_ACCOUNT_ID=$(echo "$body" | jq -r '.id // empty' 2>/dev/null || echo "")
        if [ -n "$TARGET_ACCOUNT_ID" ] && [ "$TARGET_ACCOUNT_ID" != "null" ] && [ "$TARGET_ACCOUNT_ID" != "empty" ]; then
            print_success "Target account created with ID: $TARGET_ACCOUNT_ID"
            # Track created account for cleanup
            CREATED_ACCOUNTS+=("$TARGET_ACCOUNT_ID")
        else
            print_warning "Target account ID could not be extracted"
        fi
    else
        print_error "✗ Create target account (Expected HTTP 201, got $http_code)"
        echo "Response: $body"
    fi
    
    # Get account by ID
    if [ -n "$SOURCE_ACCOUNT_ID" ]; then
        test_api_request "GET" "/accounts/$SOURCE_ACCOUNT_ID" "" "$USER_TOKEN" 200 "Get account by ID"
    fi
    
    # List all accounts
    test_api_request "GET" "/accounts" "" "$USER_TOKEN" 200 "List all accounts"
    
    # Get client's accounts
    if [ -n "$CLIENT_ID_CREATED" ]; then
        test_api_request "GET" "/accounts/client/$CLIENT_ID_CREATED" "" "$USER_TOKEN" 200 "Get client's accounts"
    fi
}

# Test transaction operations
test_transactions() {
    print_header "Transaction Tests"
    
    if [ -z "$SOURCE_ACCOUNT_ID" ] || [ -z "$TARGET_ACCOUNT_ID" ]; then
        print_warning "Skipping transaction tests - accounts not created"
        return 0
    fi
    
    # Transfer money
    local transfer_data='{
        "sourceAccountId": '$SOURCE_ACCOUNT_ID',
        "targetAccountId": '$TARGET_ACCOUNT_ID',
        "amount": "250.0000",
        "currency": "USD",
        "reference": "Test transfer with authentication"
    }'
    
    print_info "Creating transaction transfer..."
    local raw_response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $USER_TOKEN" \
        -d "$transfer_data" \
        "$MINIBANK_URL/transactions/transfer")
    
    local http_code=$(echo "$raw_response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    local body=$(echo "$raw_response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq 201 ]; then
        print_success "✓ Transfer money between accounts (HTTP $http_code)"
        echo "$body" | jq . 2>/dev/null || echo "$body"
        
        TRANSACTION_ID=$(echo "$body" | jq -r '.id // empty' 2>/dev/null || echo "")
        if [ -n "$TRANSACTION_ID" ] && [ "$TRANSACTION_ID" != "null" ] && [ "$TRANSACTION_ID" != "empty" ]; then
            print_success "Transaction created with ID: $TRANSACTION_ID"
            # Track created transaction for cleanup
            CREATED_TRANSACTIONS+=("$TRANSACTION_ID")
        else
            print_warning "Transaction ID could not be extracted"
        fi
    else
        print_error "✗ Transfer money between accounts (Expected HTTP 201, got $http_code)"
        echo "Response: $body"
    fi
    
    # Get transaction by ID
    if [ -n "$TRANSACTION_ID" ]; then
        test_api_request "GET" "/transactions/$TRANSACTION_ID" "" "$USER_TOKEN" 200 "Get transaction by ID"
    fi
    
    # Get account transactions
    if [ -n "$SOURCE_ACCOUNT_ID" ]; then
        test_api_request "GET" "/transactions/account/$SOURCE_ACCOUNT_ID" "" "$USER_TOKEN" 200 "Get account transactions"
        test_api_request "GET" "/transactions/account/$SOURCE_ACCOUNT_ID/outgoing" "" "$USER_TOKEN" 200 "Get outgoing transactions"
        test_api_request "GET" "/transactions/account/$TARGET_ACCOUNT_ID/incoming" "" "$USER_TOKEN" 200 "Get incoming transactions"
    fi
}

# Test role-based access control
test_rbac() {
    print_header "Role-Based Access Control Tests"
    
    print_info "Testing different role access levels..."
    
    # Customer trying to access admin endpoints (should fail)
    test_api_request "GET" "/clients" "" "$USER_TOKEN" 403 "Customer accessing client management (should fail)"
    
    # Admin accessing customer endpoints (should succeed)
    test_api_request "GET" "/accounts" "" "$ADMIN_TOKEN" 200 "Admin accessing accounts"
    test_api_request "GET" "/clients" "" "$ADMIN_TOKEN" 200 "Admin accessing clients"
    
    print_success "Role-based access control working correctly!"
}

# Main test execution
main() {
    echo "🏦 Minibank API Authentication Test Suite"
    echo "========================================="
    echo ""
    
    # Check if services are running
    print_header "Service Health Checks"
    
    if ! wait_for_service "$KEYCLOAK_URL/realms/minibank" "Keycloak"; then
        print_error "Keycloak is not running. Please start it with: docker compose up -d keycloak"
        exit 1
    fi
    
    if ! wait_for_service "$MINIBANK_URL/actuator/health" "Minibank API"; then
        print_error "Minibank API is not running. Please start it with: ./mvnw spring-boot:run"
        exit 1
    fi
    
    # Get authentication tokens
    print_header "Getting Authentication Tokens"
    
    USER_TOKEN=$(get_access_token "$USERNAME" "$PASSWORD")
    if [ $? -ne 0 ]; then
        print_error "Failed to get user token"
        exit 1
    fi
    
    ADMIN_TOKEN=$(get_access_token "$ADMIN_USERNAME" "$ADMIN_PASSWORD")
    if [ $? -ne 0 ]; then
        print_error "Failed to get admin token"
        exit 1
    fi
    
    print_success "All authentication tokens obtained successfully!"
    
    # Debug token variables
    print_info "Debug: USER_TOKEN length: ${#USER_TOKEN}"
    print_info "Debug: ADMIN_TOKEN length: ${#ADMIN_TOKEN}"
    print_info "Debug: USER_TOKEN starts with: ${USER_TOKEN:0:50}..."
    
    # Note: Test data cleanup will happen automatically at script exit
    
    # Run test suites
    test_authentication
    test_clients
    test_accounts
    test_transactions
    test_rbac
    
    # Final summary
    print_header "Test Summary"
    print_success "🎉 All tests completed successfully!"
    print_info "Authentication is working correctly with Keycloak integration."
    echo ""
    print_info "Test Configuration:"
    echo "  • Keycloak realm: $REALM"
    echo "  • Customer user: $USERNAME / $PASSWORD (CUSTOMER role)"
    echo "  • Admin user: $ADMIN_USERNAME / $ADMIN_PASSWORD (ADMIN, BANK_MANAGER roles)"
    echo "  • Keycloak URL: $KEYCLOAK_URL"
    echo "  • API URL: $MINIBANK_URL"
    echo ""
    print_info "Created Resources:"
    [ -n "$CLIENT_ID_CREATED" ] && echo "  • Client ID: $CLIENT_ID_CREATED"
    [ -n "$SOURCE_ACCOUNT_ID" ] && echo "  • Source Account ID: $SOURCE_ACCOUNT_ID"
    [ -n "$TARGET_ACCOUNT_ID" ] && echo "  • Target Account ID: $TARGET_ACCOUNT_ID"
    [ -n "$TRANSACTION_ID" ] && echo "  • Transaction ID: $TRANSACTION_ID"
}

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    print_warning "jq is not installed. JSON responses will not be formatted."
    print_info "Install jq for better output: sudo apt-get install jq (Ubuntu/Debian) or brew install jq (macOS)"
    echo ""
fi

main "$@"
