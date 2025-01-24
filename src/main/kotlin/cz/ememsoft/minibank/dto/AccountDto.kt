package cz.ememsoft.minibank.dto

import cz.ememsoft.minibank.entity.ClientEntity
import jakarta.validation.constraints.NotNull
import java.io.Serializable

/**
 * Data Transfer Object (DTO) for representing an account.
 *
 * This DTO is used to transfer account data between different layers of the application.
 * It maps to the {@link cz.ememsoft.minibank.entity.AccountEntity} entity.
 *
 * @property id The unique identifier for the account.
 * @property clientEntity The client associated with this account. This field is mandatory.
 */
data class AccountDto(
    val id: Long,
    @NotNull(message = "clientEntity must not be null")
    val clientEntity: ClientEntity
) : Serializable
