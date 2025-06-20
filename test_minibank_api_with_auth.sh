#!/bin/bash

# ==============================================================================
# Exhaustive Test Suite for the Minibank API
#
# Version: 1.0
# Description: This script performs a comprehensive end-to-end test of the
#              Minibank application, covering authentication, authorization,
#              business logic, and error handling for all major endpoints.
# ==============================================================================

set -o pipefail

# --- Configuration ---
KEYCLOAK_URL="http://localhost:8080"
MINIBANK_URL="http://localhost:8082"
REALM="minibank"
CLIENT_ID="minibank-api"
CLIENT_SECRET="minibank-secret"

# --- Test Users ---
ADMIN_USER="admin"
ADMIN_PASS="admin"
USER1_NAME="user"
USER1_PASS="user"
USER2_NAME="user2"
USER2_PASS="user2pass"

# --- Keycloak User IDs (from minibank-realm.json and created user) ---
USER1_KEYCLOAK_ID="c9b2a7a4-3e9f-4b9a-b24c-22a36d23f310"
USER2_KEYCLOAK_ID="" # Will be fetched dynamically

# --- Global State ---
ADMIN_TOKEN=""
USER1_TOKEN=""
USER2_TOKEN=""
USER1_CLIENT_ID=""
USER2_CLIENT_ID=""
USER1_CHECKING_ID=""
USER1_SAVINGS_ID=""
USER2_CHECKING_ID=""
CREATED_RESOURCES=() # Tracks all created resources for cleanup

# --- Test Counters ---
TESTS_RUN=0
TESTS_PASSED=0

# --- Color Definitions ---
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ==============================================================================
# Helper Functions
# ==============================================================================

# Prints a section header
print_header() {
    echo -e "\n${CYAN}# ============================================================${NC}"
    echo -e "${CYAN}# $1${NC}"
    echo -e "${CYAN}# ============================================================${NC}"
}

# Logs a test result and increments counters
log_test() {
    local description=$1
    local status=$2
    ((TESTS_RUN++))
    if [ "$status" -eq 0 ]; then
        ((TESTS_PASSED++))
        echo -e "${GREEN}✓ PASSED:${NC} $description"
    else
        echo -e "${RED}✗ FAILED:${NC} $description"
    fi
    return "$status"
}

# Generic API request function
# Usage: test_api "description" "METHOD" "/endpoint" "data" "token" "expected_status"
test_api() {
    local description="$1" method="$2" endpoint="$3" data="$4" token="$5" expected_status="$6"
    local result=0
    local response_body=""

    local headers=(-s -w "\n%{http_code}" -H "Content-Type: application/json")
    [ -n "$token" ] && headers+=(-H "Authorization: Bearer $token")

    local response=$(curl "${headers[@]}" -X "$method" --data "$data" "$MINIBANK_URL$endpoint")

    local http_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | sed '$d')

    if [ "$http_code" -ne "$expected_status" ]; then
        echo -e "\n${YELLOW}--- Test Details ---${NC}"
        echo "Endpoint: $method $endpoint"
        echo "Expected Status: $expected_status, Got: $http_code"
        echo "Response Body:"
        echo "$response_body" | jq . 2>/dev/null || echo "$response_body"
        echo -e "${YELLOW}--------------------${NC}"
        result=1
    fi

    log_test "$description" "$result"

    # Return the body for successful calls, so it can be captured
    if [ "$result" -eq 0 ]; then
        echo "$response_body"
    fi

    return "$result"
}

# Waits for a service to become healthy
wait_for_service() {
    local url=$1 name=$2 max_attempts=30 attempt=1
    echo -e "${BLUE}--> Waiting for $name at $url...${NC}"
    while ! curl -f -s "$url" >/dev/null; do
        if [ $attempt -ge $max_attempts ]; then
            log_test "$name readiness" 1
            echo "Error: $name did not become healthy in time."
            exit 1
        fi
        printf "."
        sleep 2
        ((attempt++))
    done
    echo ""
    log_test "$name readiness" 0
}

# Retrieves an access token from Keycloak
get_token() {
    local user=$1 pass=$2
    local token=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "grant_type=password&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&username=$user&password=$pass" | jq -r '.access_token')

    if [ -z "$token" ] || [ "$token" == "null" ]; then
        log_test "Get token for user '$user'" 1
        return 1
    fi
    log_test "Get token for user '$user'" 0
    echo "$token"
}

# Creates a user in Keycloak via its Admin API
create_keycloak_user() {
    local user=$1 pass=$2
    local response_code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$KEYCLOAK_URL/admin/realms/$REALM/users" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -d '{"username": "'$user'", "enabled": true, "credentials": [{"type": "password", "value": "'$pass'", "temporary": false}]}')
    [ "$response_code" -eq 201 ] || [ "$response_code" -eq 409 ] # 201 Created or 409 Conflict if exists
    log_test "Create Keycloak user '$user'" $?
}

