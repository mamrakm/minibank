package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.service.TransactionService
import cz.ememsoft.minibank.transaction.request.TransferRequestDto
import cz.ememsoft.minibank.transaction.response.TransferResponseDto
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
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
     * @return A [Mono] emitting the [TransferResponseDto] containing the transaction details.
     */
    @PostMapping("/transfer", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun transferMoney(@Valid @RequestBody transferRequest: TransferRequestDto): Mono<TransferResponseDto> {
        logger.info { "Received transfer request: $transferRequest" }
        return transactionService.transferMoney(transferRequest)
            .doOnSuccess { logger.info { "Successfully processed transfer with ID: ${it.id}" } }
            .doOnError { logger.error(it) { "Error processing transfer request" } }
    }

    /**
     * Retrieves a transaction by its ID.
     *
     * @param id The ID of the transaction to retrieve.
     * @return A [Mono] emitting the [TransferResponseDto] if found.
     */
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTransaction(@PathVariable id: Long): Mono<TransferResponseDto> {
        logger.info { "Fetching transaction with ID: $id" }
        return transactionService.getTransactionById(id)
            .doOnSuccess { logger.info { "Successfully retrieved transaction with ID: $id" } }
            .doOnError { logger.error(it) { "Error retrieving transaction with ID: $id" } }
    }

    /**
     * Retrieves all transactions associated with an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all [TransferResponseDto] objects associated with the account.
     */
    @GetMapping("/account/{accountId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTransactionsByAccount(@PathVariable accountId: Long): Flux<TransferResponseDto> {
        logger.info { "Fetching transactions for account ID: $accountId" }
        return transactionService.getTransactionsByAccountId(accountId)
            .doOnComplete { logger.info { "Successfully retrieved transactions for account ID: $accountId" } }
            .doOnError { logger.error(it) { "Error retrieving transactions for account ID: $accountId" } }
    }

    /**
     * Retrieves outgoing transactions from an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all outgoing [TransferResponseDto] objects from the account.
     */
    @GetMapping("/account/{accountId}/outgoing", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getOutgoingTransactions(@PathVariable accountId: Long): Flux<TransferResponseDto> {
        logger.info { "Fetching outgoing transactions for account ID: $accountId" }
        return transactionService.getOutgoingTransactions(accountId)
            .doOnComplete { logger.info { "Successfully retrieved outgoing transactions for account ID: $accountId" } }
            .doOnError { logger.error(it) { "Error retrieving outgoing transactions for account ID: $accountId" } }
    }

    /**
     * Retrieves incoming transactions to an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all incoming [TransferResponseDto] objects to the account.
     */
    @GetMapping("/account/{accountId}/incoming", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getIncomingTransactions(@PathVariable accountId: Long): Flux<TransferResponseDto> {
        logger.info { "Fetching incoming transactions for account ID: $accountId" }
        return transactionService.getIncomingTransactions(accountId)
            .doOnComplete { logger.info { "Successfully retrieved incoming transactions for account ID: $accountId" } }
            .doOnError { logger.error(it) { "Error retrieving incoming transactions for account ID: $accountId" } }
    }
}