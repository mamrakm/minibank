package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.api.account.request.CreateAccountRequestDto
import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.enumeration.AccountStatusEnum
import cz.ememsoft.minibank.enumeration.AccountTypeEnum
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

/**
 * Mapper interface for converting between Account entities and Data Transfer Objects (DTOs).
 *
 * This interface leverages MapStruct to automatically generate the mapping implementations
 * for converting between AccountEntity and various DTOs with explicit mappings.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = [AccountStatusEnum::class, AccountTypeEnum::class, CurrencyEnum::class])
interface AccountMapper {

    /**
     * Converts an AccountEntity to an AccountDto with explicit field mappings.
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "clientId", target = "clientId")
    @Mapping(source = "balance", target = "balance")
    @Mapping(source = "accountType", target = "accountType")
    @Mapping(source = "currency", target = "currency")
    @Mapping(source = "status", target = "status")
    fun entityToDto(accountEntity: AccountEntity): AccountDto

    /**
     * Converts an AccountDto to an AccountEntity with explicit field mappings.
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "clientId", target = "clientId")
    @Mapping(source = "balance", target = "balance")
    @Mapping(target = "accountTypeOrdinal", expression = "java(accountDto.getAccountType().ordinal())")
    @Mapping(target = "currencyOrdinal", expression = "java(accountDto.getCurrency().ordinal())")
    @Mapping(target = "statusOrdinal", expression = "java(accountDto.getStatus().ordinal())")
    @Mapping(target = "withBalance", ignore = true)
    @Mapping(target = "creditBalance", ignore = true)
    @Mapping(target = "debitBalance", ignore = true)
    fun dtoToEntity(accountDto: AccountDto): AccountEntity

    /**
     * Converts a CreateAccountRequestDto to a partial AccountEntity.
     * The account name is generated based on account type and client ID.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", expression = "java(createAccountRequestDto.getAccountType().toUpperCase() + \" Account for Client \" + createAccountRequestDto.getClientId())")
    @Mapping(target = "accountTypeOrdinal", expression = "java(AccountTypeEnum.valueOf(createAccountRequestDto.getNormalizedAccountType()).ordinal())")
    @Mapping(target = "currencyOrdinal", expression = "java(CurrencyEnum.valueOf(createAccountRequestDto.getNormalizedCurrency()).ordinal())")
    @Mapping(target = "statusOrdinal", expression = "java(AccountStatusEnum.ACTIVE.ordinal())")
    @Mapping(target = "withBalance", ignore = true)
    @Mapping(target = "creditBalance", ignore = true)
    @Mapping(target = "debitBalance", ignore = true)
    fun createRequestToEntity(createAccountRequestDto: CreateAccountRequestDto): AccountEntity
}