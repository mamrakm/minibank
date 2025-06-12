package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.transaction.request.TransactionRequestDto
import cz.ememsoft.minibank.api.transaction.response.TransactionResponseDto
import cz.ememsoft.minibank.enumeration.TransactionStatusEnum
import cz.ememsoft.minibank.exception.AccountNotFoundException
import cz.ememsoft.minibank.exception.InsufficientFundsException
import cz.ememsoft.minibank.exception.TransactionNotFoundException
import cz.ememsoft.minibank.service.TransactionService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TransactionControllerUnitTest {

    @Mock
    private lateinit var transactionService: TransactionService

    @InjectMocks
    private lateinit var transactionController: TransactionController

    private lateinit var transactionRequest: TransactionRequestDto
    private lateinit var transactionResponse: TransactionResponseDto

    @BeforeEach
    fun setUp() {
        transactionRequest = TransactionRequestDto(
            sourceAccountId = 1L,
            targetAccountId = 2L,
            amount = BigDecimal("150.0000"),
            currency = "USD",
            reference = "Test transfer"
        )

        transactionResponse = TransactionResponseDto(
            id = 1L,
            sourceAccountId = 1L,
            targetAccountId = 2L,
            amount = BigDecimal("150.0000"),
            currency = "USD",
            timestamp = LocalDateTime.now(),
            status = TransactionStatusEnum.COMPLETED,
            reference = "Test transfer"
        )
    }

    @Test
    fun `processTransaction should complete transfer successfully`() {
        // Arrange
        given(transactionService.processTransaction(transactionRequest))
            .willReturn(Mono.just(transactionResponse))

        // Act & Assert
        StepVerifier.create(transactionController.processTransaction(transactionRequest))
            .expectNext(transactionResponse)
            .verifyComplete()

        verify(transactionService).processTransaction(transactionRequest)
    }

    @Test
    fun `processTransaction should propagate AccountNotFoundException for invalid source`() {
        // Arrange
        val exception = AccountNotFoundException("Source account 999 not found")
        given(transactionService.processTransaction(transactionRequest))
            .willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(transactionController.processTransaction(transactionRequest))
            .expectErrorMatches {
                it is AccountNotFoundException && it.message == "Source account 999 not found"
            }
            .verify()

        verify(transactionService).processTransaction(transactionRequest)
    }

    @Test
    fun `processTransaction should propagate InsufficientFundsException`() {
        // Arrange
        val exception = InsufficientFundsException("Insufficient funds in account 1")
        given(transactionService.processTransaction(transactionRequest))
            .willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(transactionController.processTransaction(transactionRequest))
            .expectErrorMatches {
                it is InsufficientFundsException && it.message == "Insufficient funds in account 1"
            }
            .verify()

        verify(transactionService).processTransaction(transactionRequest)
    }

    @Test
    fun `getTransaction should return transaction when found`() {
        // Arrange
        given(transactionService.getTransactionById(1L))
            .willReturn(Mono.just(transactionResponse))

        // Act & Assert
        StepVerifier.create(transactionController.getTransaction(1L))
            .expectNext(transactionResponse)
            .verifyComplete()

        verify(transactionService).getTransactionById(1L)
    }

    @Test
    fun `getTransaction should propagate TransactionNotFoundException`() {
        // Arrange
        val exception = TransactionNotFoundException("Transaction with ID 999 not found")
        given(transactionService.getTransactionById(999L))
            .willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(transactionController.getTransaction(999L))
            .expectErrorMatches {
                it is TransactionNotFoundException && it.message == "Transaction with ID 999 not found"
            }
            .verify()

        verify(transactionService).getTransactionById(999L)
    }

    @Test
    fun `getTransactionsByAccount should return account transactions`() {
        // Arrange
        val secondResponse = transactionResponse.copy(id = 2L, amount = BigDecimal("200.0000"))
        given(transactionService.getTransactionsByAccountId(1L))
            .willReturn(Flux.just(transactionResponse, secondResponse))

        // Act & Assert
        StepVerifier.create(transactionController.getTransactionsByAccount(1L))
            .expectNext(transactionResponse)
            .expectNext(secondResponse)
            .verifyComplete()

        verify(transactionService).getTransactionsByAccountId(1L)
    }

    @Test
    fun `getTransactionsByAccount should handle empty result`() {
        // Arrange
        given(transactionService.getTransactionsByAccountId(999L))
            .willReturn(Flux.empty())

        // Act & Assert
        StepVerifier.create(transactionController.getTransactionsByAccount(999L))
            .verifyComplete()

        verify(transactionService).getTransactionsByAccountId(999L)
    }

    @Test
    fun `getOutgoingTransactions should return outgoing transactions`() {
        // Arrange
        given(transactionService.getOutgoingTransactions(1L))
            .willReturn(Flux.just(transactionResponse))

        // Act & Assert
        StepVerifier.create(transactionController.getOutgoingTransactions(1L))
            .expectNext(transactionResponse)
            .verifyComplete()

        verify(transactionService).getOutgoingTransactions(1L)
    }

    @Test
    fun `getIncomingTransactions should return incoming transactions`() {
        // Arrange
        given(transactionService.getIncomingTransactions(2L))
            .willReturn(Flux.just(transactionResponse))

        // Act & Assert
        StepVerifier.create(transactionController.getIncomingTransactions(2L))
            .expectNext(transactionResponse)
            .verifyComplete()

        verify(transactionService).getIncomingTransactions(2L)
    }

    @Test
    fun `getOutgoingTransactions should propagate AccountNotFoundException`() {
        // Arrange
        val exception = AccountNotFoundException("Account with ID 999 not found")
        given(transactionService.getOutgoingTransactions(999L))
            .willReturn(Flux.error(exception))

        // Act & Assert
        StepVerifier.create(transactionController.getOutgoingTransactions(999L))
            .expectError(AccountNotFoundException::class.java)
            .verify()

        verify(transactionService).getOutgoingTransactions(999L)
    }

    @Test
    fun `getIncomingTransactions should propagate AccountNotFoundException`() {
        // Arrange
        val exception = AccountNotFoundException("Account with ID 999 not found")
        given(transactionService.getIncomingTransactions(999L))
            .willReturn(Flux.error(exception))

        // Act & Assert
        StepVerifier.create(transactionController.getIncomingTransactions(999L))
            .expectError(AccountNotFoundException::class.java)
            .verify()

        verify(transactionService).getIncomingTransactions(999L)
    }
}