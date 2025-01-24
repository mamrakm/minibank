package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.api.dto.request.CreateAccountRequest
import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.entity.AccountEntity
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy

/**
 * Mapper for converting between [AccountDto], [AccountEntity], and related objects.
 *
 * This abstract class leverages MapStruct to automatically generate the implementation for:
 * - Mapping between [AccountDto] and [AccountEntity]
 * - Partially updating an [AccountEntity] from an [AccountDto]
 * - Mapping from a [CreateAccountRequest] to [AccountDto] and [AccountEntity]
 *
 * ### Configuration:
 * - **Unmapped Target Policy**: [ReportingPolicy.IGNORE] - Unmapped properties will be ignored.
 * - **Spring Component**: The generated implementation is registered as a Spring Bean using
 *   `componentModel = MappingConstants.ComponentModel.SPRING`.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class AccountMapper {

    /**
     * Maps an [AccountDto] to an [AccountEntity].
     *
     * @param accountDto The DTO to be converted.
     * @return The resulting [AccountEntity].
     */
    abstract fun dtoToEntity(accountDto: AccountDto): AccountEntity

    /**
     * Maps an [AccountEntity] to an [AccountDto].
     *
     * @param accountEntity The entity to be converted.
     * @return The resulting [AccountDto].
     */
    abstract fun entityToDto(accountEntity: AccountEntity): AccountDto

    /**
     * Partially updates an [AccountEntity] with non-null values from an [AccountDto].
     *
     * This method ensures that only the non-null properties in the [AccountDto] overwrite
     * the corresponding properties in the target [AccountEntity].
     *
     * ### Configuration:
     * - **Null Value Handling**: [NullValuePropertyMappingStrategy.IGNORE] ensures null properties in the DTO
     *   do not overwrite existing values in the target entity.
     *
     * @param accountEntityDto The DTO containing updated values.
     * @param accountEntity The target entity to be updated.
     * @return The updated [AccountEntity].
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract fun partialUpdate(
        accountEntityDto: AccountDto,
        @MappingTarget accountEntity: AccountEntity
    ): AccountEntity

    /**
     * Maps a [CreateAccountRequest] to an [AccountDto].
     *
     * @param request The request object containing account creation details.
     * @return The resulting [AccountDto].
     */
    abstract fun requestToDto(request: CreateAccountRequest): AccountDto

    /**
     * Maps a [CreateAccountRequest] to an [AccountEntity].
     *
     * @param request The request object containing account creation details.
     * @return The resulting [AccountEntity].
     */
    abstract fun requestToEntity(request: CreateAccountRequest): AccountEntity
}
