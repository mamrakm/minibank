package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.TransactionEntity
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import cz.ememsoft.minibank.enumeration.TransactionStatusEnum
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

@Repository
interface TransactionRepository : R2dbcRepository<TransactionEntity, Long> {

    @Query("SELECT * FROM bank.transaction WHERE source_account_id = :accountId OR target_account_id = :accountId ORDER BY timestamp DESC")
    fun findByAccountId(accountId: Long): Flux<TransactionEntity>

    fun findBySourceAccountId(accountId: Long): Flux<TransactionEntity>

    fun findByTargetAccountId(accountId: Long): Flux<TransactionEntity>

    @Query(
        "INSERT INTO bank.transaction " +
                "(source_account_id, target_account_id, amount, currency, timestamp, status, reference) " +
                "VALUES (:sourceAccountId, :targetAccountId, :amount, :currencyOrdinal, :timestamp, :statusOrdinal, :reference) " +
                "RETURNING id, source_account_id, target_account_id, amount, currency, timestamp, status, reference"
    )
    fun saveAndReturnWithOrdinals(
        sourceAccountId: Long,
        targetAccountId: Long,
        amount: BigDecimal,
        currencyOrdinal: Int,
        timestamp: LocalDateTime,
        statusOrdinal: Int,
        reference: String
    ): Mono<TransactionEntity>

    // Add the missing update method
    @Query(
        "UPDATE bank.transaction SET " +
                "source_account_id = :sourceAccountId, " +
                "target_account_id = :targetAccountId, " +
                "amount = :amount, " +
                "currency = :currencyOrdinal, " +
                "timestamp = :timestamp, " +
                "status = :statusOrdinal, " +
                "reference = :reference " +
                "WHERE id = :id " +
                "RETURNING id, source_account_id, target_account_id, amount, currency, timestamp, status, reference"
    )
    fun updateAndReturnWithOrdinals(
        id: Long,
        sourceAccountId: Long,
        targetAccountId: Long,
        amount: BigDecimal,
        currencyOrdinal: Int,
        timestamp: LocalDateTime,
        statusOrdinal: Int,
        reference: String
    ): Mono<TransactionEntity>

    @Query(
        "SELECT * FROM bank.transaction " +
                "WHERE (source_account_id = :accountId OR target_account_id = :accountId) " +
                "AND timestamp BETWEEN :startDate AND :endDate " +
                "ORDER BY timestamp DESC"
    )
    fun findByAccountIdAndTimestampBetween(
        accountId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flux<TransactionEntity>

    @Query(
        "SELECT * FROM bank.transaction " +
                "WHERE amount BETWEEN :minAmount AND :maxAmount " +
                "ORDER BY timestamp DESC"
    )
    fun findByAmountBetween(
        minAmount: BigDecimal,
        maxAmount: BigDecimal
    ): Flux<TransactionEntity>

    @Query("SELECT * FROM bank.transaction WHERE currency = :currencyOrdinal")
    fun findByCurrencyOrdinal(currencyOrdinal: Int): Flux<TransactionEntity>

    @Query("SELECT * FROM bank.transaction WHERE status = :statusOrdinal")
    fun findByStatusOrdinal(statusOrdinal: Int): Flux<TransactionEntity>

    @Query("SELECT * FROM bank.transaction WHERE currency = :currencyOrdinal AND status = :statusOrdinal")
    fun findByCurrencyOrdinalAndStatusOrdinal(currencyOrdinal: Int, statusOrdinal: Int): Flux<TransactionEntity>

    @Query("SELECT * FROM bank.transaction WHERE currency = :currencyOrdinal AND amount BETWEEN :minAmount AND :maxAmount ORDER BY timestamp DESC")
    fun findByCurrencyOrdinalAndAmountBetween(
        currencyOrdinal: Int,
        minAmount: BigDecimal,
        maxAmount: BigDecimal
    ): Flux<TransactionEntity>

    @Query("SELECT COUNT(*) FROM bank.transaction WHERE currency = :currencyOrdinal")
    fun countByCurrencyOrdinal(currencyOrdinal: Int): Mono<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM bank.transaction WHERE currency = :currencyOrdinal AND status = :statusOrdinal")
    fun getTotalAmountByCurrencyOrdinalAndStatusOrdinal(currencyOrdinal: Int, statusOrdinal: Int): Mono<BigDecimal>
}

// Extension functions for enum-based convenience methods
fun TransactionRepository.findByCurrency(currency: CurrencyEnum): Flux<TransactionEntity> =
    findByCurrencyOrdinal(currency.ordinal)

fun TransactionRepository.findByStatus(status: TransactionStatusEnum): Flux<TransactionEntity> =
    findByStatusOrdinal(status.ordinal)

fun TransactionRepository.findByCurrencyAndStatus(currency: CurrencyEnum, status: TransactionStatusEnum): Flux<TransactionEntity> =
    findByCurrencyOrdinalAndStatusOrdinal(currency.ordinal, status.ordinal)

fun TransactionRepository.findByCurrencyAndAmountBetween(
    currency: CurrencyEnum,
    minAmount: BigDecimal,
    maxAmount: BigDecimal
): Flux<TransactionEntity> = findByCurrencyOrdinalAndAmountBetween(currency.ordinal, minAmount, maxAmount)

fun TransactionRepository.saveAndReturnWithEnums(
    sourceAccountId: Long,
    targetAccountId: Long,
    amount: BigDecimal,
    currency: CurrencyEnum,
    timestamp: LocalDateTime,
    status: TransactionStatusEnum,
    reference: String
): Mono<TransactionEntity> = saveAndReturnWithOrdinals(
    sourceAccountId, targetAccountId, amount,
    currency.ordinal, timestamp, status.ordinal, reference
)

// Add the missing updateAndReturnWithEnums extension function
fun TransactionRepository.updateAndReturnWithEnums(
    id: Long,
    sourceAccountId: Long,
    targetAccountId: Long,
    amount: BigDecimal,
    currency: CurrencyEnum,
    timestamp: LocalDateTime,
    status: TransactionStatusEnum,
    reference: String
): Mono<TransactionEntity> = updateAndReturnWithOrdinals(
    id, sourceAccountId, targetAccountId, amount,
    currency.ordinal, timestamp, status.ordinal, reference
)

fun TransactionRepository.countByCurrency(currency: CurrencyEnum): Mono<Long> =
    countByCurrencyOrdinal(currency.ordinal)

fun TransactionRepository.getTotalAmountByCurrencyAndStatus(currency: CurrencyEnum, status: TransactionStatusEnum): Mono<BigDecimal> =
    getTotalAmountByCurrencyOrdinalAndStatusOrdinal(currency.ordinal, status.ordinal)