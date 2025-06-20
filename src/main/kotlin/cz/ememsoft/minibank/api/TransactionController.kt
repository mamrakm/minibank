package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.transaction.request.TransactionRequestDto
import cz.ememsoft.minibank.api.transaction.response.TransactionResponseDto
import cz.ememsoft.minibank.service.TransactionService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
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

    @PostMapping("/transfer", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionService.isAccountOwner(authentication, #transactionRequest.sourceAccountId)")
    fun processTransaction(@RequestBody transactionRequest: TransactionRequestDto): Mono<TransactionResponseDto> {
        logger.info { "Processing transaction request: $transactionRequest" }
        return transactionService.processTransaction(transactionRequest)
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ADMIN')") // Only admins can fetch arbitrary transaction by ID
    fun getTransaction(@PathVariable id: Long): Mono<TransactionResponseDto> {
        logger.info { "Fetching transaction with ID: $id" }
        return transactionService.getTransactionById(id)
    }

    @GetMapping("/account/{accountId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ADMIN') or @permissionService.isAccountOwner(authentication, #accountId)")
    fun getTransactionsByAccount(@PathVariable accountId: Long): Flux<TransactionResponseDto> {
        logger.info { "Fetching transactions for account ID: $accountId" }
        return transactionService.getTransactionsByAccountId(accountId)
    }

    @GetMapping("/account/{accountId}/outgoing", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ADMIN') or @permissionService.isAccountOwner(authentication, #accountId)")
    fun getOutgoingTransactions(@PathVariable accountId: Long): Flux<TransactionResponseDto> {
        logger.info { "Fetching outgoing transactions for account ID: $accountId" }
        return transactionService.getOutgoingTransactions(accountId)
    }

    @GetMapping("/account/{accountId}/incoming", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ADMIN') or @permissionService.isAccountOwner(authentication, #accountId)")
    fun getIncomingTransactions(@PathVariable accountId: Long): Flux<TransactionResponseDto> {
        logger.info { "Fetching incoming transactions for account ID: $accountId" }
        return transactionService.getIncomingTransactions(accountId)
    }
}
