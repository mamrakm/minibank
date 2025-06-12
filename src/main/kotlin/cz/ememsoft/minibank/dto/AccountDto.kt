package cz.ememsoft.minibank.dto

import cz.ememsoft.minibank.enumeration.AccountTypeEnum
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

/**
 * Data Transfer Object (DTO) for representing an account.
 *
 * This DTO is used to transfer account data between different layers of the application.
 * It maps to the {@link cz.ememsoft.minibank.entity. AccountEntity} entity.
 *
 * @property id The unique identifier for the account.
 * @property name The name of the account.
 * @property clientId The ID of the client associated with this account. This field is mandatory.
 * @property balance The current balance of the account.
 * @property accountType The type of the account, represented as an [AccountTypeEnum].
 * @property currency The currency of the account, represented as a [CurrencyEnum].
 */
data class AccountDto(
    val id: Long,

    @NotNull(message = "Account name must not be null")
    val name: String,

    @NotNull(message = "Client ID must not be null")
    val clientId: Long,

    @NotNull(message = "Balance must not be null")
    val balance: BigDecimal,

    @NotNull(message = "Account type must not be null")
    val accountType: AccountTypeEnum,

    @NotNull(message = "Currency must not be null")
    val currency: CurrencyEnum
) {
    override fun toString(): String {
        return "AccountDto(id=$id, name='$name', clientId=$clientId, balance=$balance, accountType=$accountType, currency=$currency)"
    }
}