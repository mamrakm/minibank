package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.transaction.request.CreateTransactionRequestDto
import cz.ememsoft.minibank.transaction.response.CreateTransactionResponseDto
import reactor.core.publisher.Mono

/**
 * Service interface for handling financial transactions.
 *
 * Provides methods for processing new transactions.
 */
fun interface TransactionService {

    /**
     * Processes a new transaction between accounts.
     *
     * @param request The transaction request data.
     * @return A [Mono] emitting the transaction response.
     */
    fun createTransaction(request: CreateTransactionRequestDto): Mono<CreateTransactionResponseDto>
}
