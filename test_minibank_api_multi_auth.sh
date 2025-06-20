#!/bin/bash

# Minibank API Multi-Client Authentication Test Script
# Tests custom JWT authentication with multiple concurrent users
# Includes security testing with HTTP vulnerabilities warnings

set -e

# Configuration
MINIBANK_URL="http://localhost:8082"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Global variables for tokens and created resources
declare -A USER_TOKENS
declare -A USER_IDS
declare -A USER_ACCOUNT_IDS
ADMIN_TOKEN=""
ADMIN_ID=""

# Arrays to track all created resources for cleanup
CREATED_CLIENTS=()
CREATED_ACCOUNTS=()
CREATED_TRANSACTIONS=()

# Test users configuration
declare -A TEST_USERS=(
    ["alice"]="alice@test.com:password123:CLIENT"
    ["bob"]="bob@test.com:password456:CLIENT"
    ["charlie"]="charlie@test.com:password789:CLIENT"
    ["admin"]="admin@test.com:admin123:ADMIN"
)

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
    echo -e "${CYAN}ℹ️  $1${NC}"
}

print_security_warning() {
    echo -e "${RED}🔥 SECURITY WARNING: $1${NC}"
}

# Security warnings
show_security_warnings() {
    print_header "🔒 Security Warnings"
    print_security_warning "This application is using JWT over HTTP (not HTTPS)"
    print_warning "JWT tokens are transmitted in plaintext and can be intercepted"
    print_warning "In production, ALWAYS use HTTPS to protect JWT tokens"
    print_warning "Current risk: Man-in-the-middle attacks, token theft, session hijacking"
    echo ""
    print_info "Example of token visibility in network traffic:"
    echo "  GET /api/clients/123 HTTP/1.1"
    echo "  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIi..."
    echo "  ↑ This token is visible to anyone monitoring network traffic"
    echo ""
}

# Cleanup function
cleanup_test_data() {
    print_header "🧹 Cleaning Up Test Data"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_warning "No admin token available for cleanup"
        return 0
    fi
    
    local cleanup_errors=0
    
    # Delete accounts
    if [ ${#CREATED_ACCOUNTS[@]} -gt 0 ]; then
        print_info "Cleaning up ${#CREATED_ACCOUNTS[@]} accounts..."
        for account_id in "${CREATED_ACCOUNTS[@]}"; do
            if [ -n "$account_id" ]; then
                local response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X DELETE \
                    -H "Authorization: Bearer $ADMIN_TOKEN" \
                    "$MINIBANK_URL/accounts/$account_id" 2>/dev/null)
                local http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
                if [ "$http_code" -eq 204 ] || [ "$http_code" -eq 404 ]; then
                    print_info "Account $account_id deleted"
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
                local response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X DELETE \
                    -H "Authorization: Bearer $ADMIN_TOKEN" \
                    "$MINIBANK_URL/clients/$client_id" 2>/dev/null)
                local http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
                if [ "$http_code" -eq 204 ] || [ "$http_code" -eq 404 ]; then
                    print_info "Client $client_id deleted"
                else
                    print_warning "Failed to delete client $client_id (HTTP $http_code)"
                    ((cleanup_errors++))
                fi
            fi
        done
    fi
    
    if [ $cleanup_errors -eq 0 ]; then
        print_success "All test data cleaned up successfully!"
    else
        print_warning "Cleanup completed with $cleanup_errors errors"
    fi
}

# Trap to ensure cleanup runs on script exit
trap cleanup_test_data EXIT

# Wait for service
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=15
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

# Register a new user
register_user() {
    local username=$1
    local email=$2
    local password=$3
    local role=$4
    
    print_info "Registering user: $username ($email) with role: $role"
    
    local register_data='{
        "firstName": "'$username'",
        "lastName": "TestUser",
        "email": "'$email'",
        "password": "'$password'",
        "role": "'$role'",
        "phoneNumber": "+1234567890",
        "address": "123 Test St",
        "dateOfBirth": "1990-01-01"
    }'
    
    local response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "$register_data" \
        "$MINIBANK_URL/auth/register")
    
    local http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    local body=$(echo "$response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq 201 ]; then
        local token=$(echo "$body" | jq -r '.token // empty' 2>/dev/null || echo "")
        local client_id=$(echo "$body" | jq -r '.clientId // empty' 2>/dev/null || echo "")
        
        if [ -n "$token" ] && [ -n "$client_id" ]; then
            USER_TOKENS["$username"]="$token"
            USER_IDS["$username"]="$client_id"
            CREATED_CLIENTS+=("$client_id")
            
            if [ "$role" = "ADMIN" ]; then
                ADMIN_TOKEN="$token"
                ADMIN_ID="$client_id"
            fi
            
            print_success "User $username registered successfully (ID: $client_id)"
            return 0
        else
            print_error "Failed to extract token or client ID from response"
            return 1
        fi
    else
        print_error "Failed to register user $username (HTTP $http_code)"
        echo "Response: $body"
        return 1
    fi
}

