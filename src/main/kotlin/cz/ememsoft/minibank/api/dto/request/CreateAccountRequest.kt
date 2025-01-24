package cz.ememsoft.minibank.api.dto.request

import java.math.BigDecimal

data class CreateAccountRequest(
    val accountType: String,
    val balance: BigDecimal,
    val currency: String,
    val clientId: Long,
) {
    override fun toString(): String {
        return "CreateAccountRequest(clientId=$clientId, accountType='$accountType', currency='$currency')"
    }
}
