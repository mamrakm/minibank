package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.entity.TransactionEntity
import cz.ememsoft.minibank.entity.TransactionStatusEnum
import cz.ememsoft.minibank.exception.InsufficientFundsException
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.TransactionRepository
import cz.ememsoft.minibank.transaction.request.TransferRequestDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * Implementation of [TransactionProcessor] that handles the core transaction processing logic.
 */
@Component
class TransactionProcessorImpl(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) : TransactionProcessor {

    /**
     * Validates a transfer request to ensure sufficient funds are available.
     */
    override fun validateTransfer(
        transferRequest: TransferRequestDto,
        sourceAccount: AccountEntity,
        targetAccount: AccountEntity,
    ): Mono<Unit> = Mono.defer {
        // Check sufficient funds
        if (sourceAccount.balance.compareTo(transferRequest.amount) < 0) {
            val errorMessage = "Insufficient funds in account ${sourceAccount.id}. " +
                    "Available: ${sourceAccount.balance}, Required: ${transferRequest.amount}"
            logger.warn { errorMessage }
            Mono.error(InsufficientFundsException(errorMessage))
        } else {
            Mono.just(Unit)
        }
    }

    /**
     * Creates a pending transaction record in the database.
     */
    override fun createPendingTransaction(
        sourceAccountId: Long,
        targetAccountId: Long,
        amount: BigDecimal,
        currency: String,
        reference: String,
        timestamp: LocalDateTime,
    ): Mono<TransactionEntity> {
        logger.info { "Creating pending transaction: $sourceAccountId -> $targetAccountId, amount: $amount, currency: $currency" }
        return transactionRepository.saveAndReturnId(
            sourceAccountId = sourceAccountId,
            targetAccountId = targetAccountId,
            amount = amount,
            currency = currency,
            timestamp = timestamp,
            status = TransactionStatusEnum.PENDING.name,
            reference = reference
        ).doOnSuccess { logger.debug { "Created pending transaction with ID: ${it.id}" } }
    }

    /**
     * Updates account balances for a transfer in an atomic operation.
     */
    override fun updateAccountBalances(
        sourceAccount: AccountEntity,
        targetAccount: AccountEntity,
        amount: BigDecimal,
    ): Mono<Unit> {
        logger.info { "Updating account balances for transfer: ${sourceAccount.id} -> ${targetAccount.id}, amount: $amount" }

        // Create updated account entities
        val updatedSourceAccount = sourceAccount.copy(
            balance = sourceAccount.balance.subtract(amount)
        )

        val updatedTargetAccount = targetAccount.copy(
            balance = targetAccount.balance.add(amount)
        )

        // Update both accounts in parallel
        return Mono.zip(
            accountRepository.save(updatedSourceAccount),
            accountRepository.save(updatedTargetAccount)
        ).map { Unit }
            .doOnSuccess { logger.debug { "Successfully updated account balances for transfer" } }
            .doOnError { logger.error(it) { "Failed to update account balances for transfer" } }
    }

    /**
     * Marks a transaction as completed.
     */
    override fun completeTransaction(transaction: TransactionEntity): Mono<TransactionEntity> {
        logger.info { "Completing transaction with ID: ${transaction.id}" }
        val completedTransaction = transaction.copy(status = TransactionStatusEnum.COMPLETED)
        return transactionRepository.save(completedTransaction)
            .doOnSuccess { logger.info { "Successfully completed transaction with ID: ${it.id}" } }
    }

    /**
     * Marks a transaction as failed with the given error message.
     */
    override fun failTransaction(transaction: TransactionEntity, errorMessage: String): Mono<TransactionEntity> {
        logger.info { "Marking transaction with ID: ${transaction.id} as failed" }
        val failedTransaction = transaction.copy(
            status = TransactionStatusEnum.FAILED,
            reference = "Failed: $errorMessage"
        )
        return transactionRepository.save(failedTransaction)
            .doOnSuccess { logger.info { "Successfully marked transaction with ID: ${it.id} as failed" } }
    }
}