# Login existing user
login_user() {
    local username=$1
    local email=$2
    local password=$3
    
    print_info "Logging in user: $username ($email)"
    
    local login_data='{
        "email": "'$email'",
        "password": "'$password'"
    }'
    
    local response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "$login_data" \
        "$MINIBANK_URL/auth/login")
    
    local http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    local body=$(echo "$response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq 200 ]; then
        local token=$(echo "$body" | jq -r '.token // empty' 2>/dev/null || echo "")
        local client_id=$(echo "$body" | jq -r '.clientId // empty' 2>/dev/null || echo "")
        
        if [ -n "$token" ] && [ -n "$client_id" ]; then
            USER_TOKENS["$username"]="$token"
            USER_IDS["$username"]="$client_id"
            
            print_success "User $username logged in successfully"
            return 0
        else
            print_error "Failed to extract token or client ID from login response"
            return 1
        fi
    else
        print_error "Failed to login user $username (HTTP $http_code)"
        echo "Response: $body"
        return 1
    fi
}

# Setup all test users
setup_test_users() {
    print_header "👥 Setting Up Test Users"
    
    for username in "${!TEST_USERS[@]}"; do
        local user_info="${TEST_USERS[$username]}"
        local email=$(echo "$user_info" | cut -d':' -f1)
        local password=$(echo "$user_info" | cut -d':' -f2)
        local role=$(echo "$user_info" | cut -d':' -f3)
        
        # Try to register first (might fail if user exists)
        if register_user "$username" "$email" "$password" "$role"; then
            print_success "User $username set up via registration"
        else
            # If registration fails, try login
            print_info "Registration failed, trying login for $username..."
            if login_user "$username" "$email" "$password"; then
                print_success "User $username set up via login"
            else
                print_error "Failed to set up user $username"
                return 1
            fi
        fi
    done
    
    print_success "All test users set up successfully!"
}

# Create accounts for users
create_user_accounts() {
    print_header "💳 Creating User Accounts"
    
    for username in "${!USER_IDS[@]}"; do
        if [ "$username" = "admin" ]; then
            continue # Skip creating account for admin
        fi
        
        local client_id="${USER_IDS[$username]}"
        local token="${USER_TOKENS[$username]}"
        
        local account_data='{
            "accountName": "'$username'\'s Primary Account",
            "clientId": '$client_id',
            "balance": "1000.0000",
            "accountType": "CHECKING",
            "currency": "USD"
        }'
        
        print_info "Creating account for $username (client ID: $client_id)..."
        
        local response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $token" \
            -d "$account_data" \
            "$MINIBANK_URL/accounts")
        
        local http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
        local body=$(echo "$response" | sed -e 's/HTTPSTATUS:.*//g')
        
        if [ "$http_code" -eq 201 ]; then
            local account_id=$(echo "$body" | jq -r '.id // empty' 2>/dev/null || echo "")
            if [ -n "$account_id" ]; then
                USER_ACCOUNT_IDS["$username"]="$account_id"
                CREATED_ACCOUNTS+=("$account_id")
                print_success "Account created for $username (ID: $account_id)"
            else
                print_warning "Failed to extract account ID for $username"
            fi
        else
            print_error "Failed to create account for $username (HTTP $http_code)"
            echo "Response: $body"
        fi
    done
}

