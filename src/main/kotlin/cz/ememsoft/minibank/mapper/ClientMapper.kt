package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.api.dto.request.CreateClientRequest
import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.entity.ClientEntity
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants

/**
 * Mapper interface for converting between API requests, DTOs, and entities related to clients.
 *
 * This interface uses MapStruct for mapping objects, ensuring seamless conversion
 * between different layers of the application. The generated implementation is registered
 * as a Spring Bean.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface ClientMapper {

    /**
     * Maps a [CreateClientRequest] to a [ClientDto].
     *
     * @param request The API request object containing client information.
     * @return The corresponding [ClientDto].
     */
    fun requestToDto(request: CreateClientRequest): ClientDto

    /**
     * Maps a [ClientDto] to a [ClientEntity].
     *
     * @param dto The internal DTO object containing client information.
     * @return The corresponding [ClientEntity] ready for persistence.
     */
    fun dtoToEntity(dto: ClientDto): ClientEntity

    /**
     * Maps a [ClientEntity] back to a [ClientDto].
     *
     * @param entity The entity object representing a client in the database.
     * @return The corresponding [ClientDto] for use in the application layers.
     */
    fun entityToDto(entity: ClientEntity): ClientDto
}
