package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.entity.AccountEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

/**
 * Mapper interface for converting between [AccountEntity] and [AccountDto].
 *
 * This interface uses MapStruct to automatically generate the mapping implementation.
 * - The `componentModel` is set to Spring for dependency injection.
 * - Unmapped properties are ignored to prevent compilation issues.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface AccountMapper {

    /**
     * Maps an [AccountEntity] to an [AccountDto].
     *
     * @param accountEntity The entity to be mapped.
     * @return The mapped [AccountDto].
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "clientId", target = "clientId")
    @Mapping(source = "balance", target = "balance")
    @Mapping(source = "accountType", target = "accountType")
    fun entityToDto(accountEntity: AccountEntity): AccountDto

    /**
     * Maps an [AccountDto] to an [AccountEntity].
     *
     * @param accountDto The DTO to be mapped.
     * @return The mapped [AccountEntity].
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "clientId", target = "clientId")
    @Mapping(source = "balance", target = "balance")
    @Mapping(source = "accountType", target = "accountType")
    fun dtoToEntity(accountDto: AccountDto): AccountEntity
}