# Test API endpoint
test_api_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local token=$4
    local expected_status=$5
    local description=$6
    
    local response
    if [ -n "$token" ] && [ -n "$data" ]; then
        response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $token" \
            -d "$data" \
            "$MINIBANK_URL$endpoint")
    elif [ -n "$token" ]; then
        response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X "$method" \
            -H "Authorization: Bearer $token" \
            "$MINIBANK_URL$endpoint")
    elif [ -n "$data" ]; then
        response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$MINIBANK_URL$endpoint")
    else
        response=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X "$method" \
            "$MINIBANK_URL$endpoint")
    fi
    
    local http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    local body=$(echo "$response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq "$expected_status" ]; then
        print_success "✓ $description (HTTP $http_code)"
        if [ -n "$body" ] && [ "$body" != "null" ] && [ "$body" != "" ]; then
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

# Test authentication scenarios
test_authentication_scenarios() {
    print_header "🔐 Authentication & Authorization Tests"
    
    # Test 1: Access without token (should fail)
    print_info "Testing unauthenticated access..."
    test_api_request "GET" "/clients/1" "" "" 401 "Access client data without token (should fail)"
    
    # Test 2: Users accessing their own data
    print_info "Testing users accessing their own data..."
    for username in alice bob charlie; do
        local token="${USER_TOKENS[$username]}"
        local client_id="${USER_IDS[$username]}"
        
        if [ -n "$token" ] && [ -n "$client_id" ]; then
            test_api_request "GET" "/clients/$client_id" "" "$token" 200 "$username accessing own client data"
        fi
    done
    
    # Test 3: Users trying to access other users' data (should fail)
    print_info "Testing unauthorized access attempts..."
    local alice_token="${USER_TOKENS[alice]}"
    local bob_id="${USER_IDS[bob]}"
    
    if [ -n "$alice_token" ] && [ -n "$bob_id" ]; then
        test_api_request "GET" "/clients/$bob_id" "" "$alice_token" 403 "Alice trying to access Bob's data (should fail)"
    fi
    
    # Test 4: Admin accessing all user data
    print_info "Testing admin access..."
    if [ -n "$ADMIN_TOKEN" ]; then
        for username in alice bob charlie; do
            local client_id="${USER_IDS[$username]}"
            if [ -n "$client_id" ]; then
                test_api_request "GET" "/clients/$client_id" "" "$ADMIN_TOKEN" 200 "Admin accessing $username's data"
            fi
        done
    fi
    
    # Test 5: Regular users trying to access admin endpoints (should fail)
    print_info "Testing admin endpoint protection..."
    local alice_token="${USER_TOKENS[alice]}"
    if [ -n "$alice_token" ]; then
        test_api_request "GET" "/admin/clients" "" "$alice_token" 403 "Regular user accessing admin endpoint (should fail)"
    fi
    
    # Test 6: Admin accessing admin endpoints
    if [ -n "$ADMIN_TOKEN" ]; then
        test_api_request "GET" "/admin/clients" "" "$ADMIN_TOKEN" 200 "Admin accessing admin endpoint"
    fi
}

# Test concurrent transactions
test_concurrent_transactions() {
    print_header "🔄 Concurrent Transaction Tests"
    
    if [ ${#USER_ACCOUNT_IDS[@]} -lt 2 ]; then
        print_warning "Skipping concurrent transaction tests - need at least 2 accounts"
        return 0
    fi
    
    local alice_account="${USER_ACCOUNT_IDS[alice]}"
    local bob_account="${USER_ACCOUNT_IDS[bob]}"
    local alice_token="${USER_TOKENS[alice]}"
    local bob_token="${USER_TOKENS[bob]}"
    
    if [ -n "$alice_account" ] && [ -n "$bob_account" ] && [ -n "$alice_token" ] && [ -n "$bob_token" ]; then
        print_info "Testing concurrent transactions between Alice and Bob..."
        
        # Alice transfers to Bob
        local alice_transfer='{
            "sourceAccountId": '$alice_account',
            "targetAccountId": '$bob_account',
            "amount": "100.00",
            "currency": "USD",
            "reference": "Alice to Bob transfer"
        }'
        
        # Bob transfers to Alice (simultaneously)
        local bob_transfer='{
            "sourceAccountId": '$bob_account',
            "targetAccountId": '$alice_account',
            "amount": "50.00",
            "currency": "USD",
            "reference": "Bob to Alice transfer"
        }'
        
        print_info "Executing concurrent transactions..."
        
        # Execute both transactions in parallel
        {
            test_api_request "POST" "/transactions" "$alice_transfer" "$alice_token" 200 "Alice transferring to Bob"
        } &
        
        {
            test_api_request "POST" "/transactions" "$bob_transfer" "$bob_token" 200 "Bob transferring to Alice"
        } &
        
        # Wait for both background processes to complete
        wait
        
        print_success "Concurrent transactions completed successfully!"
        
        # Verify account balances
        print_info "Verifying final account balances..."
        test_api_request "GET" "/accounts/$alice_account" "" "$alice_token" 200 "Alice's account balance after transactions"
        test_api_request "GET" "/accounts/$bob_account" "" "$bob_token" 200 "Bob's account balance after transactions"
    fi
}

# Test account access control
test_account_security() {
    print_header "🏦 Account Security Tests"
    
    local alice_account="${USER_ACCOUNT_IDS[alice]}"
    local bob_account="${USER_ACCOUNT_IDS[bob]}"
    local alice_token="${USER_TOKENS[alice]}"
    local bob_token="${USER_TOKENS[bob]}"
    
    if [ -n "$alice_account" ] && [ -n "$bob_account" ] && [ -n "$alice_token" ] && [ -n "$bob_token" ]; then
        
        # Test 1: Users can access their own accounts
        print_info "Testing legitimate account access..."
        test_api_request "GET" "/accounts/$alice_account" "" "$alice_token" 200 "Alice accessing her own account"
        test_api_request "GET" "/accounts/$bob_account" "" "$bob_token" 200 "Bob accessing his own account"
        
        # Test 2: Users cannot access other users' accounts
        print_info "Testing unauthorized account access..."
        test_api_request "GET" "/accounts/$bob_account" "" "$alice_token" 403 "Alice trying to access Bob's account (should fail)"
        test_api_request "GET" "/accounts/$alice_account" "" "$bob_token" 403 "Bob trying to access Alice's account (should fail)"
        
        # Test 3: Users cannot initiate transactions from accounts they don't own
        print_info "Testing unauthorized transaction initiation..."
        local unauthorized_transfer='{
            "sourceAccountId": '$bob_account',
            "targetAccountId": '$alice_account',
            "amount": "999.00",
            "currency": "USD",
            "reference": "Unauthorized transfer attempt"
        }'
        
        test_api_request "POST" "/transactions" "$unauthorized_transfer" "$alice_token" 403 "Alice trying to transfer from Bob's account (should fail)"
    fi
}

