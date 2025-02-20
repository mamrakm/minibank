package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.api.client.request.CreateClientRequestDto
import cz.ememsoft.minibank.api.client.response.CreateClientResponseDto
import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.entity.ClientEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

/**
 * Mapper interface for converting between API requests, DTOs, and entities related to clients.
 *
 * This interface utilizes MapStruct for mapping objects, ensuring efficient and type-safe
 * conversions between different layers of the application. The generated implementation
 * is registered as a Spring Bean using `componentModel = MappingConstants.ComponentModel.SPRING`.
 *
 * ## Mapping Capabilities:
 * - **API Layer ↔ DTOs**: Converts API request objects to internal DTOs.
 * - **DTOs ↔ Entities**: Handles persistence-related mappings.
 * - **Entities ↔ DTOs**: Transforms database records into application-friendly objects.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface ClientMapper {

    /**
     * Maps a [CreateClientRequestDto] to a [ClientDto].
     *
     * This method converts an incoming API request containing client information
     * into an internal DTO format used within the service layer.
     *
     * @param request The API request object containing client details.
     * @return A corresponding [ClientDto] instance.
     */
    fun requestToDto(request: CreateClientRequestDto): ClientDto

    /**
     * Converts a [ClientDto] to a [ClientEntity] for persistence.
     *
     * This mapping is used when saving or updating client data in the database.
     *
     * @param dto The internal DTO object containing client details.
     * @return A [ClientEntity] instance ready for database persistence.
     */
    @Mapping(target = "id", ignore = true)
    fun dtoToEntity(dto: ClientDto): ClientEntity

    /**
     * Converts a [ClientEntity] retrieved from the database to a [ClientDto].
     *
     * This transformation ensures the service and API layers receive a structured DTO
     * rather than a direct database entity.
     *
     * @param entity The entity object representing a stored client.
     * @return A corresponding [ClientDto] for application use.
     */
    fun entityToDto(entity: ClientEntity): ClientDto

    /**
     * Maps a [ClientDto] to a [CreateClientResponseDto] for API responses.
     *
     * This method prepares response objects that will be returned to clients after
     * creating a new client entity.
     *
     * @param dto The DTO containing client data.
     * @return A [CreateClientResponseDto] object to be sent as an API response.
     */
    fun dtoToCreateResponse(dto: ClientDto): CreateClientResponseDto

    /**
     * Maps a [ClientEntity] to a [CreateClientResponseDto].
     *
     * This method converts a database entity representing a client into a response DTO
     * used for API responses. It ensures that only the necessary client data is exposed
     * in the response.
     *
     * @param entity The [ClientEntity] object to be converted.
     * @return The corresponding [CreateClientResponseDto] containing client details.
     */
    fun entityToCreateResponse(entity: ClientEntity): CreateClientResponseDto

}
