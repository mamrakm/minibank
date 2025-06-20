package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.api.client.request.CreateClientRequestDto
import cz.ememsoft.minibank.api.client.response.CreateClientResponseDto
import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.entity.ClientEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

/**
 * Mapper interface for converting between Client entities and Data Transfer Objects (DTOs).
 *
 * This interface leverages MapStruct to automatically generate the mapping implementations
 * for converting between ClientEntity and various DTOs with explicit mappings.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface ClientMapper {

    /**
     * Converts a ClientEntity to a ClientDto with explicit field mappings.
     * Password hash is excluded for security reasons.
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "personalNumber", target = "personalNumber")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "enabled", target = "enabled")
    @Mapping(source = "createdAt", target = "createdAt")
    fun entityToDto(clientEntity: ClientEntity): ClientDto

    /**
     * Converts a ClientDto to a ClientEntity with explicit field mappings.
     * Password hash and audit fields need to be preserved from existing entity.
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "personalNumber", target = "personalNumber")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "enabled", target = "enabled")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    fun dtoToEntity(clientDto: ClientDto): ClientEntity

    /**
     * Converts a CreateClientRequestDto to a partial ClientEntity with explicit field mappings.
     * This is deprecated - use RegisterRequestDto instead for new client creation with authentication.
     */
    @Deprecated("Use AuthenticationService.register() instead")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "personalNumber", ignore = true)
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    fun createRequestToEntity(createClientRequestDto: CreateClientRequestDto): ClientEntity

    /**
     * Converts a ClientEntity to a CreateClientResponseDto with explicit field mappings.
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "personalNumber", target = "personalNumber")
    fun entityToCreateResponse(clientEntity: ClientEntity): CreateClientResponseDto

    /**
     * Converts a list of ClientEntity to a list of ClientDto.
     */
    fun entitiesToDtos(clientEntities: List<ClientEntity>): List<ClientDto>

    /**
     * Converts a list of ClientDto to a list of ClientEntity.
     */
    fun dtosToEntities(clientDtos: List<ClientDto>): List<ClientEntity>

    /**
     * Updates entity fields from DTO while preserving authentication and audit fields from existing entity.
     * This creates a new entity instance with updated data and preserved sensitive fields.
     */
    @Mapping(source = "existingEntity.id", target = "id")
    @Mapping(source = "updatedDto.firstName", target = "firstName")
    @Mapping(source = "updatedDto.lastName", target = "lastName")
    @Mapping(source = "updatedDto.email", target = "email")
    @Mapping(source = "existingEntity.passwordHash", target = "passwordHash")
    @Mapping(source = "updatedDto.role", target = "role")
    @Mapping(source = "updatedDto.phoneNumber", target = "phoneNumber")
    @Mapping(source = "updatedDto.address", target = "address")
    @Mapping(source = "updatedDto.dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "existingEntity.personalNumber", target = "personalNumber")
    @Mapping(source = "updatedDto.status", target = "status")
    @Mapping(source = "updatedDto.enabled", target = "enabled")
    @Mapping(source = "existingEntity.createdAt", target = "createdAt")
    @Mapping(source = "existingEntity.lastLoginAt", target = "lastLoginAt")
    fun mergeForUpdate(updatedDto: ClientDto, existingEntity: ClientEntity): ClientEntity
}