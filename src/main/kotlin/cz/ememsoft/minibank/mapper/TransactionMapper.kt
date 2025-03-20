package cz.ememsoft.minibank.mapper

import cz.ememsoft.minibank.entity.TransactionEntity
import cz.ememsoft.minibank.transaction.request.TransferRequestDto
import cz.ememsoft.minibank.transaction.response.TransferResponseDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

/**
 * Mapper interface for converting between Transaction entities and Data Transfer Objects (DTOs).
 *
 * <p>
 * This interface leverages MapStruct to automatically generate the mapping implementations
 * for converting between the following types:
 * <ul>
 *   <li>[TransactionEntity] and [TransferResponseDto]</li>
 *   <li>[TransferRequestDto] and [TransactionEntity]</li>
 * </ul>
 * </p>
 *
 * <p>
 * The component model is configured for Spring, enabling dependency injection of the generated mapper.
 * </p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface TransactionMapper {

    /**
     * Converts a [TransactionEntity] to a [TransferResponseDto].
     *
     * @param transactionEntity The transaction entity to be converted.
     * @return The corresponding transfer response DTO.
     */
    fun entityToResponseDto(transactionEntity: TransactionEntity): TransferResponseDto

    /**
     * Converts a [TransferRequestDto] to a partial [TransactionEntity].
     *
     * This mapping ignores the ID and timestamp fields, which will be set during the transaction processing.
     *
     * @param transferRequestDto The transfer request DTO to be converted.
     * @return A partially populated transaction entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "status", ignore = true)
    fun requestToEntity(transferRequestDto: TransferRequestDto): TransactionEntity
}