# Fetches a Keycloak user's internal ID
get_keycloak_id() {
    local user=$1
    local id=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM/users?username=$user" \
      -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')
    if [ -z "$id" ] || [ "$id" == "null" ]; then
        log_test "Get Keycloak ID for user '$user'" 1
        return 1
    fi
    log_test "Get Keycloak ID for user '$user'" 0
    echo "$id"
}

# Function to run the setup process
run_setup() {
    print_header "SETUP: Preparing Test Environment"
    ADMIN_TOKEN=$(get_token "$ADMIN_USER" "$ADMIN_PASS")

    # Create User 2 in Keycloak if not exists
    create_keycloak_user "$USER2_NAME" "$USER2_PASS"
    USER2_KEYCLOAK_ID=$(get_keycloak_id "$USER2_NAME")

    # Get tokens for test users
    USER1_TOKEN=$(get_token "$USER1_NAME" "$USER1_PASS")
    USER2_TOKEN=$(get_token "$USER2_NAME" "$USER2_PASS")

    # Admin creates client profiles for both users
    local client1_payload='{"keycloakUserId":"'$USER1_KEYCLOAK_ID'","firstName":"Test","lastName":"UserOne","email":"user1@test.com"}'
    local client2_payload='{"keycloakUserId":"'$USER2_KEYCLOAK_ID'","firstName":"Test","lastName":"UserTwo","email":"user2@test.com"}'

    local client1_response=$(test_api "[Admin] Create client profile for User 1" "POST" "/admin/clients" "$client1_payload" "$ADMIN_TOKEN" 201) || exit 1
    USER1_CLIENT_ID=$(echo "$client1_response" | jq -r '.id')
    CREATED_RESOURCES+=("client/$USER1_CLIENT_ID")

    local client2_response=$(test_api "[Admin] Create client profile for User 2" "POST" "/admin/clients" "$client2_payload" "$ADMIN_TOKEN" 201) || exit 1
    USER2_CLIENT_ID=$(echo "$client2_response" | jq -r '.id')
    CREATED_RESOURCES+=("client/$USER2_CLIENT_ID")
}

# Function to run all tests
run_tests() {
    print_header "TEST SUITE: Client Profile Endpoints"
    test_api "[User] Get own profile via /me" "GET" "/clients/me" "" "$USER1_TOKEN" 200
    test_api "[User] Fails to get admin client list" "GET" "/admin/clients" "" "$USER1_TOKEN" 403
    test_api "[Admin] Gets list of all clients" "GET" "/admin/clients" "" "$ADMIN_TOKEN" 200

    print_header "TEST SUITE: Account Management Endpoints"
    local acc1_payload='{"accountType":"CHECKING","balance":"2000.00","currency":"USD","clientId":'$USER1_CLIENT_ID'}'
    local acc1_response=$(test_api "[User 1] Create CHECKING account" "POST" "/accounts" "$acc1_payload" "$USER1_TOKEN" 201)
    USER1_CHECKING_ID=$(echo "$acc1_response" | jq -r '.id')
    CREATED_RESOURCES+=("account/$USER1_CHECKING_ID")

    local acc2_payload='{"accountType":"SAVINGS","balance":"500.00","currency":"USD","clientId":'$USER1_CLIENT_ID'}'
    local acc2_response=$(test_api "[User 1] Create SAVINGS account" "POST" "/accounts" "$acc2_payload" "$USER1_TOKEN" 201)
    USER1_SAVINGS_ID=$(echo "$acc2_response" | jq -r '.id')
    CREATED_RESOURCES+=("account/$USER1_SAVINGS_ID")

    local acc3_payload='{"accountType":"CHECKING","balance":"1500.00","currency":"USD","clientId":'$USER2_CLIENT_ID'}'
    local acc3_response=$(test_api "[User 2] Create CHECKING account" "POST" "/accounts" "$acc3_payload" "$USER2_TOKEN" 201)
    USER2_CHECKING_ID=$(echo "$acc3_response" | jq -r '.id')
    CREATED_RESOURCES+=("account/$USER2_CHECKING_ID")

    test_api "[User 1] Get own account list" "GET" "/accounts/client/$USER1_CLIENT_ID" "" "$USER1_TOKEN" 200
    test_api "[User 1] Get own specific account" "GET" "/accounts/$USER1_CHECKING_ID" "" "$USER1_TOKEN" 200
    test_api "[Admin] Get any specific account" "GET" "/accounts/$USER1_CHECKING_ID" "" "$ADMIN_TOKEN" 200

    print_header "TEST SUITE: Transaction Endpoints"
    local transfer_payload='{"sourceAccountId":'$USER1_CHECKING_ID',"targetAccountId":'$USER2_CHECKING_ID',"amount":"150.75","currency":"USD","reference":"Payment for services"}'
    test_api "[User 1] Transfer money to User 2" "POST" "/transactions/transfer" "$transfer_payload" "$USER1_TOKEN" 201
    test_api "[User 1] Verify own outgoing transactions" "GET" "/transactions/account/$USER1_CHECKING_ID/outgoing" "" "$USER1_TOKEN" 200
    test_api "[User 2] Verify own incoming transactions" "GET" "/transactions/account/$USER2_CHECKING_ID/incoming" "" "$USER2_TOKEN" 200

    print_header "TEST SUITE: Security Failure Scenarios (Authorization)"
    test_api "[SECURITY] Unauthenticated access is denied" "GET" "/clients/me" "" "" 401
    test_api "[SECURITY] User 1 is FORBIDDEN from viewing User 2's accounts" "GET" "/accounts/client/$USER2_CLIENT_ID" "" "$USER1_TOKEN" 403
    test_api "[SECURITY] User 1 is FORBIDDEN from initiating transfer from User 2's account" "POST" "/transactions/transfer" '{"sourceAccountId":'$USER2_CHECKING_ID',"targetAccountId":'$USER1_SAVINGS_ID',"amount":"10","currency":"USD"}' "$USER1_TOKEN" 403
    test_api "[SECURITY] User 1 is FORBIDDEN from viewing User 2's transaction list" "GET" "/transactions/account/$USER2_CHECKING_ID" "" "$USER1_TOKEN" 403

    print_header "TEST SUITE: Business Logic Failure Scenarios"
    local insufficient_payload='{"sourceAccountId":'$USER1_SAVINGS_ID',"targetAccountId":'$USER2_CHECKING_ID',"amount":"9999.99","currency":"USD"}'
    test_api "[BUSINESS] Fails transfer with insufficient funds" "POST" "/transactions/transfer" "$insufficient_payload" "$USER1_TOKEN" 400

    local mismatch_payload='{"sourceAccountId":'$USER1_CHECKING_ID',"targetAccountId":'$USER2_CHECKING_ID',"amount":"10","currency":"EUR"}'
    test_api "[BUSINESS] Fails transfer with currency mismatch" "POST" "/transactions/transfer" "$mismatch_payload" "$USER1_TOKEN" 400
}

