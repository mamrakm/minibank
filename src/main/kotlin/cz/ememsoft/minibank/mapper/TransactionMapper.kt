package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.api.transaction.request.TransactionRequestDto
import cz.ememsoft.minibank.api.transaction.response.TransactionResponseDto
import cz.ememsoft.minibank.entity.TransactionEntity
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import cz.ememsoft.minibank.enumeration.TransactionStatusEnum
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = [CurrencyEnum::class, TransactionStatusEnum::class]
)
interface TransactionMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "sourceAccountId", target = "sourceAccountId")
    @Mapping(source = "targetAccountId", target = "targetAccountId")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "currency", target = "currency")
    @Mapping(source = "timestamp", target = "timestamp")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "reference", target = "reference")
    fun entityToResponseDto(transactionEntity: TransactionEntity): TransactionResponseDto

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currencyOrdinal", expression = "java(CurrencyEnum.valueOf(transactionRequestDto.getCurrency().toUpperCase().trim()).ordinal())")
    @Mapping(target = "statusOrdinal", expression = "java(TransactionStatusEnum.PENDING.ordinal())")
    @Mapping(source = "sourceAccountId", target = "sourceAccountId")
    @Mapping(source = "targetAccountId", target = "targetAccountId")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "reference", target = "reference")
    fun requestToEntity(transactionRequestDto: TransactionRequestDto): TransactionEntity
}