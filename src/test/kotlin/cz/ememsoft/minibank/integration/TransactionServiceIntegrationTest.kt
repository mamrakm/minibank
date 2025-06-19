package cz.ememsoft.minibank.integration

import cz.ememsoft.minibank.TestBase
import cz.ememsoft.minibank.api.transaction.request.TransactionRequestDto
import cz.ememsoft.minibank.enumeration.TransactionStatusEnum
import cz.ememsoft.minibank.exception.AccountNotFoundException
import cz.ememsoft.minibank.exception.TransactionNotFoundException
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.ClientRepository
import cz.ememsoft.minibank.repository.TransactionRepository
import cz.ememsoft.minibank.service.TransactionService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

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
        // Clean up a database before each test
        cleanupDatabase()
        setupTestData()
    }

    private fun cleanupDatabase() {
        // Delete it in the correct order due to foreign key constraints
        transactionRepository.deleteAll().block()
        accountRepository.deleteAll().block()
        clientRepository.deleteAll().block()
    }

    private fun setupTestData() {
        // Create a source client using raw SQL to avoid entity mapping issues
        val sourcePersonalNumber = UUID.randomUUID()
        sourceClientId = databaseClient.sql(
            """
            INSERT INTO bank.client (first_name, last_name, email, phone_number, address, date_of_birth, personal_number)
            VALUES (:firstName, :lastName, :email, :phoneNumber, :address, :dateOfBirth, :personalNumber)
            RETURNING id
        """
        )
            .bind("firstName", "John")
            .bind("lastName", "Doe")
            .bind("email", "john.doe@example.com")
            .bind("phoneNumber", "+1234567890")
            .bind("address", "123 Main St")
            .bind("dateOfBirth", LocalDate.of(1990, 1, 1))
            .bind("personalNumber", sourcePersonalNumber)
            .map { row -> row.get("id", Long::class.javaObjectType) }
            .one()
            .block() ?: error("Failed to retrieve source client ID or ID was null after insert")

        // Create a target client using raw SQL
        val targetPersonalNumber = UUID.randomUUID()
        targetClientId = databaseClient.sql(
            """
            INSERT INTO bank.client (first_name, last_name, email, phone_number, address, date_of_birth, personal_number)
            VALUES (:firstName, :lastName, :email, :phoneNumber, :address, :dateOfBirth, :personalNumber)
            RETURNING id
        """
        )
            .bind("firstName", "Jane")
            .bind("lastName", "Smith")
            .bind("email", "jane.smith@example.com")
            .bind("phoneNumber", "+0987654321")
            .bind("address", "456 Oak Ave")
            .bind("dateOfBirth", LocalDate.of(1985, 5, 15))
            .bind("personalNumber", targetPersonalNumber)
            .map { row -> row.get("id", Long::class.javaObjectType) }
            .one()
            .block() ?: error("Failed to retrieve target client ID or ID was null after insert")

        // Create a source account using raw SQL with ordinal values
        sourceAccountId = databaseClient.sql(
            """
            INSERT INTO bank.account (account_name, client_id, balance, account_type, currency)
            VALUES (:accountName, :clientId, :balance, :accountType, :currency)
            RETURNING id
        """
        )
            .bind("accountName", "John's Checking Account")
            .bind("clientId", sourceClientId)
            .bind("balance", BigDecimal("1000.0000"))
            .bind("accountType", 0) // CHECKING ordinal
            .bind("currency", 0) // USD ordinal
            .map { row -> row.get("id", Long::class.javaObjectType) }
            .one()
            .block() ?: error("Failed to retrieve source account ID or ID was null after insert")

        // Create a target account using raw SQL with ordinal values
        targetAccountId = databaseClient.sql(
            """
            INSERT INTO bank.account (account_name, client_id, balance, account_type, currency)
            VALUES (:accountName, :clientId, :balance, :accountType, :currency)
            RETURNING id
        """
        )
            .bind("accountName", "Jane's Savings Account")
            .bind("clientId", targetClientId)
            .bind("balance", BigDecimal("500.0000"))
            .bind("accountType", 2) // SAVINGS ordinal
            .bind("currency", 0) // USD ordinal
            .map { row -> row.get("id", Long::class.javaObjectType) }
            .one()
            .block() ?: error("Failed to retrieve target account ID or ID was null after insert")
    }

    @Test
    fun `processTransaction should successfully transfer money between accounts`() {
        // Arrange
        val transactionRequest = TransactionRequestDto(
            sourceAccountId = sourceAccountId,
            targetAccountId = targetAccountId,
            amount = BigDecimal("100.50"),
            currency = "USD",
            reference = "Integration test transfer"
        )

        // Act & Assert
        StepVerifier.create(transactionService.processTransaction(transactionRequest))
            .assertNext { response ->
                assertThat(response.sourceAccountId).isEqualTo(sourceAccountId)
                assertThat(response.targetAccountId).isEqualTo(targetAccountId)
                assertThat(response.amount).isEqualByComparingTo(BigDecimal("100.5000"))
                assertThat(response.currency).isEqualTo("USD")
                assertThat(response.status).isEqualTo(TransactionStatusEnum.COMPLETED)
                assertThat(response.reference).isEqualTo("Integration test transfer")
                assertThat(response.id).isNotNull()
                assertThat(response.timestamp).isNotNull()
            }
            .verifyComplete()

        // Verify account balances were updated correctly
        StepVerifier.create(accountRepository.findById(sourceAccountId))
            .assertNext { account ->
                assertThat(account.balance).isEqualByComparingTo(BigDecimal("899.5000"))
            }
            .verifyComplete()

        StepVerifier.create(accountRepository.findById(targetAccountId))
            .assertNext { account ->
                assertThat(account.balance).isEqualByComparingTo(BigDecimal("600.5000"))
            }
            .verifyComplete()
    }

    @Test
    fun `processTransaction should fail when source account does not exist`() {
        // Arrange
        val transactionRequest = TransactionRequestDto(
            sourceAccountId = 99999L,
            targetAccountId = targetAccountId,
            amount = BigDecimal("100.00"),
            currency = "USD",
            reference = "Test transfer"
        )

        // Act & Assert
        StepVerifier.create(transactionService.processTransaction(transactionRequest))
            .expectErrorMatches { throwable ->
                throwable is AccountNotFoundException &&
                        (throwable.message?.contains("Source account 99999 not found") ?: false)
            }
            .verify()
    }

    @Test
    fun `processTransaction should fail when target account does not exist`() {
        // Arrange
        val transactionRequest = TransactionRequestDto(
            sourceAccountId = sourceAccountId,
            targetAccountId = 99999L,
            amount = BigDecimal("100.00"),
            currency = "USD",
            reference = "Test transfer"
        )

        // Act & Assert
        StepVerifier.create(transactionService.processTransaction(transactionRequest))
            .expectErrorMatches { throwable ->
                throwable is AccountNotFoundException &&
                        (throwable.message?.contains("Target account 99999 not found") ?: false)
            }
            .verify()
    }

    @Test
    fun `getTransactionById should return transaction when it exists`() {
        // Arrange - first create a transaction
        val transactionRequest = TransactionRequestDto(
            sourceAccountId = sourceAccountId,
            targetAccountId = targetAccountId,
            amount = BigDecimal("75.25"),
            currency = "USD",
            reference = "Test transaction for retrieval"
        )

        val createdTransactionId = transactionService.processTransaction(transactionRequest)
            .map { it.id }
            .block() ?: error("Created transaction ID was null after processing")

        // Act & Assert
        StepVerifier.create(transactionService.getTransactionById(createdTransactionId))
            .assertNext { response ->
                assertThat(response.id).isEqualTo(createdTransactionId)
                assertThat(response.sourceAccountId).isEqualTo(sourceAccountId)
                assertThat(response.targetAccountId).isEqualTo(targetAccountId)
                assertThat(response.amount).isEqualByComparingTo(BigDecimal("75.2500"))
                assertThat(response.currency).isEqualTo("USD")
                assertThat(response.reference).isEqualTo("Test transaction for retrieval")
                assertThat(response.status).isEqualTo(TransactionStatusEnum.COMPLETED)
            }
            .verifyComplete()
    }

    @Test
    fun `getTransactionById should fail when transaction does not exist`() {
        // Act & Assert
        StepVerifier.create(transactionService.getTransactionById(99999L))
            .expectErrorMatches { throwable ->
                throwable is TransactionNotFoundException &&
                        (throwable.message?.contains("Transaction with ID 99999 not found") ?: false)
            }
            .verify()
    }

    @Test
    fun `getTransactionsByAccountId should return all transactions for account`() {
        // Arrange - create multiple transactions involving the source account
        val outgoingTransaction = TransactionRequestDto(
            sourceAccountId = sourceAccountId,
            targetAccountId = targetAccountId,
            amount = BigDecimal("50.00"),
            currency = "USD",
            reference = "Outgoing transaction"
        )

        val incomingTransaction = TransactionRequestDto(
            sourceAccountId = targetAccountId,
            targetAccountId = sourceAccountId,
            amount = BigDecimal("25.00"),
            currency = "USD",
            reference = "Incoming transaction"
        )

        // Process both transactions
        transactionService.processTransaction(outgoingTransaction).block()
        transactionService.processTransaction(incomingTransaction).block()

        // Act & Assert - check all transactions for a source account
        StepVerifier.create(transactionService.getTransactionsByAccountId(sourceAccountId))
            .expectNextCount(2) // Should have 2 transactions (1 outgoing, 1 incoming)
            .verifyComplete()
    }

    @Test
    fun `getOutgoingTransactions should return only outgoing transactions`() {
        // Arrange - create transactions in both directions
        val outgoingTransaction = TransactionRequestDto(
            sourceAccountId = sourceAccountId,
            targetAccountId = targetAccountId,
            amount = BigDecimal("60.00"),
            currency = "USD",
            reference = "Outgoing only"
        )

        val incomingTransaction = TransactionRequestDto(
            sourceAccountId = targetAccountId,
            targetAccountId = sourceAccountId,
            amount = BigDecimal("30.00"),
            currency = "USD",
            reference = "Incoming only"
        )

        // Process both transactions
        transactionService.processTransaction(outgoingTransaction).block()
        transactionService.processTransaction(incomingTransaction).block()

        // Act & Assert - check only outgoing transactions from source account
        StepVerifier.create(transactionService.getOutgoingTransactions(sourceAccountId))
            .assertNext { response ->
                assertThat(response.sourceAccountId).isEqualTo(sourceAccountId)
                assertThat(response.targetAccountId).isEqualTo(targetAccountId)
                assertThat(response.amount).isEqualByComparingTo(BigDecimal("60.0000"))
                assertThat(response.reference).isEqualTo("Outgoing only")
            }
            .verifyComplete()
    }

    @Test
    fun `getIncomingTransactions should return only incoming transactions`() {
        // Arrange - create transactions in both directions
        val outgoingTransaction = TransactionRequestDto(
            sourceAccountId = sourceAccountId,
            targetAccountId = targetAccountId,
            amount = BigDecimal("80.00"),
            currency = "USD",
            reference = "Outgoing test"
        )

        val incomingTransaction = TransactionRequestDto(
            sourceAccountId = targetAccountId,
            targetAccountId = sourceAccountId,
            amount = BigDecimal("40.00"),
            currency = "USD",
            reference = "Incoming test"
        )

        // Process both transactions
        transactionService.processTransaction(outgoingTransaction).block()
        transactionService.processTransaction(incomingTransaction).block()

        // Act & Assert - check only incoming transactions to source account
        StepVerifier.create(transactionService.getIncomingTransactions(sourceAccountId))
            .assertNext { response ->
                assertThat(response.sourceAccountId).isEqualTo(targetAccountId)
                assertThat(response.targetAccountId).isEqualTo(sourceAccountId)
                assertThat(response.amount).isEqualByComparingTo(BigDecimal("40.0000"))
                assertThat(response.reference).isEqualTo("Incoming test")
            }
            .verifyComplete()
    }

    @Test
    fun `processTransaction should normalize currency to uppercase`() {
        // Arrange - use lowercase currency
        val transactionRequest = TransactionRequestDto(
            sourceAccountId = sourceAccountId,
            targetAccountId = targetAccountId,
            amount = BigDecimal("45.75"),
            currency = "usd", // lowercase
            reference = "Currency normalization test"
        )

        // Act & Assert - should normalize to uppercase
        StepVerifier.create(transactionService.processTransaction(transactionRequest))
            .assertNext { response ->
                assertThat(response.currency).isEqualTo("USD") // normalized to uppercase
                assertThat(response.amount).isEqualByComparingTo(BigDecimal("45.7500"))
            }
            .verifyComplete()
    }

    @Test
    fun `multiple sequential transactions should update balances correctly`() {
        // Arrange - create multiple sequential transactions
        val transaction1 = TransactionRequestDto(
            sourceAccountId = sourceAccountId,
            targetAccountId = targetAccountId,
            amount = BigDecimal("100.00"),
            currency = "USD",
            reference = "First transaction"
        )

        val transaction2 = TransactionRequestDto(
            sourceAccountId = sourceAccountId,
            targetAccountId = targetAccountId,
            amount = BigDecimal("200.00"),
            currency = "USD",
            reference = "Second transaction"
        )

        val transaction3 = TransactionRequestDto(
            sourceAccountId = targetAccountId,
            targetAccountId = sourceAccountId,
            amount = BigDecimal("50.00"),
            currency = "USD",
            reference = "Return transaction"
        )

        // Act - process transactions sequentially
        transactionService.processTransaction(transaction1).block()
        transactionService.processTransaction(transaction2).block()
        transactionService.processTransaction(transaction3).block()

        // Assert - verify final balances
        // Source: 1000 - 100 - 200 + 50 = 750
        // Target: 500 + 100 + 200 - 50 = 750
        StepVerifier.create(accountRepository.findById(sourceAccountId))
            .assertNext { account ->
                assertThat(account.balance).isEqualByComparingTo(BigDecimal("750.0000"))
            }
            .verifyComplete()

        StepVerifier.create(accountRepository.findById(targetAccountId))
            .assertNext { account ->
                assertThat(account.balance).isEqualByComparingTo(BigDecimal("750.0000"))
            }
            .verifyComplete()

        // Verify transaction count
        StepVerifier.create(transactionService.getTransactionsByAccountId(sourceAccountId))
            .expectNextCount(3) // Should have 3 transactions
            .verifyComplete()
    }

    @Test
    fun `getTransactionsByAccountId should fail when account does not exist`() {
        // Act & Assert
        StepVerifier.create(transactionService.getTransactionsByAccountId(99999L))
            .expectErrorMatches { throwable ->
                throwable is AccountNotFoundException &&
                        (throwable.message?.contains("Account with ID 99999 not found") ?: false)
            }
            .verify()
    }

    @Test
    fun `getOutgoingTransactions should fail when account does not exist`() {
        // Act & Assert
        StepVerifier.create(transactionService.getOutgoingTransactions(99999L))
            .expectErrorMatches { throwable ->
                throwable is AccountNotFoundException &&
                        (throwable.message?.contains("Account with ID 99999 not found") ?: false)
            }
            .verify()
    }

    @Test
    fun `getIncomingTransactions should fail when account does not exist`() {
        // Act & Assert
        StepVerifier.create(transactionService.getIncomingTransactions(99999L))
            .expectErrorMatches { throwable ->
                throwable is AccountNotFoundException &&
                        (throwable.message?.contains("Account with ID 99999 not found") ?: false)
            }
            .verify()
    }
}