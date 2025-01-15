package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.api.dto.CustomerDto
import cz.ememsoft.minibank.api.dto.request.CustomerSaveRequest
import cz.ememsoft.minibank.entity.CustomerEntity
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface CustomerMapper {

    // Map API request to internal DTO
    fun toDto(request: CustomerSaveRequest): CustomerDto

    // Map DTO to Entity
    fun toEntity(dto: CustomerDto): CustomerEntity

    // Map Entity back to DTO
    fun toDtoFromEntity(entity: CustomerEntity): CustomerDto
}
