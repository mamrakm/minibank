package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.enumeration.TransactionStatusEnum
import cz.ememsoft.minibank.exception.AccountNotFoundException
import cz.ememsoft.minibank.exception.CurrencyMismatchException
import cz.ememsoft.minibank.exception.InsufficientFundsException
import cz.ememsoft.minibank.mapper.TransactionMapper
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.transaction.entity.TransactionEntity
import cz.ememsoft.minibank.transaction.repository.TransactionRepository
import cz.ememsoft.minibank.transaction.request.CreateTransactionRequestDto
import cz.ememsoft.minibank.transaction.response.CreateTransactionResponseDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * Service implementation for processing financial transactions.
 *
 * This implementation ensures that a transaction record is created with an initial
 * status of PENDING. After validating the involved accounts and updating their balances,
 * the transaction record is updated to SUCCESS. If an error occurs during processing,
 * the transaction record is updated to FAILED.
 *
 * @property accountRepository Repository for accessing account data.
 * @property transactionRepository Repository for managing transaction records.
 * @property transactionMapper Mapper for converting transaction entities to response DTOs.
 */
@Service
@Transactional
class TransactionServiceImpl(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionMapper: TransactionMapper
) : TransactionService {

    /**
     * Processes a new transaction between accounts.
     *
     * The processing flow is as follows:
     * 1. Create an initial transaction record with status PENDING.
     * 2. Fetch and validate the sender and recipient accounts.
     * 3. Update account balances.
     * 4. Update the transaction record status to SUCCESS.
     * 5. In case of any error, update the transaction record status to FAILED.
     *
     * @param request The transaction request data.
     * @return A [Mono] emitting the transaction response.
     * @throws AccountNotFoundException if either account is not found.
     * @throws InsufficientFundsException if the sender has insufficient funds.
     * @throws CurrencyMismatchException if there is a currency mismatch between accounts.
     */
    override fun createTransaction(request: CreateTransactionRequestDto): Mono<CreateTransactionResponseDto> {
        logger.info { "Initiating transaction: $request" }
        // Step 1: Create a pending transaction record.
        return transactionRepository.save(
            TransactionEntity(
                fromAccountId = request.fromAccountId,
                toAccountId = request.toAccountId,
                amount = request.amount,
                currency = request.currency,
                status = TransactionStatusEnum.PENDING
            )
        ).flatMap { pendingTransaction ->
            // Step 2: Fetch and validate sender and recipient accounts.
            fetchAndValidateAccounts(request)
                .flatMap { (sender, recipient) ->
                    // Step 3: Update account balances.
                    updateAccountsBalances(request, sender, recipient)
                }
                .then(Mono.defer { transactionRepository.save(pendingTransaction.copy(status = TransactionStatusEnum.SUCCESS)) })
                .onErrorResume { ex ->
                    // Step 5: On error, update the pending transaction record to FAILED and propagate the error.
                    transactionRepository.save(pendingTransaction.copy(status = TransactionStatusEnum.FAILED))
                        .then(Mono.error(ex))
                }
        }
            .map { transactionEntity ->
                transactionMapper.entityToResponseDto(transactionEntity)
            }
            .doOnSuccess { logger.info { "Transaction completed successfully: $it" } }
            .doOnError { ex -> logger.error(ex) { "Transaction failed: ${ex.message}" } }
    }

    /**
     * Fetches and validates the sender and recipient accounts.
     *
     * Validates that:
     * - The sender account exists and has sufficient funds.
     * - The recipient account exists.
     * - Both accounts use the currency specified in the request.
     *
     * @param request The transaction request data.
     * @return A [Mono] emitting a [Pair] of sender and recipient accounts if validations pass.
     */
    private fun fetchAndValidateAccounts(request: CreateTransactionRequestDto): Mono<Pair<AccountEntity, AccountEntity>> {
        return accountRepository.findById(request.fromAccountId)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Sender account not found.")))
            .flatMap { sender ->
                if (sender.balance < request.amount) {
                    Mono.error(InsufficientFundsException("Insufficient funds in sender account."))
                } else {
                    accountRepository.findById(request.toAccountId)
                        .switchIfEmpty(Mono.error(AccountNotFoundException("Recipient account not found.")))
                        .flatMap { recipient ->
                            if (sender.currency != request.currency || recipient.currency != request.currency) {
                                Mono.error(CurrencyMismatchException("Currency mismatch between accounts."))
                            } else {
                                Mono.just(Pair(sender, recipient))
                            }
                        }
                }
            }
    }

    /**
     * Updates the account balances for both sender and recipient.
     *
     * Creates updated immutable copies of the sender and recipient accounts with new balances,
     * then saves these accounts.
     *
     * @param request The transaction request data.
     * @param sender The validated sender account.
     * @param recipient The validated recipient account.
     * @return A [Mono] signaling completion of the balance update operations.
     */
    private fun updateAccountsBalances(
        request: CreateTransactionRequestDto,
        sender: AccountEntity,
        recipient: AccountEntity
    ): Mono<Void> {
        val updatedSender = sender.copy(balance = sender.balance.subtract(request.amount))
        val updatedRecipient = recipient.copy(balance = recipient.balance.add(request.amount))
        return accountRepository.save(updatedSender)
            .then(accountRepository.save(updatedRecipient))
            .then()
    }
}
