package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.entity.AccountEntity
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy

/**
 * Mapper for converting between [AccountDto] and [AccountEntity].
 *
 * This abstract class uses MapStruct to generate the implementation for mapping between
 * the `AccountDto` and `AccountEntity` objects. It includes full mapping, partial updates,
 * and configuration for handling null properties.
 *
 * - The `unmappedTargetPolicy` is set to [ReportingPolicy.IGNORE], meaning unmapped properties
 *   will not cause compilation errors.
 * - The generated implementation is registered as a Spring Bean using
 *   `componentModel = MappingConstants.ComponentModel.SPRING`.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class AccountMapper {

    /**
     * Maps an [AccountDto] to an [AccountEntity].
     *
     * @param accountEntityDto The DTO to be mapped.
     * @return The mapped [AccountEntity].
     */
    abstract fun toEntity(accountEntityDto: AccountDto): AccountEntity

    /**
     * Maps an [AccountEntity] to an [AccountDto].
     *
     * @param accountEntity The entity to be mapped.
     * @return The mapped [AccountDto].
     */
    abstract fun toDto(accountEntity: AccountEntity): AccountDto

    /**
     * Partially updates an [AccountEntity] with values from an [AccountDto].
     *
     * Only non-null properties in the [AccountDto] will overwrite corresponding
     * properties in the target [AccountEntity].
     *
     * - Uses the [NullValuePropertyMappingStrategy.IGNORE] to ignore null values
     *   during the mapping process.
     *
     * @param accountEntityDto The DTO containing the updated values.
     * @param accountEntity The target entity to be updated.
     * @return The updated [AccountEntity].
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract fun partialUpdate(
        accountEntityDto: AccountDto,
        @MappingTarget accountEntity: AccountEntity
    ): AccountEntity
}
