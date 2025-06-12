package cz.ememsoft.minibank.api.client.response

import java.time.LocalDate
import java.util.UUID

/**
 * Response DTO for client creation operations.
 *
 * This DTO is returned after successfully creating a new client,
 * containing the generated ID and all client information.
 */
data class CreateClientResponseDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String?,
    val address: String?,
    val dateOfBirth: LocalDate?,
    val personalNumber: UUID
) {
    override fun toString(): String {
        return "CreateClientResponseDto(id=$id, firstName='$firstName', lastName='$lastName', email='$email', phoneNumber='$phoneNumber', address='$address', dateOfBirth=$dateOfBirth, personalNumber=$personalNumber)"
    }
}