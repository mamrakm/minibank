package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.transaction.request.TransactionRequestDto
import cz.ememsoft.minibank.api.transaction.response.TransactionResponseDto
import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.entity.TransactionEntity
import cz.ememsoft.minibank.enumeration.AccountStatusEnum
import cz.ememsoft.minibank.enumeration.AccountTypeEnum
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import cz.ememsoft.minibank.enumeration.TransactionStatusEnum
import cz.ememsoft.minibank.exception.AccountNotFoundException
import cz.ememsoft.minibank.exception.TransactionNotFoundException
import cz.ememsoft.minibank.mapper.TransactionMapper
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.TransactionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDateTime

class TransactionServiceTest {

    private val accountRepository: AccountRepository = mock()
    private val transactionRepository: TransactionRepository = mock()
    private val transactionProcessor: TransactionProcessor = mock()
    private val transactionMapper: TransactionMapper = mock()
    private lateinit var transactionService: TransactionService

    private val sourceAccount = AccountEntity(
        id = 1L,
        name = "Source Account",
        clientId = 100L,
        balance = BigDecimal("1000.0000"),
        accountTypeOrdinal = AccountTypeEnum.CHECKING.ordinal,
        currencyOrdinal = CurrencyEnum.USD.ordinal,
        statusOrdinal = AccountStatusEnum.ACTIVE.ordinal
    )

    private val targetAccount = AccountEntity(
        id = 2L,
        name = "Target Account",
        clientId = 200L,
        balance = BigDecimal("500.0000"),
        accountTypeOrdinal = AccountTypeEnum.SAVINGS.ordinal,
        currencyOrdinal = CurrencyEnum.USD.ordinal,
        statusOrdinal = AccountStatusEnum.ACTIVE.ordinal
    )

    private val transactionRequest = TransactionRequestDto(
        sourceAccountId = 1L,
        targetAccountId = 2L,
        amount = BigDecimal("100.00"),
        currency = "USD",
        reference = "Test transaction"
    )

    private val pendingTransaction = TransactionEntity.create(
        id = 1L,
        sourceAccountId = 1L,
        targetAccountId = 2L,
        amount = BigDecimal("100.0000"),
        currency = CurrencyEnum.USD,
        timestamp = LocalDateTime.now(),
        status = TransactionStatusEnum.PENDING,
        reference = "Test transaction"
    )

    private val completedTransaction = pendingTransaction.withStatus(TransactionStatusEnum.COMPLETED)

    private val transactionResponse = TransactionResponseDto(
        id = 1L,
        sourceAccountId = 1L,
        targetAccountId = 2L,
        amount = BigDecimal("100.0000"),
        currency = "USD",
        timestamp = LocalDateTime.now(),
        status = TransactionStatusEnum.COMPLETED,
        reference = "Test transaction"
    )

    @BeforeEach
    fun setup() {
        transactionService = TransactionServiceImpl(
            accountRepository,
            transactionRepository,
            transactionProcessor,
            transactionMapper
        )
    }

