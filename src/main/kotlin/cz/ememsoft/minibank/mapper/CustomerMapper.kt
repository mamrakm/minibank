import cz.ememsoft.minibank.api.dto.request.CustomerSaveRequest
import cz.ememsoft.minibank.dto.CustomerDto
import cz.ememsoft.minibank.entity.CustomerEntity
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
abstract class CustomerMapper {

    // Map API request to internal DTO
    abstract fun toDto(request: CustomerSaveRequest): CustomerDto

    // Map DTO to Entity
    abstract fun toEntity(dto: CustomerDto): CustomerEntity

    // Map Entity back to DTO
    abstract fun toDtoFromEntity(entity: CustomerEntity): CustomerDto
}
