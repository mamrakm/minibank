package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.transaction.response.TransferResponse
import cz.ememsoft.minibank.service.TransactionService
import cz.ememsoft.minibank.transaction.request.TransferRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * REST controller for managing transactions in the application.
 *
 * This controller provides endpoints for initiating money transfers between accounts
 * and retrieving transaction history.
 *
 * @property transactionService The service layer for transaction-related operations.
 */
@RestController
@RequestMapping("/transactions")
class TransactionController(private val transactionService: TransactionService) {

    /**
     * Initiates a money transfer between accounts.
     *
     * @param transferRequest The details of the transfer operation.
     * @return A [Mono] emitting the [TransferResponse] containing the transaction details.
     */
    @PostMapping("/transfer", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun transferMoney(@Valid @RequestBody transferRequest: TransferRequest): Mono<TransferResponse> {
        logger.info { "Received transfer request: $transferRequest" }
        return transactionService.transferMoney(transferRequest)
            .doOnSuccess { logger.info { "Successfully processed transfer with ID: ${it.id}" } }
            .doOnError { logger.error(it) { "Error processing transfer request" } }
    }

    /**
     * Retrieves a transaction by its ID.
     *
     * @param id The ID of the transaction to retrieve.
     * @return A [Mono] emitting the [TransferResponse] if found.
     */
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTransaction(@PathVariable id: Long): Mono<TransferResponse> {
        logger.info { "Fetching transaction with ID: $id" }
        return transactionService.getTransactionById(id)
            .doOnSuccess { logger.info { "Successfully retrieved transaction with ID: $id" } }
            .doOnError { logger.error(it) { "Error retrieving transaction with ID: $id" } }
    }

    /**
     * Retrieves all transactions associated with an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all [TransferResponse] objects associated with the account.
     */
    @GetMapping("/account/{accountId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTransactionsByAccount(@PathVariable accountId: Long): Flux<TransferResponse> {
        logger.info { "Fetching transactions for account ID: $accountId" }
        return transactionService.getTransactionsByAccountId(accountId)
            .doOnComplete { logger.info { "Successfully retrieved transactions for account ID: $accountId" } }
            .doOnError { logger.error(it) { "Error retrieving transactions for account ID: $accountId" } }
    }

    /**
     * Retrieves outgoing transactions from an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all outgoing [TransferResponse] objects from the account.
     */
    @GetMapping("/account/{accountId}/outgoing", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getOutgoingTransactions(@PathVariable accountId: Long): Flux<TransferResponse> {
        logger.info { "Fetching outgoing transactions for account ID: $accountId" }
        return transactionService.getOutgoingTransactions(accountId)
            .doOnComplete { logger.info { "Successfully retrieved outgoing transactions for account ID: $accountId" } }
            .doOnError { logger.error(it) { "Error retrieving outgoing transactions for account ID: $accountId" } }
    }

    /**
     * Retrieves incoming transactions to an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all incoming [TransferResponse] objects to the account.
     */
    @GetMapping("/account/{accountId}/incoming", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getIncomingTransactions(@PathVariable accountId: Long): Flux<TransferResponse> {
        logger.info { "Fetching incoming transactions for account ID: $accountId" }
        return transactionService.getIncomingTransactions(accountId)
            .doOnComplete { logger.info { "Successfully retrieved incoming transactions for account ID: $accountId" } }
            .doOnError { logger.error(it) { "Error retrieving incoming transactions for account ID: $accountId" } }
    }
}