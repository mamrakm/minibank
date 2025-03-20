package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.transaction.request.TransferRequestDto
import cz.ememsoft.minibank.transaction.response.TransferResponseDto
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
     * @return A [Mono] emitting the [TransferResponseDto] containing the transaction details.
     */
    fun transferMoney(transferRequest: TransferRequestDto): Mono<TransferResponseDto>

    /**
     * Retrieves a transaction by its ID.
     *
     * @param id The ID of the transaction to retrieve.
     * @return A [Mono] emitting the [TransferResponseDto] if found.
     */
    fun getTransactionById(id: Long): Mono<TransferResponseDto>

    /**
     * Retrieves all transactions associated with an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all [TransferResponseDto] objects associated with the account.
     */
    fun getTransactionsByAccountId(accountId: Long): Flux<TransferResponseDto>

    /**
     * Retrieves outgoing transactions from an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all outgoing [TransferResponseDto] objects from the account.
     */
    fun getOutgoingTransactions(accountId: Long): Flux<TransferResponseDto>

    /**
     * Retrieves incoming transactions to an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all incoming [TransferResponseDto] objects to the account.
     */
    fun getIncomingTransactions(accountId: Long): Flux<TransferResponseDto>
}