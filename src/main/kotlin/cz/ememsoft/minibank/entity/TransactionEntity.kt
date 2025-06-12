package cz.ememsoft.minibank.entity

import cz.ememsoft.minibank.enumeration.CurrencyEnum
import cz.ememsoft.minibank.enumeration.TransactionStatusEnum
import cz.ememsoft.minibank.util.MoneyUtils
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table(name = "transaction", schema = "bank")
data class TransactionEntity(
    @Id
    @Column("id")
    val id: Long,
    @Column("source_account_id")
    val sourceAccountId: Long,
    @Column("target_account_id")
    val targetAccountId: Long,
    @Column("amount")
    val amount: BigDecimal,
    @Column("currency")
    private val currencyOrdinal: Int,
    @Column("timestamp")
    val timestamp: LocalDateTime,
    @Column("status")
    private val statusOrdinal: Int,
    @Column("reference")
    val reference: String
) {
    init {
        validateOrdinals()
    }

    val currency: CurrencyEnum
        get() = CurrencyEnum.entries.getOrNull(currencyOrdinal)
            ?: throw IllegalStateException("Invalid currency ordinal: $currencyOrdinal")

    val status: TransactionStatusEnum
        get() = TransactionStatusEnum.entries.getOrNull(statusOrdinal)
            ?: throw IllegalStateException("Invalid status ordinal: $statusOrdinal")

    // Internal accessor for repository operations
    internal fun getCurrencyOrdinal(): Int = currencyOrdinal
    internal fun getStatusOrdinal(): Int = statusOrdinal

    private fun validateOrdinals() {
        require(currencyOrdinal in CurrencyEnum.entries.toTypedArray().indices) {
            "Invalid currency ordinal: $currencyOrdinal. Valid range: 0-${CurrencyEnum.entries.size - 1}"
        }
        require(statusOrdinal in TransactionStatusEnum.entries.toTypedArray().indices) {
            "Invalid status ordinal: $statusOrdinal. Valid range: 0-${TransactionStatusEnum.entries.size - 1}"
        }
    }

    companion object {
        fun create(
            id: Long,
            sourceAccountId: Long,
            targetAccountId: Long,
            amount: BigDecimal,
            currency: CurrencyEnum,
            timestamp: LocalDateTime,
            status: TransactionStatusEnum,
            reference: String
        ): TransactionEntity {
            require(sourceAccountId != targetAccountId) {
                "Source and target accounts must be different"
            }

            return TransactionEntity(
                id = id,
                sourceAccountId = sourceAccountId,
                targetAccountId = targetAccountId,
                amount = MoneyUtils.validateAmount(amount),
                currencyOrdinal = currency.ordinal,
                timestamp = timestamp,
                statusOrdinal = status.ordinal,
                reference = reference
            )
        }

        fun create(
            id: Long,
            sourceAccountId: Long,
            targetAccountId: Long,
            amount: BigDecimal,
            currency: String,
            timestamp: LocalDateTime,
            status: TransactionStatusEnum,
            reference: String
        ): TransactionEntity {
            val validatedCurrency = MoneyUtils.validateCurrency(currency)
            val currencyEnum = CurrencyEnum.entries.find { it.name == validatedCurrency }
                ?: throw IllegalArgumentException("Unsupported currency: $currency. " +
                        "Supported currencies: ${CurrencyEnum.entries.joinToString { it.name }}")

            return create(id, sourceAccountId, targetAccountId, amount, currencyEnum, timestamp, status, reference)
        }
    }

    fun withStatus(newStatus: TransactionStatusEnum): TransactionEntity {
        return copy(statusOrdinal = newStatus.ordinal)
    }

    fun withReference(newReference: String): TransactionEntity {
        return copy(reference = newReference)
    }

    fun getFormattedAmount(): String {
        return MoneyUtils.formatAmount(amount, currency.name)
    }

    fun involvesAccount(accountId: Long): Boolean {
        return sourceAccountId == accountId || targetAccountId == accountId
    }

    fun getCurrencyString(): String = currency.name

    override fun toString(): String {
        return "TransactionEntity(id=$id, sourceAccountId=$sourceAccountId, targetAccountId=$targetAccountId, " +
                "amount=$amount, currency=${currency.name}, timestamp=$timestamp, status=${status.name}, reference='$reference')"
    }
}