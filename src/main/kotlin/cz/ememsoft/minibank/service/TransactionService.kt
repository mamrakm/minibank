package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.transaction.response.TransferResponse
import cz.ememsoft.minibank.entity.TransactionEntity
import cz.ememsoft.minibank.transaction.request.TransferRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service interface for managing transaction-related operations.
 * Provides reactive methods for performing money transfers and retrieving transaction history.
 */
interface TransactionService {

    /**
     * Transfers money between two accounts.
     *
     * @param transferRequest The details of the transfer operation.
     * @return A [Mono] emitting the [TransferResponse] containing the transaction details.
     */
    fun transferMoney(transferRequest: TransferRequest): Mono<TransferResponse>

    /**
     * Retrieves a transaction by its ID.
     *
     * @param id The ID of the transaction to retrieve.
     * @return A [Mono] emitting the [TransactionEntity] if found.
     */
    fun getTransactionById(id: Long): Mono<TransferResponse>

    /**
     * Retrieves all transactions associated with an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all [TransactionEntity] objects associated with the account.
     */
    fun getTransactionsByAccountId(accountId: Long): Flux<TransferResponse>

    /**
     * Retrieves outgoing transactions from an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all outgoing [TransactionEntity] objects from the account.
     */
    fun getOutgoingTransactions(accountId: Long): Flux<TransferResponse>

    /**
     * Retrieves incoming transactions to an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all incoming [TransactionEntity] objects to the account.
     */
    fun getIncomingTransactions(accountId: Long): Flux<TransferResponse>
}