    @Test
    fun `processTransaction should successfully process valid transaction`() {
        // Given
        whenever(accountRepository.findById(1L)).thenReturn(Mono.just(sourceAccount))
        whenever(accountRepository.findById(2L)).thenReturn(Mono.just(targetAccount))
        whenever(transactionProcessor.validateTransaction(eq(transactionRequest), eq(sourceAccount), eq(targetAccount)))
            .thenReturn(Mono.empty())
        whenever(transactionProcessor.createPendingTransaction(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(pendingTransaction))
        whenever(transactionProcessor.updateAccountBalances(eq(sourceAccount), eq(targetAccount), any()))
            .thenReturn(Mono.empty())
        whenever(transactionProcessor.completeTransaction(pendingTransaction))
            .thenReturn(Mono.just(completedTransaction))
        whenever(transactionMapper.entityToResponseDto(completedTransaction))
            .thenReturn(transactionResponse)

        // When & Then
        StepVerifier.create(transactionService.processTransaction(transactionRequest))
            .expectNext(transactionResponse)
            .verifyComplete()

        verify(accountRepository).findById(1L)
        verify(accountRepository).findById(2L)
        verify(transactionProcessor).validateTransaction(eq(transactionRequest), eq(sourceAccount), eq(targetAccount))
        verify(transactionProcessor).createPendingTransaction(any(), any(), any(), any(), any(), any())
        verify(transactionProcessor).updateAccountBalances(eq(sourceAccount), eq(targetAccount), any())
        verify(transactionProcessor).completeTransaction(pendingTransaction)
        verify(transactionMapper).entityToResponseDto(completedTransaction)
    }

    @Test
    fun `processTransaction should fail when source account not found`() {
        // Given
        whenever(accountRepository.findById(1L)).thenReturn(Mono.empty())
        whenever(accountRepository.findById(2L)).thenReturn(Mono.just(targetAccount))

        // When & Then
        StepVerifier.create(transactionService.processTransaction(transactionRequest))
            .expectError(AccountNotFoundException::class.java)
            .verify()

        verify(accountRepository).findById(1L)
        verify(accountRepository).findById(2L)
    }

    @Test
    fun `processTransaction should fail when target account not found`() {
        // Given
        whenever(accountRepository.findById(1L)).thenReturn(Mono.just(sourceAccount))
        whenever(accountRepository.findById(2L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(transactionService.processTransaction(transactionRequest))
            .expectError(AccountNotFoundException::class.java)
            .verify()

        verify(accountRepository).findById(1L)
        verify(accountRepository).findById(2L)
    }

    @Test
    fun `processTransaction should handle validation failure`() {
        // Given
        val validationError = RuntimeException("Insufficient funds")
        whenever(accountRepository.findById(1L)).thenReturn(Mono.just(sourceAccount))
        whenever(accountRepository.findById(2L)).thenReturn(Mono.just(targetAccount))
        whenever(transactionProcessor.validateTransaction(eq(transactionRequest), eq(sourceAccount), eq(targetAccount)))
            .thenReturn(Mono.error(validationError))

        // When & Then
        StepVerifier.create(transactionService.processTransaction(transactionRequest))
            .expectError(RuntimeException::class.java)
            .verify()

        verify(transactionProcessor).validateTransaction(eq(transactionRequest), eq(sourceAccount), eq(targetAccount))
    }

    @Test
    fun `processTransaction should handle transaction completion failure`() {
        // Given
        val completionError = RuntimeException("Balance update failed")
        whenever(accountRepository.findById(1L)).thenReturn(Mono.just(sourceAccount))
        whenever(accountRepository.findById(2L)).thenReturn(Mono.just(targetAccount))
        whenever(transactionProcessor.validateTransaction(eq(transactionRequest), eq(sourceAccount), eq(targetAccount)))
            .thenReturn(Mono.empty())
        whenever(transactionProcessor.createPendingTransaction(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(pendingTransaction))
        whenever(transactionProcessor.updateAccountBalances(eq(sourceAccount), eq(targetAccount), any()))
            .thenReturn(Mono.error(completionError))
        whenever(transactionProcessor.completeTransaction(pendingTransaction))
            .thenReturn(Mono.just(completedTransaction))
        whenever(transactionProcessor.failTransaction(eq(pendingTransaction), any()))
            .thenReturn(Mono.just(pendingTransaction.withStatus(TransactionStatusEnum.FAILED)))

        // When & Then
        StepVerifier.create(transactionService.processTransaction(transactionRequest))
            .expectError(RuntimeException::class.java)
            .verify()

        verify(transactionProcessor).failTransaction(eq(pendingTransaction), any())
    }

    @Test
    fun `getTransactionById should return transaction when found`() {
        // Given
        whenever(transactionRepository.findById(1L)).thenReturn(Mono.just(completedTransaction))
        whenever(transactionMapper.entityToResponseDto(completedTransaction)).thenReturn(transactionResponse)

        // When & Then
        StepVerifier.create(transactionService.getTransactionById(1L))
            .expectNext(transactionResponse)
            .verifyComplete()

        verify(transactionRepository).findById(1L)
        verify(transactionMapper).entityToResponseDto(completedTransaction)
    }

    @Test
    fun `getTransactionById should fail when transaction not found`() {
        // Given
        whenever(transactionRepository.findById(999L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(transactionService.getTransactionById(999L))
            .expectError(TransactionNotFoundException::class.java)
            .verify()

        verify(transactionRepository).findById(999L)
    }

    @Test
    fun `getTransactionsByAccountId should return transactions when account exists`() {
        // Given
        val transactions = listOf(completedTransaction, pendingTransaction)
        whenever(accountRepository.findById(1L)).thenReturn(Mono.just(sourceAccount))
        whenever(transactionRepository.findByAccountId(1L)).thenReturn(Flux.fromIterable(transactions))
        whenever(transactionMapper.entityToResponseDto(completedTransaction)).thenReturn(transactionResponse)
        whenever(transactionMapper.entityToResponseDto(pendingTransaction))
            .thenReturn(transactionResponse.copy(status = TransactionStatusEnum.PENDING))

        // When & Then
        StepVerifier.create(transactionService.getTransactionsByAccountId(1L))
            .expectNextCount(2)
            .verifyComplete()

        verify(accountRepository).findById(1L)
        verify(transactionRepository).findByAccountId(1L)
    }

    @Test
    fun `getTransactionsByAccountId should fail when account not found`() {
        // Given
        whenever(accountRepository.findById(999L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(transactionService.getTransactionsByAccountId(999L))
            .expectError(AccountNotFoundException::class.java)
            .verify()

        verify(accountRepository).findById(999L)
    }

    @Test
    fun `getOutgoingTransactions should return outgoing transactions`() {
        // Given
        whenever(accountRepository.findById(1L)).thenReturn(Mono.just(sourceAccount))
        whenever(transactionRepository.findBySourceAccountId(1L)).thenReturn(Flux.just(completedTransaction))
        whenever(transactionMapper.entityToResponseDto(completedTransaction)).thenReturn(transactionResponse)

        // When & Then
        StepVerifier.create(transactionService.getOutgoingTransactions(1L))
            .expectNext(transactionResponse)
            .verifyComplete()

        verify(accountRepository).findById(1L)
        verify(transactionRepository).findBySourceAccountId(1L)
    }

    @Test
    fun `getIncomingTransactions should return incoming transactions`() {
        // Given
        whenever(accountRepository.findById(2L)).thenReturn(Mono.just(targetAccount))
        whenever(transactionRepository.findByTargetAccountId(2L)).thenReturn(Flux.just(completedTransaction))
        whenever(transactionMapper.entityToResponseDto(completedTransaction)).thenReturn(transactionResponse)

        // When & Then
        StepVerifier.create(transactionService.getIncomingTransactions(2L))
            .expectNext(transactionResponse)
            .verifyComplete()

        verify(accountRepository).findById(2L)
        verify(transactionRepository).findByTargetAccountId(2L)
    }

    @Test
    fun `getOutgoingTransactions should fail when account not found`() {
        // Given
        whenever(accountRepository.findById(999L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(transactionService.getOutgoingTransactions(999L))
            .expectError(AccountNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `getIncomingTransactions should fail when account not found`() {
        // Given
        whenever(accountRepository.findById(999L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(transactionService.getIncomingTransactions(999L))
            .expectError(AccountNotFoundException::class.java)
            .verify()
    }
}