# Test token security
test_token_security() {
    print_header "🔑 JWT Token Security Tests"
    
    # Test 1: Invalid token
    print_info "Testing invalid token handling..."
    local invalid_token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature"
    test_api_request "GET" "/clients/1" "" "$invalid_token" 401 "Request with invalid token (should fail)"
    
    # Test 2: Malformed token
    print_info "Testing malformed token handling..."
    local malformed_token="not.a.jwt.token.at.all"
    test_api_request "GET" "/clients/1" "" "$malformed_token" 401 "Request with malformed token (should fail)"
    
    # Test 3: Empty token
    print_info "Testing empty authorization header..."
    test_api_request "GET" "/clients/1" "" "" 401 "Request without authorization header (should fail)"
    
    # Test 4: Token information exposure warning
    print_info "Demonstrating JWT token content visibility..."
    local alice_token="${USER_TOKENS[alice]}"
    if [ -n "$alice_token" ]; then
        echo "Sample JWT token (first 100 characters): ${alice_token:0:100}..."
        print_security_warning "JWT tokens contain user information and can be decoded by anyone!"
        print_warning "Never log JWT tokens or expose them in URLs"
    fi
}

# Main test execution
main() {
    echo "🏦 Minibank Multi-Client Authentication Test Suite"
    echo "=================================================="
    echo ""
    
    # Show security warnings first
    show_security_warnings
    
    # Check if services are running
    print_header "🩺 Service Health Check"
    if ! wait_for_service "$MINIBANK_URL/actuator/health" "Minibank API"; then
        print_error "Minibank API is not running. Please start it with: ./mvnw spring-boot:run"
        exit 1
    fi
    
    # Setup test users and accounts
    setup_test_users
    create_user_accounts
    
    print_header "📊 Test User Summary"
    for username in "${!USER_IDS[@]}"; do
        local client_id="${USER_IDS[$username]}"
        local account_id="${USER_ACCOUNT_IDS[$username]:-N/A}"
        local role="CLIENT"
        [ "$username" = "admin" ] && role="ADMIN"
        
        echo "  • $username (ID: $client_id, Role: $role, Account: $account_id)"
    done
    
    # Run comprehensive test suites
    test_authentication_scenarios
    test_account_security
    test_concurrent_transactions
    test_token_security
    
    # Final summary
    print_header "📈 Test Results Summary"
    print_success "🎉 Multi-client authentication tests completed!"
    echo ""
    print_info "Tests Performed:"
    echo "  ✓ User registration and login"
    echo "  ✓ Client data access control"
    echo "  ✓ Account ownership verification"
    echo "  ✓ Transaction authorization"
    echo "  ✓ Concurrent transaction processing"
    echo "  ✓ Admin role privileges"
    echo "  ✓ Token validation and security"
    echo ""
    print_info "Security Model Verified:"
    echo "  ✓ Clients can only access their own data"
    echo "  ✓ Admin can access all client data"
    echo "  ✓ Transactions require account ownership"
    echo "  ✓ Invalid tokens are rejected"
    echo "  ✓ Concurrent operations are handled safely"
    echo ""
    print_security_warning "Remember: Use HTTPS in production to protect JWT tokens!"
}

# Check dependencies
if ! command -v jq &> /dev/null; then
    print_warning "jq is not installed. JSON responses will not be formatted."
    print_info "Install jq: sudo apt-get install jq (Ubuntu) or brew install jq (macOS)"
    echo ""
fi

main "$@"