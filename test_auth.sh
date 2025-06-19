#!/bin/bash

# Minibank Authentication Test Script
# Tests Keycloak integration and JWT token authentication

set -e

echo "🔐 Minibank Authentication Test"
echo "================================"

# Configuration
KEYCLOAK_URL="http://localhost:8080"
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

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Function to wait for service
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    print_status "Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$url" > /dev/null 2>&1; then
            print_success "$service_name is ready!"
            return 0
        fi
        print_status "Attempt $attempt/$max_attempts - $service_name not ready yet..."
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
    
    print_status "Getting access token for user: $username"
    
    local response=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "grant_type=password" \
        -d "client_id=$CLIENT_ID" \
        -d "client_secret=$CLIENT_SECRET" \
        -d "username=$username" \
        -d "password=$password")
    
    local access_token=$(echo "$response" | jq -r '.access_token // empty')
    
    if [ -z "$access_token" ] || [ "$access_token" = "null" ]; then
        print_error "Failed to get access token for $username"
        echo "Response: $response"
        return 1
    fi
    
    print_success "Access token obtained for $username"
    echo "$access_token"
}

# Function to test API endpoint
test_endpoint() {
    local endpoint=$1
    local token=$2
    local expected_status=$3
    local description=$4
    
    print_status "Testing: $description"
    print_status "Endpoint: GET $endpoint"
    
    if [ -n "$token" ]; then
        local response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
            -H "Authorization: Bearer $token" \
            "$MINIBANK_URL$endpoint")
    else
        local response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
            "$MINIBANK_URL$endpoint")
    fi
    
    local http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    local body=$(echo "$response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq "$expected_status" ]; then
        print_success "✓ $description (HTTP $http_code)"
        if [ -n "$body" ] && [ "$body" != "null" ]; then
            echo "Response: $body" | jq . 2>/dev/null || echo "Response: $body"
        fi
    else
        print_error "✗ $description (Expected HTTP $expected_status, got $http_code)"
        echo "Response: $body"
        return 1
    fi
    
    echo ""
}

# Main test execution
main() {
    echo "Starting authentication tests..."
    echo ""
    
    # Wait for services
    print_status "Checking if services are running..."
    
    if ! wait_for_service "$KEYCLOAK_URL/health/ready" "Keycloak"; then
        print_error "Keycloak is not running. Please start it with: docker compose up -d keycloak"
        exit 1
    fi
    
    if ! wait_for_service "$MINIBANK_URL/actuator/health" "Minibank API"; then
        print_error "Minibank API is not running. Please start it with: ./mvnw spring-boot:run"
        exit 1
    fi
    
    echo ""
    print_status "Both services are ready! Starting authentication tests..."
    echo ""
    
    # Test 1: Public endpoint (no auth required)
    test_endpoint "/auth/public" "" 200 "Public endpoint access"
    
    # Test 2: Protected endpoint without token (should fail)
    test_endpoint "/auth/me" "" 401 "Protected endpoint without token (should fail)"
    
    # Test 3: Get token for regular user
    USER_TOKEN=$(get_access_token "$USERNAME" "$PASSWORD")
    if [ $? -ne 0 ]; then
        print_error "Failed to get user token"
        exit 1
    fi
    echo ""
    
    # Test 4: Access user info with valid token
    test_endpoint "/auth/me" "$USER_TOKEN" 200 "User info with valid token"
    
    # Test 5: Customer endpoint with customer role
    test_endpoint "/auth/customer" "$USER_TOKEN" 200 "Customer endpoint with CUSTOMER role"
    
    # Test 6: Admin endpoint with customer role (should fail)
    test_endpoint "/auth/admin" "$USER_TOKEN" 403 "Admin endpoint with CUSTOMER role (should fail)"
    
    # Test 7: Get token for admin user
    ADMIN_TOKEN=$(get_access_token "$ADMIN_USERNAME" "$ADMIN_PASSWORD")
    if [ $? -ne 0 ]; then
        print_error "Failed to get admin token"
        exit 1
    fi
    echo ""
    
    # Test 8: Admin endpoint with admin role
    test_endpoint "/auth/admin" "$ADMIN_TOKEN" 200 "Admin endpoint with ADMIN role"
    
    # Test 9: Test actual API endpoints
    test_endpoint "/clients" "$ADMIN_TOKEN" 200 "Clients endpoint with ADMIN role"
    test_endpoint "/accounts" "$USER_TOKEN" 200 "Accounts endpoint with CUSTOMER role"
    
    echo ""
    print_success "🎉 All authentication tests completed successfully!"
    print_status "Authentication is working correctly with Keycloak integration."
    echo ""
    print_status "Test Summary:"
    echo "  • Keycloak realm: $REALM"
    echo "  • Default user: $USERNAME / $PASSWORD (CUSTOMER role)"
    echo "  • Admin user: $ADMIN_USERNAME / $ADMIN_PASSWORD (ADMIN role)"
    echo "  • Keycloak URL: $KEYCLOAK_URL"
    echo "  • API URL: $MINIBANK_URL"
}

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    print_warning "jq is not installed. JSON responses will not be formatted."
    print_status "Install jq for better output: sudo apt-get install jq (Ubuntu/Debian) or brew install jq (macOS)"
    echo ""
fi

main "$@"