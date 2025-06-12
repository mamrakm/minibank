package cz.ememsoft.minibank.entity

import cz.ememsoft.minibank.enumeration.AccountTypeEnum
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import cz.ememsoft.minibank.util.MoneyUtils
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

/**
 * Represents a bank account entity with money-safe operations using MoneyUtils.
 */
@Table(name = "account", schema = "bank")
data class AccountEntity(
    @Id
    @Column("id")
    val id: Long,
    @Column("account_name")
    val name: String,
    @Column("client_id")
    val clientId: Long,
    @Column("balance")
    val balance: BigDecimal,
    @Column("account_type")
    private val accountTypeOrdinal: Int,
    @Column("currency")
    private val currencyOrdinal: Int
) {
    @get:Transient
    val accountType: AccountTypeEnum
        get() = AccountTypeEnum.entries[accountTypeOrdinal]

    @get:Transient
    val currency: CurrencyEnum
        get() = CurrencyEnum.entries[currencyOrdinal]

    companion object {
        /**
         * Creates a new AccountEntity with money-safe balance using MoneyUtils.
         */
        fun create(
            id: Long,
            name: String,
            clientId: Long,
            balance: BigDecimal,
            accountType: AccountTypeEnum,
            currency: CurrencyEnum
        ): AccountEntity {
            return AccountEntity(
                id = id,
                name = name,
                clientId = clientId,
                balance = MoneyUtils.validateAmount(balance, allowZero = true),
                accountTypeOrdinal = accountType.ordinal,
                currencyOrdinal = currency.ordinal
            )
        }
    }

    /**
     * Creates a copy with a new balance using MoneyUtils validation.
     */
    fun withBalance(newBalance: BigDecimal): AccountEntity {
        return copy(balance = MoneyUtils.validateAmount(newBalance, allowZero = true))
    }

    /**
     * Safely adds an amount to the current balance using MoneyUtils.
     */
    fun creditBalance(amount: BigDecimal): AccountEntity {
        val newBalance = MoneyUtils.add(balance, amount)
        return withBalance(newBalance)
    }

    /**
     * Safely subtracts an amount from the current balance using MoneyUtils.
     */
    fun debitBalance(amount: BigDecimal): AccountEntity {
        val newBalance = MoneyUtils.subtract(balance, amount)
        return withBalance(newBalance)
    }

    /**
     * Checks if the account has sufficient funds using MoneyUtils.
     */
    fun hasSufficientFunds(amount: BigDecimal): Boolean {
        return MoneyUtils.hasSufficientFunds(balance, amount)
    }

    /**
     * Returns formatted balance with currency using MoneyUtils.
     */
    fun getFormattedBalance(): String {
        return MoneyUtils.formatAmount(balance, currency.name)
    }
}