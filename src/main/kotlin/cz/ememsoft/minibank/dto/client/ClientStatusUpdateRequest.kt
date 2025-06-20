package cz.ememsoft.minibank.dto.client

import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import jakarta.validation.constraints.NotNull

/**
 * DTO for updating client status (admin operation).
 */
data class ClientStatusUpdateRequest(
    @field:NotNull(message = "Status must not be null")
    val status: ClientStatusEnum
)