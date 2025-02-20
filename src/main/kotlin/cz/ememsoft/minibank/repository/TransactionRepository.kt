package cz.ememsoft.minibank.transaction.repository

import cz.ememsoft.minibank.transaction.entity.TransactionEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

/**
 * Repository interface for managing [TransactionEntity] persistence.
 *
 * Provides reactive CRUD operations and custom queries for transactions.
 */
@Repository
interface TransactionRepository : R2dbcRepository<TransactionEntity, Long> {

    /**
     * Retrieves transactions for a given sender account.
     *
     * @param fromAccountId The sender's account ID.
     * @return A [Flux] emitting transactions associated with the sender.
     */
    fun findByFromAccountId(fromAccountId: Long): Flux<TransactionEntity>

    /**
     * Retrieves transactions for a given recipient account.
     *
     * @param toAccountId The recipient's account ID.
     * @return A [Flux] emitting transactions associated with the recipient.
     */
    fun findByToAccountId(toAccountId: Long): Flux<TransactionEntity>
}
