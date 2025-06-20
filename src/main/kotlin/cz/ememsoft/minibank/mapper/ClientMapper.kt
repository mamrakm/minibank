package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.dto.client.CreateClientProfileRequestDto
import cz.ememsoft.minibank.entity.ClientEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

/**
 * Mapper interface for converting between Client entities and Data Transfer Objects (DTOs).
 *
 * This interface leverages MapStruct to automatically generate the mapping implementations
 * for converting between ClientEntity and various DTOs. Updated for Keycloak integration.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface ClientMapper {

    /**
     * Converts a ClientEntity to a ClientDto.
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "keycloakUserId", target = "keycloakUserId")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdAt")
    fun entityToDto(clientEntity: ClientEntity): ClientDto

    /**
     * Converts a CreateClientProfileRequestDto to a ClientEntity for admin profile creation.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "keycloakUserId", target = "keycloakUserId")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    fun createProfileRequestToEntity(request: CreateClientProfileRequestDto): ClientEntity

    /**
     * Updates entity fields from DTO while preserving keycloak user ID and audit fields.
     */
    @Mapping(source = "existingEntity.id", target = "id")
    @Mapping(source = "existingEntity.keycloakUserId", target = "keycloakUserId")
    @Mapping(source = "updatedDto.firstName", target = "firstName")
    @Mapping(source = "updatedDto.lastName", target = "lastName")
    @Mapping(source = "updatedDto.email", target = "email")
    @Mapping(source = "updatedDto.role", target = "role")
    @Mapping(source = "updatedDto.phoneNumber", target = "phoneNumber")
    @Mapping(source = "updatedDto.address", target = "address")
    @Mapping(source = "updatedDto.dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "updatedDto.status", target = "status")
    @Mapping(source = "existingEntity.createdAt", target = "createdAt")
    fun mergeForUpdate(updatedDto: ClientDto, existingEntity: ClientEntity): ClientEntity

    /**
     * Converts a list of ClientEntity to a list of ClientDto.
     */
    fun entitiesToDtos(clientEntities: List<ClientEntity>): List<ClientDto>
}