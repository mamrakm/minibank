package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.api.transaction.request.TransactionRequestDto
import cz.ememsoft.minibank.api.transaction.response.TransactionResponseDto
import cz.ememsoft.minibank.entity.TransactionEntity
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface TransactionMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "sourceAccountId", target = "sourceAccountId")
    @Mapping(source = "targetAccountId", target = "targetAccountId")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "currency", target = "currency") // Maps enum to string
    @Mapping(source = "timestamp", target = "timestamp")
    @Mapping(source = "status", target = "status") // Maps enum to enum
    @Mapping(source = "reference", target = "reference")
    fun entityToResponseDto(transactionEntity: TransactionEntity): TransactionResponseDto

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "currencyOrdinal", expression = "java(mapCurrencyToOrdinal(transactionRequestDto.getCurrency()))")
    @Mapping(target = "statusOrdinal", expression = "java(cz.ememsoft.minibank.enumeration.TransactionStatusEnum.PENDING.ordinal())")
    @Mapping(source = "sourceAccountId", target = "sourceAccountId")
    @Mapping(source = "targetAccountId", target = "targetAccountId")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "reference", target = "reference")
    fun requestToEntity(transactionRequestDto: TransactionRequestDto): TransactionEntity

    fun mapCurrencyToOrdinal(currency: String): Int {
        return CurrencyEnum.valueOf(currency.uppercase().trim()).ordinal
    }
}