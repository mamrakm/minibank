package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.transaction.request.TransactionRequestDto
import cz.ememsoft.minibank.api.transaction.response.TransactionResponseDto
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service interface for managing transaction-related operations.
 * Provides reactive methods for performing money transactions and retrieving transaction history.
 */
interface TransactionService {

    /**
     * Processes a money transaction between two accounts.
     *
     * @param transactionRequest The details of the transaction operation.
     * @return A [Mono] emitting the [TransactionResponseDto] containing the transaction details.
     */
    fun processTransaction(transactionRequest: TransactionRequestDto): Mono<TransactionResponseDto>

    /**
     * Retrieves a transaction by its ID.
     *
     * @param id The ID of the transaction to retrieve.
     * @return A [Mono] emitting the [TransactionResponseDto] if found.
     */
    fun getTransactionById(id: Long): Mono<TransactionResponseDto>

    /**
     * Retrieves all transactions associated with an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all [TransactionResponseDto] objects associated with the account.
     */
    fun getTransactionsByAccountId(accountId: Long): Flux<TransactionResponseDto>

    /**
     * Retrieves outgoing transactions from an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all outgoing [TransactionResponseDto] objects from the account.
     */
    fun getOutgoingTransactions(accountId: Long): Flux<TransactionResponseDto>

    /**
     * Retrieves incoming transactions to an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all incoming [TransactionResponseDto] objects to the account.
     */
    fun getIncomingTransactions(accountId: Long): Flux<TransactionResponseDto>
}