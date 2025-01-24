package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.api.dto.request.ClientSaveRequest
import cz.ememsoft.minibank.dto.ClientDto
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants

/**
 * Mapper for converting between [ClientSaveRequest] and [ClientDto].
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface ClientRequestToDtoMapper {

    /**
     * Maps a [ClientSaveRequest] to a [ClientDto].
     *
     * @param request The request object containing client details.
     * @return The corresponding [ClientDto].
     */
    fun toDto(request: ClientSaveRequest): ClientDto
}
