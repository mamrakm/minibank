package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.entity.TransactionEntity
import cz.ememsoft.minibank.transaction.request.TransferRequestDto
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * Interface for processing transactions between accounts.
 *
 * This interface defines the core functionality for validating and processing
 * money transfers between accounts, following the Single Responsibility Principle.
 */
interface TransactionProcessor {

    /**
     * Validates a transfer request before processing.
     *
     * @param transferRequest The transfer request to validate
     * @param sourceAccount The source account entity
     * @param targetAccount The target account entity
     * @return A [Mono] that completes successfully if validation passes or emits an error if validation fails
     */
    fun validateTransfer(
        transferRequest: TransferRequestDto,
        sourceAccount: AccountEntity,
        targetAccount: AccountEntity
    ): Mono<Unit>

    /**
     * Creates a pending transaction record.
     *
     * @param sourceAccountId ID of the source account
     * @param targetAccountId ID of the target account
     * @param amount Amount to transfer
     * @param currency Currency of the transaction
     * @param reference Reference text for the transaction
     * @param timestamp Timestamp of the transaction
     * @return A [Mono] emitting the created [TransactionEntity]
     */
    fun createPendingTransaction(
        sourceAccountId: Long,
        targetAccountId: Long,
        amount: java.math.BigDecimal,
        currency: String,
        reference: String,
        timestamp: LocalDateTime
    ): Mono<TransactionEntity>

    /**
     * Updates account balances for a transfer.
     *
     * @param sourceAccount The source account to debit
     * @param targetAccount The target account to credit
     * @param amount The amount to transfer
     * @return A [Mono] that completes when both accounts are updated
     */
    fun updateAccountBalances(
        sourceAccount: AccountEntity,
        targetAccount: AccountEntity,
        amount: java.math.BigDecimal
    ): Mono<Unit>

    /**
     * Marks a transaction as completed.
     *
     * @param transaction The transaction entity to update
     * @return A [Mono] emitting the updated [TransactionEntity]
     */
    fun completeTransaction(transaction: TransactionEntity): Mono<TransactionEntity>

    /**
     * Marks a transaction as failed.
     *
     * @param transaction The transaction entity to update
     * @param errorMessage The error message describing why the transaction failed
     * @return A [Mono] emitting the updated [TransactionEntity]
     */
    fun failTransaction(transaction: TransactionEntity, errorMessage: String): Mono<TransactionEntity>
}