# Function to run cleanup
run_cleanup() {
    print_header "CLEANUP: Removing Test Data"
    if [ -z "$ADMIN_TOKEN" ]; then
        log_test "Cleanup skipped (no admin token)" 1
        return
    fi

    # Cleanup is tricky due to dependencies. Accounts must be deleted first.
    # The client deletion is a soft-delete (status update) as per API.
    for resource in "${CREATED_RESOURCES[@]}"; do
        local type=$(echo "$resource" | cut -d'/' -f1)
        local id=$(echo "$resource" | cut -d'/' -f2)

        if [ "$type" == "account" ]; then
             test_api "[Cleanup] Delete account $id" "DELETE" "/accounts/$id" "" "$ADMIN_TOKEN" 204 || \
             test_api "[Cleanup] Delete account $id (already gone)" "DELETE" "/accounts/$id" "" "$ADMIN_TOKEN" 404
        fi
    done

    # After accounts, update client status
    for resource in "${CREATED_RESOURCES[@]}"; do
        local type=$(echo "$resource" | cut -d'/' -f1)
        local id=$(echo "$resource" | cut -d'/' -f2)

        if [ "$type" == "client" ]; then
            test_api "[Cleanup] Deactivate client $id" "PUT" "/admin/clients/$id/status" '{"status":"INACTIVE"}' "$ADMIN_TOKEN" 200 || \
            test_api "[Cleanup] Deactivate client $id (already gone)" "PUT" "/admin/clients/$id/status" '{"status":"INACTIVE"}' "$ADMIN_TOKEN" 404
        fi
    done
}

# ==============================================================================
# Main Execution
# ==============================================================================

# Ensure cleanup runs on script exit
trap run_cleanup EXIT

# Check for required tools
if ! command -v jq &> /dev/null || ! command -v curl &> /dev/null; then
    echo "Error: 'jq' and 'curl' are required to run this script." >&2
    exit 1
fi

print_header "INITIALIZING: Minibank API Test Suite"

# Health Checks
wait_for_service "$KEYCLOAK_URL/health/ready" "Keycloak"
wait_for_service "$MINIBANK_URL/actuator/health" "Minibank API"

# Run setup and tests
run_setup
run_tests

# Final Summary
print_header "TEST SUITE SUMMARY"
if [ "$TESTS_RUN" -eq "$TESTS_PASSED" ]; then
    echo -e "${GREEN}All $TESTS_PASSED tests passed successfully! 🎉${NC}"
    exit 0
else
    local failed=$((TESTS_RUN - TESTS_PASSED))
    echo -e "${RED}Test suite finished with $failed failure(s).${NC}"
    echo -e "  - ${GREEN}Passed: $TESTS_PASSED${NC}"
    echo -e "  - ${RED}Failed: $failed${NC}"
    echo -e "  - ${BLUE}Total:  $TESTS_RUN${NC}"
    exit 1
fi
