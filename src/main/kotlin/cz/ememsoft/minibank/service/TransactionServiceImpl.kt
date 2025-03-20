package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.transaction.response.TransferResponse
import cz.ememsoft.minibank.exception.InvalidTransactionAmountException
import cz.ememsoft.minibank.exception.SameAccountTransferException
import cz.ememsoft.minibank.exception.TransactionFailedException
import cz.ememsoft.minibank.exception.TransactionNotFoundException
import cz.ememsoft.minibank.mapper.TransactionMapper
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.TransactionRepository
import cz.ememsoft.minibank.transaction.request.TransferRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.math.BigDecimal
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * Implementation of the [TransactionService] interface that orchestrates
 * transaction operations using the [TransactionProcessor].
 */
@Service
class TransactionServiceImpl(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val transactionMapper: TransactionMapper,
    private val transactionProcessor: TransactionProcessor,
) : TransactionService {

    /**
     * Transfers money between two accounts in a transactional manner.
     *
     * This implementation follows a workflow pattern:
     * 1. Validate the request
     * 2. Retrieve the accounts
     * 3. Validate the accounts
     * 4. Create a pending transaction
     * 5. Process the transfer
     * 6. Mark the transaction as completed or failed
     *
     * @param transferRequest The details of the transfer operation
     * @return A [Mono] emitting the [TransferResponse] containing the transaction details
     */
    @Transactional
    override fun transferMoney(transferRequest: TransferRequest): Mono<TransferResponse> {
        logger.info { "Initiating money transfer: $transferRequest" }

        // Initial validations
        return validateInitialRequest(transferRequest)
            // Fetch accounts
            .then(fetchAccounts(transferRequest))
            // Process the transfer
            .flatMap { (sourceAccountId, targetAccountId) ->
                // Validate transfer conditions (sufficient funds)
                transactionProcessor.validateTransfer(transferRequest, sourceAccount, targetAccount)
                    // Create pending transaction
                    .then(
                        transactionProcessor.createPendingTransaction(
                            sourceAccountId = sourceAccount.id,
                            targetAccountId = targetAccount.id,
                            amount = transferRequest.amount,
                            currency = transferRequest.currency,
                            reference = transferRequest.reference,
                            timestamp = LocalDateTime.now()
                        )
                    )
                    // Update account balances
                    .flatMap { transaction ->
                        transactionProcessor.updateAccountBalances(
                            sourceAccount = sourceAccount,
                            targetAccount = targetAccount,
                            amount = transferRequest.amount
                        )
                            // Mark transaction as completed
                            .then(transactionProcessor.completeTransaction(transaction))
                            // Handle any errors during processing
                            .onErrorResume { error ->
                                logger.error(error) { "Error during transfer processing" }
                                transactionProcessor.failTransaction(transaction, error.message ?: "Unknown error")
                                    .flatMap { Mono.error<TransferResponse>(TransactionFailedException("Transaction failed: ${error.message}")) }
                            }
                    }
                    // Map to response DTO
                    .map { transactionMapper.entityToResponseDto(it) }
            }
            .doOnSuccess { logger.info { "Successfully completed money transfer with ID: ${it.id}" } }
            .doOnError { logger.error(it) { "Failed to complete money transfer" } }
    }

    /**
     * Validates the initial transfer request.
     *
     * @param transferRequest The transfer request to validate
     * @return A [Mono] that completes successfully if validation passes
     */
    private fun validateInitialRequest(transferRequest: TransferRequest): Mono<Unit> = Mono.defer {
        // Validate positive amount
        if (transferRequest.amount <= BigDecimal.ZERO) {
            logger.warn { "Invalid transaction amount: ${transferRequest.amount}" }
            return@defer Mono.error<Unit>(InvalidTransactionAmountException("Transaction amount must be positive"))
        }

        // Validate different accounts
        if (transferRequest.sourceAccountId == transferRequest.targetAccountId) {
            logger.warn { "Attempted transfer to same account: ${transferRequest.sourceAccountId}" }
            return@defer Mono.error<Unit>(SameAccountTransferException("Cannot transfer money to the same account"))
        }

        Mono.just(Unit)
    }

    /**
     * Fetches source and target accounts for a transfer.
     *
     * @param transferRequest The transfer request containing account IDs
     * @return A [Mono] emitting a pair of source and target account entities
     */
    private fun fetchAccounts(transferRequest: TransferRequest) = Mono.zip(
        accountRepository.findById(transferRequest.sourceAccountId)
            .switchIfEmpty {
                logger.warn { "Source account not found: ${transferRequest.sourceAccountId}" }
                Mono.error(AccountNotFoundException("Source account with ID ${transferRequest.sourceAccountId} not found"))
            },
        accountRepository.findById(transferRequest.targetAccountId)
            .switchIfEmpty {
                logger.warn { "Target account not found: ${transferRequest.targetAccountId}" }
                Mono.error(AccountNotFoundException("Target account with ID ${transferRequest.targetAccountId} not found"))
            }
    )

    /**
     * Retrieves a transaction by its ID.
     *
     * @param id The ID of the transaction to retrieve
     * @return A [Mono] emitting the [TransferResponse] if found
     */
    override fun getTransactionById(id: Long): Mono<TransferResponse> {
        logger.info { "Fetching transaction with ID: $id" }
        return transactionRepository.findById(id)
            .switchIfEmpty {
                logger.warn { "Transaction not found: $id" }
                Mono.error(TransactionNotFoundException("Transaction with ID $id not found"))
            }
            .map { transactionMapper.entityToResponseDto(it) }
            .doOnSuccess { logger.debug { "Successfully retrieved transaction with ID: $id" } }
            .doOnError { logger.error(it) { "Error retrieving transaction with ID: $id" } }
    }

    /**
     * Retrieves all transactions associated with an account.
     *
     * @param accountId The ID of the account
     * @return A [Flux] emitting all [TransferResponse] objects associated with the account
     */
    override fun getTransactionsByAccountId(accountId: Long): Flux<TransferResponse> {
        logger.info { "Fetching transactions for account ID: $accountId" }
        return validateAccountExists(accountId)
            .thenMany(
                transactionRepository.findByAccountId(accountId)
                    .map { transactionMapper.entityToResponseDto(it) }
            )
            .doOnComplete { logger.debug { "Successfully retrieved transactions for account ID: $accountId" } }
            .doOnError { logger.error(it) { "Error retrieving transactions for account ID: $accountId" } }
    }

    /**
     * Retrieves outgoing transactions from an account.
     *
     * @param accountId The ID of the account
     * @return A [Flux] emitting all outgoing [TransferResponse] objects from the account
     */
    override fun getOutgoingTransactions(accountId: Long): Flux<TransferResponse> {
        logger.info { "Fetching outgoing transactions for account ID: $accountId" }
        return validateAccountExists(accountId)
            .thenMany(
                transactionRepository.findBySourceAccountId(accountId)
                    .map { transactionMapper.entityToResponseDto(it) }
            )
            .doOnComplete { logger.debug { "Successfully retrieved outgoing transactions for account ID: $accountId" } }
            .doOnError { logger.error(it) { "Error retrieving outgoing transactions for account ID: $accountId" } }
    }

    /**
     * Retrieves incoming transactions to an account.
     *
     * @param accountId The ID of the account
     * @return A [Flux] emitting all incoming [TransferResponse] objects to the account
     */
    override fun getIncomingTransactions(accountId: Long): Flux<TransferResponse> {
        logger.info { "Fetching incoming transactions for account ID: $accountId" }
        return validateAccountExists(accountId)
            .thenMany(
                transactionRepository.findByTargetAccountId(accountId)
                    .map { transactionMapper.entityToResponseDto(it) }
            )
            .doOnComplete { logger.debug { "Successfully retrieved incoming transactions for account ID: $accountId" } }
            .doOnError { logger.error(it) { "Error retrieving incoming transactions for account ID: $accountId" } }
    }

    /**
     * Validates that an account exists.
     *
     * @param accountId The ID of the account to check
     * @return A [Mono] that completes if the account exists, or emits an error if not
     */
    private fun validateAccountExists(accountId: Long): Mono<Unit> {
        return accountRepository.findById(accountId)
            .switchIfEmpty {
                logger.warn { "Account not found: $accountId" }
                Mono.error(AccountNotFoundException("Account with ID $accountId not found"))
            }
            .then(Mono.just(Unit))
    }
}