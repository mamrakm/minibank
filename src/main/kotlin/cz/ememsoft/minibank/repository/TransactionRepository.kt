package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.TransactionEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Repository interface for managing [TransactionEntity] objects.
 *
 * This repository provides CRUD operations and custom query methods for handling
 * transaction-related data in a reactive manner.
 */
@Repository
interface TransactionRepository : R2dbcRepository<TransactionEntity, Long> {

    /**
     * Finds all transactions where the account is either the source or target.
     *
     * @param accountId The ID of the account to find transactions for.
     * @return A [Flux] emitting all transactions associated with the account.
     */
    @Query("SELECT * FROM bank.transaction WHERE source_account_id = :accountId OR target_account_id = :accountId ORDER BY timestamp DESC")
    fun findByAccountId(accountId: Long): Flux<TransactionEntity>

    /**
     * Finds all transactions where the account is the source.
     *
     * @param accountId The ID of the account to find outgoing transactions for.
     * @return A [Flux] emitting all outgoing transactions from the account.
     */
    fun findBySourceAccountId(accountId: Long): Flux<TransactionEntity>

    /**
     * Finds all transactions where the account is the target.
     *
     * @param accountId The ID of the account to find incoming transactions for.
     * @return A [Flux] emitting all incoming transactions to the account.
     */
    fun findByTargetAccountId(accountId: Long): Flux<TransactionEntity>

    /**
     * Inserts a new transaction into the database and returns the saved entity.
     *
     * This method performs an **INSERT** operation into the `bank.transaction` table,
     * inserting the provided transaction details and returning the newly created transaction entity.
     * The `RETURNING` clause ensures that the full entity, including the generated ID,
     * is retrieved after the insertion.
     *
     * @param sourceAccountId The ID of the source account.
     * @param targetAccountId The ID of the target account.
     * @param amount The amount of the transaction.
     * @param timestamp The timestamp of the transaction.
     * @param status The status of the transaction.
     * @param reference The reference or description of the transaction.
     * @return A [Mono] emitting the saved [TransactionEntity] with all populated fields, including the generated ID.
     */
    @Query(
        "INSERT INTO bank.transaction " +
                "(source_account_id, target_account_id, amount, currency, timestamp, status, reference) " +
                "VALUES (:sourceAccountId, :targetAccountId, :amount, :currency, :timestamp, :status, :reference) " +
                "RETURNING id, source_account_id, target_account_id, amount, currency, timestamp, status, reference"
    )
    fun saveAndReturnId(
        sourceAccountId: Long,
        targetAccountId: Long,
        amount: BigDecimal,
        currency: String,
        timestamp: LocalDateTime,
        status: String,
        reference: String
    ): Mono<TransactionEntity>
}