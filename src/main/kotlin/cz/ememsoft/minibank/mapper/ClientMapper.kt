package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.api.dto.CustomerDto
import cz.ememsoft.minibank.api.dto.request.ClientSaveRequest
import cz.ememsoft.minibank.entity.ClientEntity
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface ClientMapper {

    // Map API request to internal DTO
    fun toDto(request: ClientSaveRequest): CustomerDto

    // Map DTO to Entity
    fun toEntity(dto: CustomerDto): ClientEntity

    // Map Entity back to DTO
    fun toDtoFromEntity(entity: ClientEntity): CustomerDto
}
