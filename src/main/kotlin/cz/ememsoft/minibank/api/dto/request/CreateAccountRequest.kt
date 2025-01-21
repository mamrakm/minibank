package cz.ememsoft.minibank.api.dto.request

import java.math.BigDecimal

data class CreateAccountRequest(
    val clientId: Long,
    val accountType: String,
    val balance: BigDecimal,
    val currency: String
) {

    override fun toString(): String {
        return "CreateAccountRequest(clientId=$clientId, accountType='$accountType', currency='$currency')"
    }
}
