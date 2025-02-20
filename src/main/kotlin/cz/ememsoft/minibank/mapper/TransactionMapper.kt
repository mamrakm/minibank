package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.transaction.entity.TransactionEntity
import cz.ememsoft.minibank.transaction.response.CreateTransactionResponseDto
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ReportingPolicy

/**
 * Mapper interface for converting transaction entities to response DTOs.
 *
 * Utilizes MapStruct to generate efficient, type-safe mapping implementations.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
fun interface TransactionMapper {

    /**
     * Converts a [TransactionEntity] to a [CreateTransactionResponseDto].
     *
     * @param entity The transaction entity to convert.
     * @return The corresponding transaction response DTO.
     */
    fun entityToResponseDto(entity: TransactionEntity): CreateTransactionResponseDto
}
