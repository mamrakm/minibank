package cz.ememsoft.minibank.transaction.api

import cz.ememsoft.minibank.service.TransactionService
import cz.ememsoft.minibank.transaction.request.CreateTransactionRequestDto
import cz.ememsoft.minibank.transaction.response.CreateTransactionResponseDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * REST controller for handling transactions between accounts.
 *
 * Provides an endpoint for initiating financial transactions in a reactive manner.
 *
 * @property transactionService The service handling transaction operations.
 */
@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    /**
     * Initiates a financial transaction between two accounts.
     *
     * Validates the transaction request, processes the funds transfer, and returns
     * details of the completed transaction.
     *
     * @param request The transaction request data.
     * @return A [Mono] emitting the transaction response containing transaction details.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createTransaction(@RequestBody request: CreateTransactionRequestDto): Mono<CreateTransactionResponseDto> {
        logger.info { "Received transaction request: $request" }
        return transactionService.createTransaction(request)
    }
}
