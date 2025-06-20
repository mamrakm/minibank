package cz.ememsoft.minibank.integration

import cz.ememsoft.minibank.TestBase
import cz.ememsoft.minibank.api.transaction.request.TransactionRequestDto
import cz.ememsoft.minibank.enumeration.TransactionStatusEnum
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.ClientRepository
import cz.ememsoft.minibank.repository.TransactionRepository
import cz.ememsoft.minibank.service.TransactionService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.security.test.context.support.WithMockUser
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@WithMockUser // Provide a mock user for the security context
class TransactionServiceIntegrationTest : TestBase() {

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

    private var sourceClientId: Long = 0
    private var targetClientId: Long = 0
    private var sourceAccountId: Long = 0
    private var targetAccountId: Long = 0

    @BeforeEach
    fun setUp() {
        transactionRepository.deleteAll().block()
        accountRepository.deleteAll().block()
        clientRepository.deleteAll().block()
        setupTestData()
    }

    private fun setupTestData() {
        val sourceKeycloakUserId = "test-source-keycloak-${UUID.randomUUID()}"
        sourceClientId = databaseClient.sql(
            """
            INSERT INTO bank.client (keycloak_user_id, first_name, last_name, email, role, phone_number, address, date_of_birth, status, created_at)
            VALUES (:keycloakUserId, :firstName, :lastName, :email, :role, :phoneNumber, :address, :dateOfBirth, :status, :createdAt)
            RETURNING id
        """
        )
            .bind("keycloakUserId", sourceKeycloakUserId)
            .bind("firstName", "John")
            .bind("lastName", "Doe")
            .bind("email", "john.doe@example.com")
            .bind("role", 0)
            .bind("phoneNumber", "+1234567890")
            .bind("address", "123 Main St")
            .bind("dateOfBirth", LocalDate.of(1990, 1, 1))
            .bind("status", 0)
            .bind("createdAt", java.time.LocalDateTime.now())
            .map { row -> row.get("id", Long::class.javaObjectType)!! }
            .one()
            .block()!!

        val targetKeycloakUserId = "test-target-keycloak-${UUID.randomUUID()}"
        targetClientId = databaseClient.sql(
            """
            INSERT INTO bank.client (keycloak_user_id, first_name, last_name, email, role, phone_number, address, date_of_birth, status, created_at)
            VALUES (:keycloakUserId, :firstName, :lastName, :email, :role, :phoneNumber, :address, :dateOfBirth, :status, :createdAt)
            RETURNING id
        """
        )
            .bind("keycloakUserId", targetKeycloakUserId)
            .bind("firstName", "Jane")
            .bind("lastName", "Smith")
            .bind("email", "jane.smith@example.com")
            .bind("role", 0)
            .bind("phoneNumber", "+0987654321")
            .bind("address", "456 Oak Ave")
            .bind("dateOfBirth", LocalDate.of(1985, 5, 15))
            .bind("status", 0)
            .bind("createdAt", java.time.LocalDateTime.now())
            .map { row -> row.get("id", Long::class.javaObjectType)!! }
            .one()
            .block()!!

        sourceAccountId = databaseClient.sql(
            "INSERT INTO bank.account (account_name, client_id, balance, account_type, currency) VALUES ('Source', :c_id, 1000.00, 0, 0) RETURNING id"
        ).bind("c_id", sourceClientId).map { row -> row.get("id", Long::class.javaObjectType)!! }.one().block()!!

        targetAccountId = databaseClient.sql(
            "INSERT INTO bank.account (account_name, client_id, balance, account_type, currency) VALUES ('Target', :c_id, 500.00, 2, 0) RETURNING id"
        ).bind("c_id", targetClientId).map { row -> row.get("id", Long::class.javaObjectType)!! }.one().block()!!
    }

    @Test
    fun `processTransaction should successfully transfer money between accounts`() {
        val transactionRequest = TransactionRequestDto(sourceAccountId, targetAccountId, BigDecimal("100.50"), "USD", "Integration test transfer")
        StepVerifier.create(transactionService.processTransaction(transactionRequest))
            .assertNext { response ->
                assertThat(response.status).isEqualTo(TransactionStatusEnum.COMPLETED)
            }
            .verifyComplete()
    }
}
