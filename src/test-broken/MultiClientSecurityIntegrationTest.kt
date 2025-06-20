package cz.ememsoft.minibank.integration

import cz.ememsoft.minibank.TestBase
import cz.ememsoft.minibank.api.transaction.request.TransactionRequestDto
import cz.ememsoft.minibank.dto.auth.LoginRequestDto
import cz.ememsoft.minibank.dto.auth.RegisterRequestDto
import cz.ememsoft.minibank.enumeration.UserRoleEnum
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.ClientRepository
import cz.ememsoft.minibank.repository.TransactionRepository
import cz.ememsoft.minibank.service.AuthenticationService
import cz.ememsoft.minibank.service.TransactionService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Integration tests for multi-client scenarios with authentication and authorization.
 * Tests concurrent operations by multiple authenticated users accessing their own data.
 */
class MultiClientSecurityIntegrationTest : TestBase() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var authenticationService: AuthenticationService

    @Autowired
    private lateinit var transactionService: TransactionService

    @Autowired
    private lateinit var clientRepository: ClientRepository

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @Autowired
    private lateinit var databaseClient: DatabaseClient

    // Test user credentials and IDs
    private lateinit var client1Token: String
    private lateinit var client2Token: String
    private lateinit var adminToken: String
    
    private var client1Id: Long = 0
    private var client2Id: Long = 0
    private var adminId: Long = 0
    
    private var client1AccountId: Long = 0
    private var client2AccountId: Long = 0

    @BeforeEach
    fun setUp() {
        // Enable security for this test
        enableSecurity()
        
        // Clean up database
        cleanupDatabase()
        
        // Create test users and get their tokens
        setupTestUsers()
        
        // Create accounts for each client
        setupAccounts()
    }

    private fun cleanupDatabase() {
        transactionRepository.deleteAll().block()
        accountRepository.deleteAll().block()
        clientRepository.deleteAll().block()
    }

    private fun setupTestUsers() {
        // Create Client 1
        val client1RegisterRequest = RegisterRequestDto(
            firstName = "Alice",
            lastName = "Johnson",
            email = "alice@test.com",
            password = "password123",
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+1111111111",
            address = "111 Alice St",
            dateOfBirth = LocalDate.of(1990, 1, 1)
        )

        val client1Response = authenticationService.register(client1RegisterRequest).block()!!
        client1Token = client1Response.token
        client1Id = client1Response.clientId

        // Create Client 2
        val client2RegisterRequest = RegisterRequestDto(
            firstName = "Bob",
            lastName = "Smith",
            email = "bob@test.com",
            password = "password456",
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+2222222222",
            address = "222 Bob Ave",
            dateOfBirth = LocalDate.of(1985, 5, 15)
        )

        val client2Response = authenticationService.register(client2RegisterRequest).block()!!
        client2Token = client2Response.token
        client2Id = client2Response.clientId

        // Create Admin User
        val adminRegisterRequest = RegisterRequestDto(
            firstName = "Admin",
            lastName = "User",
            email = "admin@test.com",
            password = "admin123",
            role = UserRoleEnum.ADMIN,
            phoneNumber = "+9999999999",
            address = "999 Admin Blvd",
            dateOfBirth = LocalDate.of(1980, 12, 31)
        )

        val adminResponse = authenticationService.register(adminRegisterRequest).block()!!
        adminToken = adminResponse.token
        adminId = adminResponse.clientId
    }

    private fun setupAccounts() {
        // Create account for Client 1
        client1AccountId = databaseClient.sql(
            """
            INSERT INTO bank.account (account_name, client_id, balance, account_type, currency, status)
            VALUES (:accountName, :clientId, :balance, :accountType, :currency, :status)
            RETURNING id
            """
        )
            .bind("accountName", "Alice's Checking")
            .bind("clientId", client1Id)
            .bind("balance", BigDecimal("1000.0000"))
            .bind("accountType", 0) // CHECKING
            .bind("currency", 0) // USD
            .bind("status", 0) // ACTIVE
            .map { row -> row.get("id", Long::class.javaObjectType) }
            .one()
            .block() ?: error("Failed to create account for client 1")

        // Create account for Client 2
        client2AccountId = databaseClient.sql(
            """
            INSERT INTO bank.account (account_name, client_id, balance, account_type, currency, status)
            VALUES (:accountName, :clientId, :balance, :accountType, :currency, :status)
            RETURNING id
            """
        )
            .bind("accountName", "Bob's Savings")
            .bind("clientId", client2Id)
            .bind("balance", BigDecimal("500.0000"))
            .bind("accountType", 2) // SAVINGS
            .bind("currency", 0) // USD
            .bind("status", 0) // ACTIVE
            .map { row -> row.get("id", Long::class.javaObjectType) }
            .one()
            .block() ?: error("Failed to create account for client 2")
    }

    // ==================== Authentication Tests ====================

    @Test
    fun `multiple clients can login and access their own data simultaneously`() {
        // Test Client 1 login
        val client1LoginRequest = LoginRequestDto(
            email = "alice@test.com",
            password = "password123"
        )

        val client1LoginResponse = authenticationService.login(client1LoginRequest).block()!!
        assertThat(client1LoginResponse.clientId).isEqualTo(client1Id)
        assertThat(client1LoginResponse.role).isEqualTo(UserRoleEnum.CLIENT)

        // Test Client 2 login
        val client2LoginRequest = LoginRequestDto(
            email = "bob@test.com",
            password = "password456"
        )

        val client2LoginResponse = authenticationService.login(client2LoginRequest).block()!!
        assertThat(client2LoginResponse.clientId).isEqualTo(client2Id)
        assertThat(client2LoginResponse.role).isEqualTo(UserRoleEnum.CLIENT)

        // Verify tokens are different
        assertThat(client1LoginResponse.token).isNotEqualTo(client2LoginResponse.token)
    }

    @Test
    fun `client can only access their own profile data`() {
        // Client 1 accessing their own data - should succeed
        webTestClient.get()
            .uri("/clients/{id}", client1Id)
            .header("Authorization", "Bearer $client1Token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(client1Id)
            .jsonPath("$.email").isEqualTo("alice@test.com")

        // Client 1 trying to access Client 2's data - should fail
        webTestClient.get()
            .uri("/clients/{id}", client2Id)
            .header("Authorization", "Bearer $client1Token")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `admin can access all client data`() {
        // Admin accessing Client 1's data - should succeed
        webTestClient.get()
            .uri("/clients/{id}", client1Id)
            .header("Authorization", "Bearer $adminToken")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(client1Id)

        // Admin accessing Client 2's data - should succeed
        webTestClient.get()
            .uri("/clients/{id}", client2Id)
            .header("Authorization", "Bearer $adminToken")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(client2Id)
    }

    // ==================== Account Access Tests ====================

    @Test
    fun `clients can only access their own accounts`() {
        // Client 1 accessing their own account - should succeed
        webTestClient.get()
            .uri("/accounts/{id}", client1AccountId)
            .header("Authorization", "Bearer $client1Token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.clientId").isEqualTo(client1Id)

        // Client 1 trying to access Client 2's account - should fail
        webTestClient.get()
            .uri("/accounts/{id}", client2AccountId)
            .header("Authorization", "Bearer $client1Token")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `getAllClients returns only own data for regular clients but all data for admin`() {
        // Regular client should only see their own data
        webTestClient.get()
            .uri("/clients")
            .header("Authorization", "Bearer $client1Token")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(1) // Only own client data

        // Admin should see all clients
        webTestClient.get()
            .uri("/clients")
            .header("Authorization", "Bearer $adminToken")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(3) // All clients including admin
    }

    // ==================== Transaction Security Tests ====================

    @Test
    fun `clients can only initiate transactions from their own accounts`() {
        // Valid transaction from Client 1's account to Client 2's account
        val validTransaction = TransactionRequestDto(
            sourceAccountId = client1AccountId,
            targetAccountId = client2AccountId,
            amount = BigDecimal("100.00"),
            currency = "USD",
            reference = "Valid inter-client transfer"
        )

        webTestClient.post()
            .uri("/transactions")
            .header("Authorization", "Bearer $client1Token")
            .header("Content-Type", "application/json")
            .bodyValue(validTransaction)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.sourceAccountId").isEqualTo(client1AccountId)
            .jsonPath("$.amount").isEqualTo("100.0000")

        // Invalid transaction - Client 1 trying to initiate from Client 2's account
        val invalidTransaction = TransactionRequestDto(
            sourceAccountId = client2AccountId, // Not Client 1's account!
            targetAccountId = client1AccountId,
            amount = BigDecimal("50.00"),
            currency = "USD",
            reference = "Invalid transaction attempt"
        )

        webTestClient.post()
            .uri("/transactions")
            .header("Authorization", "Bearer $client1Token")
            .header("Content-Type", "application/json")
            .bodyValue(invalidTransaction)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `clients can only view transactions involving their own accounts`() {
        // Create a transaction between the two clients
        val transaction = TransactionRequestDto(
            sourceAccountId = client1AccountId,
            targetAccountId = client2AccountId,
            amount = BigDecimal("75.00"),
            currency = "USD",
            reference = "Test transaction for viewing"
        )

        val transactionResponse = transactionService.processTransaction(transaction).block()!!

        // Client 1 should be able to view this transaction (involves their account)
        webTestClient.get()
            .uri("/transactions/{id}", transactionResponse.id)
            .header("Authorization", "Bearer $client1Token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(transactionResponse.id)

        // Client 2 should also be able to view this transaction (involves their account)
        webTestClient.get()
            .uri("/transactions/{id}", transactionResponse.id)
            .header("Authorization", "Bearer $client2Token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(transactionResponse.id)
    }

    // ==================== Concurrent Operations Tests ====================

    @Test
    fun `multiple clients can perform concurrent transactions safely`() {
        // Create multiple transactions simultaneously
        val transaction1 = TransactionRequestDto(
            sourceAccountId = client1AccountId,
            targetAccountId = client2AccountId,
            amount = BigDecimal("50.00"),
            currency = "USD",
            reference = "Concurrent transaction 1"
        )

        val transaction2 = TransactionRequestDto(
            sourceAccountId = client2AccountId,
            targetAccountId = client1AccountId,
            amount = BigDecimal("25.00"),
            currency = "USD",
            reference = "Concurrent transaction 2"
        )

        // Execute transactions concurrently using reactive streams
        val result1 = transactionService.processTransaction(transaction1)
        val result2 = transactionService.processTransaction(transaction2)

        // Verify both transactions complete successfully
        StepVerifier.create(result1)
            .assertNext { response ->
                assertThat(response.sourceAccountId).isEqualTo(client1AccountId)
                assertThat(response.amount).isEqualByComparingTo(BigDecimal("50.0000"))
            }
            .verifyComplete()

        StepVerifier.create(result2)
            .assertNext { response ->
                assertThat(response.sourceAccountId).isEqualTo(client2AccountId)
                assertThat(response.amount).isEqualByComparingTo(BigDecimal("25.0000"))
            }
            .verifyComplete()

        // Verify final balances are correct
        // Client 1: 1000 - 50 + 25 = 975
        // Client 2: 500 + 50 - 25 = 525
        StepVerifier.create(accountRepository.findById(client1AccountId))
            .assertNext { account ->
                assertThat(account.balance).isEqualByComparingTo(BigDecimal("975.0000"))
            }
            .verifyComplete()

        StepVerifier.create(accountRepository.findById(client2AccountId))
            .assertNext { account ->
                assertThat(account.balance).isEqualByComparingTo(BigDecimal("525.0000"))
            }
            .verifyComplete()
    }

    // ==================== Admin Access Tests ====================

    @Test
    fun `admin can access admin endpoints but regular clients cannot`() {
        // Admin accessing admin endpoint - should succeed
        webTestClient.get()
            .uri("/admin/clients")
            .header("Authorization", "Bearer $adminToken")
            .exchange()
            .expectStatus().isOk

        // Regular client trying to access admin endpoint - should fail
        webTestClient.get()
            .uri("/admin/clients")
            .header("Authorization", "Bearer $client1Token")
            .exchange()
            .expectStatus().isForbidden

        webTestClient.get()
            .uri("/admin/clients")
            .header("Authorization", "Bearer $client2Token")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `unauthenticated requests should be rejected for protected endpoints`() {
        // Try to access protected endpoint without token
        webTestClient.get()
            .uri("/clients/{id}", client1Id)
            .exchange()
            .expectStatus().isUnauthorized

        // Try to access admin endpoint without token
        webTestClient.get()
            .uri("/admin/clients")
            .exchange()
            .expectStatus().isUnauthorized

        // Try to make transaction without token
        val transaction = TransactionRequestDto(
            sourceAccountId = client1AccountId,
            targetAccountId = client2AccountId,
            amount = BigDecimal("100.00"),
            currency = "USD",
            reference = "Unauthorized transaction attempt"
        )

        webTestClient.post()
            .uri("/transactions")
            .header("Content-Type", "application/json")
            .bodyValue(transaction)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `invalid or expired tokens should be rejected`() {
        val invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.token"

        webTestClient.get()
            .uri("/clients/{id}", client1Id)
            .header("Authorization", "Bearer $invalidToken")
            .exchange()
            .expectStatus().isUnauthorized
    }
}