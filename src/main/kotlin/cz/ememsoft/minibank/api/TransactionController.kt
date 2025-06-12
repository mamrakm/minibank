package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.transaction.request.TransactionRequestDto
import cz.ememsoft.minibank.api.transaction.response.TransactionResponseDto
import cz.ememsoft.minibank.service.TransactionService
import io.github.oshai.kotlinlogging.KotlinLogging
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

@RestController
@RequestMapping("/transactions")
class TransactionController(private val transactionService: TransactionService) {

    /**
     * Processes a money transaction between two accounts.
     *
     * @param transactionRequest The details of the transaction operation.
     * @return A [Mono] emitting the [TransactionResponseDto] containing the transaction details.
     */
    @PostMapping(
        path = ["/transfer"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun processTransaction(@RequestBody transactionRequest: TransactionRequestDto): Mono<TransactionResponseDto> {
        logger.info { "Processing transaction request: $transactionRequest" }
        return transactionService.processTransaction(transactionRequest)
            .doOnSuccess { logger.info { "Transaction completed successfully: ${it.id}" } }
            .doOnError { logger.error(it) { "Transaction failed: $transactionRequest" } }
    }

    /**
     * Retrieves a transaction by its ID.
     *
     * @param id The ID of the transaction to retrieve.
     * @return A [Mono] emitting the [TransactionResponseDto] if found.
     */
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTransaction(@PathVariable id: Long): Mono<TransactionResponseDto> {
        logger.info { "Fetching transaction with ID: $id" }
        return transactionService.getTransactionById(id)
            .doOnSuccess { logger.info { "Successfully retrieved transaction with ID: $id" } }
            .doOnError { logger.error(it) { "Error retrieving transaction with ID: $id" } }
    }

    /**
     * Retrieves all transactions associated with an account.
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all [TransactionResponseDto] objects associated with the account.
     */
    @GetMapping("/account/{accountId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTransactionsByAccount(@PathVariable accountId: Long): Flux<TransactionResponseDto> {
        logger.info { "Fetching transactions for account ID: $accountId" }
        return transactionService.getTransactionsByAccountId(accountId)
            .doOnComplete { logger.info { "Successfully retrieved transactions for account ID: $accountId" } }
            .doOnError { logger.error(it) { "Error retrieving transactions for account ID: $accountId" } }
    }

    /**
     * Retrieves outgoing transactions from an account.
     * Fixed endpoint path to match documentation: /account/{accountId}/outgoing
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all outgoing [TransactionResponseDto] objects from the account.
     */
    @GetMapping("/account/{accountId}/outgoing", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getOutgoingTransactions(@PathVariable accountId: Long): Flux<TransactionResponseDto> {
        logger.info { "Fetching outgoing transactions for account ID: $accountId" }
        return transactionService.getOutgoingTransactions(accountId)
            .doOnComplete { logger.info { "Successfully retrieved outgoing transactions for account ID: $accountId" } }
            .doOnError { logger.error(it) { "Error retrieving outgoing transactions for account ID: $accountId" } }
    }

    /**
     * Retrieves incoming transactions to an account.
     * Fixed endpoint path to match documentation: /account/{accountId}/incoming
     *
     * @param accountId The ID of the account.
     * @return A [Flux] emitting all incoming [TransactionResponseDto] objects to the account.
     */
    @GetMapping("/account/{accountId}/incoming", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getIncomingTransactions(@PathVariable accountId: Long): Flux<TransactionResponseDto> {
        logger.info { "Fetching incoming transactions for account ID: $accountId" }
        return transactionService.getIncomingTransactions(accountId)
            .doOnComplete { logger.info { "Successfully retrieved incoming transactions for account ID: $accountId" } }
            .doOnError { logger.error(it) { "Error retrieving incoming transactions for account ID: $accountId" } }
    }
}