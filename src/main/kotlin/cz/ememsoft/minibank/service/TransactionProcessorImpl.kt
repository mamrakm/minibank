package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.transaction.request.TransactionRequestDto
import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.entity.TransactionEntity
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import cz.ememsoft.minibank.enumeration.TransactionStatusEnum
import cz.ememsoft.minibank.exception.CurrencyMismatchException
import cz.ememsoft.minibank.exception.InsufficientFundsException
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.TransactionRepository
import cz.ememsoft.minibank.repository.saveAndReturnWithEnums
import cz.ememsoft.minibank.repository.updateAndReturnWithEnums
import cz.ememsoft.minibank.util.MoneyUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Component
class TransactionProcessorImpl(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) : TransactionProcessor {

    override fun validateTransaction(
        transferRequest: TransactionRequestDto,
        sourceAccount: AccountEntity,
        targetAccount: AccountEntity,
    ): Mono<Unit> = Mono.defer {
        try {
            val transactionAmount = MoneyUtils.validateAmount(transferRequest.amount)
            val requestedCurrencyString = MoneyUtils.validateCurrency(transferRequest.currency)

            val requestedCurrency = try {
                CurrencyEnum.valueOf(requestedCurrencyString)
            } catch (e: IllegalArgumentException) {
                return@defer Mono.error<Unit>(
                    CurrencyMismatchException("Invalid currency: ${transferRequest.currency}. " +
                            "Supported currencies: ${CurrencyEnum.entries.joinToString()}")
                )
            }

            if (sourceAccount.currency != requestedCurrency) {
                val errorMessage = "Currency mismatch: source account has ${sourceAccount.currency.name}, " +
                        "but transfer requests ${requestedCurrency.name}"
                logger.warn { errorMessage }
                return@defer Mono.error<Unit>(CurrencyMismatchException(errorMessage))
            }

            if (targetAccount.currency != requestedCurrency) {
                val errorMessage = "Currency mismatch: target account has ${targetAccount.currency.name}, " +
                        "but transfer requests ${requestedCurrency.name}"
                logger.warn { errorMessage }
                return@defer Mono.error<Unit>(CurrencyMismatchException(errorMessage))
            }

            if (!sourceAccount.hasSufficientFunds(transactionAmount)) {
                val errorMessage = "Insufficient funds in account ${sourceAccount.id}. " +
                        "Available: ${MoneyUtils.formatAmount(sourceAccount.balance, sourceAccount.currency.name)}, " +
                        "Required: ${MoneyUtils.formatAmount(transactionAmount, requestedCurrency.name)}"
                logger.warn { errorMessage }
                return@defer Mono.error<Unit>(InsufficientFundsException(errorMessage))
            }

            logger.debug {
                "Transaction validation passed for ${sourceAccount.id} -> ${targetAccount.id}, " +
                        "amount: ${MoneyUtils.formatAmount(transactionAmount, requestedCurrency.name)}"
            }
            Mono.just(Unit)

        } catch (exception: Exception) {
            logger.error(exception) { "Unexpected error during transaction validation" }
            Mono.error(exception)
        }
    }

    override fun createPendingTransaction(
        sourceAccountId: Long,
        targetAccountId: Long,
        amount: BigDecimal,
        currency: String,
        reference: String,
        timestamp: LocalDateTime,
    ): Mono<TransactionEntity> {
        logger.info {
            "Creating pending transaction: $sourceAccountId -> $targetAccountId, " +
                    "amount: ${MoneyUtils.formatAmount(amount, currency)}"
        }

        val safeAmount = MoneyUtils.validateAmount(amount)
        val safeCurrencyString = MoneyUtils.validateCurrency(currency)
        val safeCurrency = try {
            CurrencyEnum.valueOf(safeCurrencyString)
        } catch (e: IllegalArgumentException) {
            return Mono.error(IllegalArgumentException("Invalid currency: $currency. " +
                    "Supported currencies: ${CurrencyEnum.entries.joinToString()}"))
        }

        return transactionRepository.saveAndReturnWithEnums(
            sourceAccountId = sourceAccountId,
            targetAccountId = targetAccountId,
            amount = safeAmount,
            currency = safeCurrency,
            timestamp = timestamp,
            status = TransactionStatusEnum.PENDING,
            reference = reference
        )
            .doOnSuccess { logger.debug { "Created pending transaction with ID: ${it.id}" } }
            .doOnError { logger.error(it) { "Failed to create pending transaction" } }
    }

