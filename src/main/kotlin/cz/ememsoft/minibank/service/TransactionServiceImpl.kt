package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.transaction.request.TransactionRequestDto
import cz.ememsoft.minibank.api.transaction.response.TransactionResponseDto
import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.entity.TransactionEntity
import cz.ememsoft.minibank.exception.AccountNotFoundException
import cz.ememsoft.minibank.exception.TransactionNotFoundException
import cz.ememsoft.minibank.mapper.TransactionMapper
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.TransactionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * Implementation of [TransactionService] that handles money transactions between accounts.
 *
 * This service coordinates the complex process of transferring money between accounts,
 * including validation, balance updates, and transaction recording.
 */
@Service
class TransactionServiceImpl(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionProcessor: TransactionProcessor,
    private val transactionMapper: TransactionMapper,
) : TransactionService {

    /**
     * Processes a money transaction between two accounts transactionally.
     *
     * The transaction process follows these steps:
     * 1. Validate accounts exist
     * 2. Validate transaction requirements (funds, currency, etc.)
     * 3. Create pending transaction record
     * 4. Update account balances atomically
     * 5. Mark the transaction as completed
     * 6. Handle any failures by marking the transaction as failed
     */
    @Transactional
    override fun processTransaction(transactionRequest: TransactionRequestDto): Mono<TransactionResponseDto> {
        logger.info { "Processing money transaction: $transactionRequest" }

        // 1. Fetch source account
        val sourceAccountMono: Mono<AccountEntity> = accountRepository.findById(transactionRequest.sourceAccountId)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Source account ${transactionRequest.sourceAccountId} not found")))

        // 2. Fetch a target account
        val targetAccountMono: Mono<AccountEntity> = accountRepository.findById(transactionRequest.targetAccountId)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Target account ${transactionRequest.targetAccountId} not found")))

        // 3. Combine both accounts
        val accountsTuple: Mono<Tuple2<AccountEntity, AccountEntity>> = Mono.zip(sourceAccountMono, targetAccountMono)

        // 4. Process the transaction
        return accountsTuple.flatMap { tuple ->
            val sourceAccount: AccountEntity = tuple.t1
            val targetAccount: AccountEntity = tuple.t2

            // 5. Validate the transaction
            processValidatedTransaction(transactionRequest, sourceAccount, targetAccount)
        }
    }


    /**
     * Retrieves a transaction by its ID.
     */
    override fun getTransactionById(id: Long): Mono<TransactionResponseDto> {
        logger.info { "Retrieving transaction with ID: $id" }

        // Find the transaction
        val transactionMono: Mono<TransactionEntity> = transactionRepository.findById(id)
            .switchIfEmpty(Mono.error(TransactionNotFoundException("Transaction with ID $id not found")))

        // Map to response DTO
        return transactionMono
            .map { entity ->
                transactionMapper.entityToResponseDto(entity)
            }
            .doOnSuccess { responseDto: TransactionResponseDto ->
                logger.debug { "Retrieved transaction: $id" }
            }
            .doOnError { error: Throwable ->
                logger.error(error) { "Error retrieving transaction with ID: $id" }
            }
    }

    /**
     * Retrieves all transactions associated with an account (both incoming and outgoing).
     */
    override fun getTransactionsByAccountId(accountId: Long): Flux<TransactionResponseDto> {
        logger.info { "Retrieving transactions for account ID: $accountId" }

        // First, verify the account exists
        val accountMono: Mono<AccountEntity> = accountRepository.findById(accountId)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID $accountId not found")))

        // Then get all transactions for this account
        return accountMono
            .flatMapMany {
                fetchAndMapTransactions(accountId, transactionRepository::findByAccountId)
            }
            .doOnComplete { logger.debug { "Retrieved transactions for account: $accountId" } }
            .doOnError { error -> logger.error(error) { "Error retrieving transactions for account ID: $accountId" } }
    }

    /**
     * Retrieves outgoing transactions from an account.
     */
    override fun getOutgoingTransactions(accountId: Long): Flux<TransactionResponseDto> {
        logger.info { "Retrieving outgoing transactions for account ID: $accountId" }

        // First, verify the account exists
        val accountMono: Mono<AccountEntity> = accountRepository.findById(accountId)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID $accountId not found")))

        // Then get outgoing transactions
        return accountMono
            .flatMapMany {
                fetchAndMapTransactions(accountId, transactionRepository::findBySourceAccountId)
            }
            .doOnComplete { logger.debug { "Retrieved outgoing transactions for account: $accountId" } }
            .doOnError { error -> logger.error(error) { "Error retrieving outgoing transactions for account ID: $accountId" } }
    }

    /**
     * Retrieves incoming transactions to an account.
     */
    override fun getIncomingTransactions(accountId: Long): Flux<TransactionResponseDto> {
        logger.info { "Retrieving incoming transactions for account ID: $accountId" }

        // First, verify the account exists
        val accountMono: Mono<AccountEntity> = accountRepository.findById(accountId)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID $accountId not found")))

        // Then get incoming transactions
        return accountMono
            .flatMapMany {
                fetchAndMapTransactions(accountId, transactionRepository::findByTargetAccountId)
            }
            .doOnComplete { logger.debug { "Retrieved incoming transactions for account: $accountId" } }
            .doOnError { error -> logger.error(error) { "Error retrieving incoming transactions for account ID: $accountId" } }
    }


    // ========================================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================================

    /**
     * Helper method to process a validated transaction.
     */
    private fun processValidatedTransaction(
        request: TransactionRequestDto,
        sourceAccount: AccountEntity,
        targetAccount: AccountEntity,
    ): Mono<TransactionResponseDto> {
        // 1. Validate the transaction
        return transactionProcessor.validateTransaction(request, sourceAccount, targetAccount)
            .then(createPendingTransaction(request))
            .flatMap { pendingTransaction: TransactionEntity ->
                completeTransaction(pendingTransaction, sourceAccount, targetAccount, request.amount)
                    .onErrorResume { error ->
                        // If completing the transaction fails, mark it as failed and re-throw
                        logger.error(error) { "Transaction ${pendingTransaction.id} failed during completion" }
                        transactionProcessor.failTransaction(
                            pendingTransaction,
                            error.message ?: "Transaction completion failed"
                        ).then(Mono.error(error))
                    }
            }
            .map { completedTransaction ->
                transactionMapper.entityToResponseDto(completedTransaction)
            }
            .doOnSuccess { responseDto: TransactionResponseDto ->
                logger.info { "Money transaction completed successfully: ${responseDto.id}" }
            }
            .doOnError { error: Throwable ->
                logger.error(error) { "Money transaction failed: $request" }
            }
    }

    /**
     * Helper method to create a pending transaction.
     */
    private fun createPendingTransaction(request: TransactionRequestDto): Mono<TransactionEntity> {
        return transactionProcessor.createPendingTransaction(
            sourceAccountId = request.sourceAccountId,
            targetAccountId = request.targetAccountId,
            amount = request.amount,
            currency = request.normalizedCurrency,
            reference = request.reference,
            timestamp = LocalDateTime.now()
        )
    }

    /**
     * Helper method to complete a transaction.
     */
    private fun completeTransaction(
        pendingTransaction: TransactionEntity,
        sourceAccount: AccountEntity,
        targetAccount: AccountEntity,
        amount: java.math.BigDecimal,
    ): Mono<TransactionEntity> {
        // Update account balances
        return transactionProcessor.updateAccountBalances(sourceAccount, targetAccount, amount)
            .then(
                // Mark the transaction as completed
                transactionProcessor.completeTransaction(pendingTransaction)
            )
            .onErrorResume { error ->
                // If the balance update fails, mark the transaction as failed
                logger.error(error) { "Failed to update account balances for transaction ${pendingTransaction.id}" }
                transactionProcessor.failTransaction(
                    pendingTransaction,
                    error.message ?: "Balance update failed"
                ).then(Mono.error(error))
            }
    }

    /**
     * Helper method to fetch and map transactions using a provided function.
     * This method ensures that TransactionEntity objects are properly converted to TransactionResponseDto objects.
     */
    private fun fetchAndMapTransactions(
        accountId: Long,
        fetchFunction: (Long) -> Flux<TransactionEntity>,
    ): Flux<TransactionResponseDto> {
        val entityFlux: Flux<TransactionEntity> = fetchFunction(accountId)
        return entityFlux.map { entity ->
            transactionMapper.entityToResponseDto(entity)
        }
    }
}