package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.account.request.CreateAccountRequestDto
import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.entity.AccountEntity
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

/**
 * Mapper interface for converting between Account entities and Data Transfer Objects (DTOs).
 *
 * <p>
 * This interface leverages MapStruct to automatically generate the mapping implementations
 * for converting between the following types:
 * <ul>
 *   <li>[AccountEntity] and [AccountDto]</li>
 *   <li>[CreateAccountRequestDto] and [AccountEntity]</li>
 * </ul>
 * </p>
 *
 * <p>
 * The component model is configured for Spring, enabling dependency injection of the generated mapper.
 * Unmapped target properties are ignored to prevent compilation issues.
 * </p>
 *
 * @see AccountEntity
 * @see AccountDto
 * @see CreateAccountRequestDto
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface AccountMapper {

    /**
     * Converts an [AccountEntity] to an [AccountDto].
     *
     * @param accountEntity the account entity to be converted.
     * @return the corresponding account DTO.
     */
    fun entityToDto(accountEntity: AccountEntity): AccountDto

    /**
     * Converts an [AccountDto] to an [AccountEntity].
     *
     * @param accountDto the account DTO to be converted.
     * @return the corresponding account entity.
     */
    fun dtoToEntity(accountDto: AccountDto): AccountEntity

    /**
     * Converts a [CreateAccountRequestDto] to an [AccountEntity].
     *
     * <p>
     * This method transforms a request DTO received from an API into an account entity
     * suitable for persistence in the database. The mapping handles the conversion of
     * all relevant fields from the request to the entity.
     * </p>
     *
     * @param createAccountRequestDto the account request DTO containing account details.
     * @return the account entity populated with data from the request DTO.
     */
    fun requestToEntity(createAccountRequestDto: CreateAccountRequestDto): AccountEntity
}