    @Transactional
    override fun updateAccountBalances(
        sourceAccount: AccountEntity,
        targetAccount: AccountEntity,
        amount: BigDecimal,
    ): Mono<Unit> {
        logger.info {
            "Updating account balances for transaction: ${sourceAccount.id} -> ${targetAccount.id}, " +
                    "amount: ${MoneyUtils.formatAmount(amount, sourceAccount.currency.name)}"
        }

        try {
            val updatedSourceAccount = sourceAccount.debitBalance(amount)
            val updatedTargetAccount = targetAccount.creditBalance(amount)

            return accountRepository.save(updatedSourceAccount)
                .then(accountRepository.save(updatedTargetAccount))
                .then(Mono.just(Unit))
                .doOnSuccess {
                    logger.info {
                        "Successfully updated account balances: " +
                                "Source ${sourceAccount.id}: ${MoneyUtils.formatAmount(sourceAccount.balance, sourceAccount.currency.name)} -> " +
                                "${MoneyUtils.formatAmount(updatedSourceAccount.balance, updatedSourceAccount.currency.name)}, " +
                                "Target ${targetAccount.id}: ${MoneyUtils.formatAmount(targetAccount.balance, targetAccount.currency.name)} -> " +
                                MoneyUtils.formatAmount(updatedTargetAccount.balance, updatedTargetAccount.currency.name)
                    }
                }
                .doOnError { logger.error(it) { "Failed to update account balances for transfer" } }

        } catch (exception: Exception) {
            logger.error(exception) { "Unexpected error during balance update" }
            return Mono.error(exception)
        }
    }

    override fun completeTransaction(transaction: TransactionEntity): Mono<TransactionEntity> {
        logger.info { "Completing transaction with ID: ${transaction.id}" }
        val transactionId = transaction.id

        return transactionRepository.updateAndReturnWithEnums(
            id = transactionId,
            sourceAccountId = transaction.sourceAccountId,
            targetAccountId = transaction.targetAccountId,
            amount = transaction.amount,
            currency = transaction.currency,
            timestamp = transaction.timestamp,
            status = TransactionStatusEnum.COMPLETED,
            reference = transaction.reference
        )
            .doOnSuccess { logger.info { "Successfully completed transaction with ID: ${it.id}" } }
            .doOnError { logger.error(it) { "Failed to complete transaction with ID: ${transaction.id}" } }
    }

    override fun failTransaction(transaction: TransactionEntity, errorMessage: String): Mono<TransactionEntity> {
        logger.warn { "Marking transaction with ID: ${transaction.id} as failed: $errorMessage" }
        val transactionId = transaction.id

        val enhancedReference = if (transaction.reference.isNotEmpty()) {
            "${transaction.reference} | Failed at ${LocalDateTime.now()}: $errorMessage"
        } else {
            "Failed at ${LocalDateTime.now()}: $errorMessage"
        }

        return transactionRepository.updateAndReturnWithEnums(
            id = transactionId,
            sourceAccountId = transaction.sourceAccountId,
            targetAccountId = transaction.targetAccountId,
            amount = transaction.amount,
            currency = transaction.currency,
            timestamp = transaction.timestamp,
            status = TransactionStatusEnum.FAILED,
            reference = enhancedReference
        )
            .doOnSuccess { logger.info { "Successfully marked transaction with ID: ${it.id} as failed" } }
            .doOnError { logger.error(it) { "Failed to mark transaction with ID: ${transaction.id} as failed" } }
    }
}