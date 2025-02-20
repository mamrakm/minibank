package cz.ememsoft.minibank.api.account.response

import java.math.BigDecimal

data class CreateAccountResponseDto(
    val id: Long,
    val accountType: String,
    val balance: BigDecimal,
    val currency: String,
    val clientId: Long,
) {}