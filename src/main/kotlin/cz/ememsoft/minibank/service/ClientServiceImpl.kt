package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.client.request.CreateClientRequestDto
import cz.ememsoft.minibank.api.client.response.CreateClientResponseDto
import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.entity.ClientEntity
import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import cz.ememsoft.minibank.exception.ClientNotFoundException
import cz.ememsoft.minibank.exception.DuplicateClientException
import cz.ememsoft.minibank.mapper.ClientMapper
import cz.ememsoft.minibank.repository.ClientRepository
import cz.ememsoft.minibank.repository.findByStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * Service implementation for managing clients in a reactive manner.
 *
 * This service provides CRUD operations and search functionality
 * for clients using a non-blocking, reactive approach with Project Reactor.
 *
 * @property clientRepository The repository responsible for client data operations.
 * @property clientMapper The mapper responsible for converting between entity and DTO representations.
 */
@Service
@Transactional
class ClientServiceImpl(
    private val clientRepository: ClientRepository,
    private val clientMapper: ClientMapper,
) : ClientService {

    /**
     * Retrieves a client by their ID.
     *
     * @param id The unique identifier of the client.
     * @return A [Mono] emitting the corresponding [ClientDto], or an error if the client is not found.
     */
    override fun getClient(id: Long): Mono<ClientDto> {
        logger.info { "Fetching client with ID: $id" }
        return clientRepository.findActiveById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .map { clientMapper.entityToDto(it) }
            .doOnSuccess { logger.debug { "Successfully retrieved client: $it" } }
            .doOnError { logger.error(it) { "Error retrieving client with ID: $id" } }
    }

    /**
     * Retrieves all clients in the system.
     *
     * @return A [Flux] emitting all clients as [ClientDto].
     */
    override fun getAllClients(): Flux<ClientDto> {
        logger.info { "Fetching all active clients" }
        return clientRepository.findAllActive()
            .map { clientMapper.entityToDto(it) }
            .doOnComplete { logger.debug { "Finished fetching all active clients" } }
            .doOnError { logger.error(it) { "Error fetching all active clients" } }
    }

    override fun createClient(clientDto: CreateClientRequestDto): Mono<CreateClientResponseDto> {
        logger.info { "Saving new client: $clientDto" }

        return clientRepository.findByEmail(clientDto.email)
            .flatMap {
                logger.warn { "Client already exists: ${clientDto.email}" }
                Mono.error<CreateClientResponseDto>(DuplicateClientException("Client with email ${clientDto.email} already exists"))
            }
            .switchIfEmpty(
                Mono.defer {
                    val clientEntity = clientMapper.createRequestToEntity(clientDto)
                    logger.debug { "Persisting new client entity: $clientEntity" }

                    clientRepository.saveAndReturn(
                        clientEntity.firstName,
                        clientEntity.lastName,
                        clientEntity.email,
                        clientEntity.phoneNumber,
                        clientEntity.address,
                        clientEntity.dateOfBirth
                    ).map { clientMapper.entityToCreateResponse(it) }
                }
            )
            .doOnSuccess { logger.info { "Client saved with ID: $it" } }
            .doOnError { logger.error(it) { "Error saving client" } }
    }

    override fun updateClient(id: Long, updatedClientDto: ClientDto): Mono<ClientDto> {
        logger.info { "Updating client with ID: $id" }
        return clientRepository.findById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .flatMap { existingClient ->
                val updatedEntity = ClientEntity(
                    id = existingClient.id,
                    firstName = updatedClientDto.firstName,
                    lastName = updatedClientDto.lastName,
                    email = updatedClientDto.email,
                    phoneNumber = updatedClientDto.phoneNumber,
                    address = updatedClientDto.address,
                    dateOfBirth = updatedClientDto.dateOfBirth,
                    personalNumber = existingClient.personalNumber,
                    status = existingClient.status
                )
                logger.debug { "Updating client entity: $updatedEntity" }
                clientRepository.save(updatedEntity)
            }
            .map { clientMapper.entityToDto(it) }
            .doOnSuccess { logger.info { "Client with ID $id updated successfully" } }
            .doOnError { logger.error(it) { "Error updating client with ID: $id" } }
    }

    override fun deleteClient(id: Long): Mono<Void> {
        logger.info { "Soft deleting client with ID: $id" }
        return clientRepository.findById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .flatMap { client ->
                val updatedClient = client.copy(status = ClientStatusEnum.INACTIVE)
                clientRepository.save(updatedClient)
            }
            .then()
            .doOnSuccess { logger.info { "Client with ID $id soft deleted successfully" } }
            .doOnError { logger.error(it) { "Error soft deleting client with ID: $id" } }
    }

    override fun findClientByEmail(email: String): Mono<ClientDto> {
        logger.info { "Searching for active client with email: $email" }
        return clientRepository.findActiveByEmail(email)
            .map { clientMapper.entityToDto(it) }
            .doOnSuccess { it?.let { logger.info { "Active client with email $email found" } } }
            .switchIfEmpty(Mono.defer {
                logger.warn { "Active client with email $email not found" }
                Mono.empty()
            })
            .doOnError { logger.error(it) { "Error searching for active client with email: $email" } }
    }

    override fun findClientsByFirstName(firstName: String): Flux<ClientDto> {
        logger.info { "Searching for active clients with first name: $firstName" }
        return clientRepository.findActiveByFirstNameIgnoreCase(firstName)
            .map { clientMapper.entityToDto(it) }
            .doOnComplete { logger.debug { "Completed searching for active clients with first name: $firstName" } }
            .doOnError { logger.error(it) { "Error searching for active clients with first name: $firstName" } }
    }

    // Administrative methods for managing clients in any status

    override fun getClientByIdAdmin(id: Long): Mono<ClientDto> {
        logger.info { "Admin: Fetching client with ID: $id (any status)" }
        return clientRepository.findById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .map { clientMapper.entityToDto(it) }
            .doOnSuccess { logger.debug { "Admin: Successfully retrieved client: $it" } }
            .doOnError { logger.error(it) { "Admin: Error retrieving client with ID: $id" } }
    }

    override fun getAllClientsAdmin(): Flux<ClientDto> {
        logger.info { "Admin: Fetching all clients (any status)" }
        return clientRepository.findAll()
            .map { clientMapper.entityToDto(it) }
            .doOnComplete { logger.debug { "Admin: Finished fetching all clients" } }
            .doOnError { logger.error(it) { "Admin: Error fetching all clients" } }
    }

    override fun getClientsByStatus(status: ClientStatusEnum): Flux<ClientDto> {
        logger.info { "Admin: Fetching clients with status: $status" }
        return clientRepository.findByStatus(status)
            .map { clientMapper.entityToDto(it) }
            .doOnComplete { logger.debug { "Admin: Finished fetching clients with status: $status" } }
            .doOnError { logger.error(it) { "Admin: Error fetching clients with status: $status" } }
    }

    override fun updateClientStatus(id: Long, newStatus: ClientStatusEnum): Mono<ClientDto> {
        logger.info { "Admin: Updating client status for ID: $id to $newStatus" }
        return clientRepository.findById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .flatMap { existingClient ->
                val updatedClient = existingClient.copy(status = newStatus)
                logger.debug { "Admin: Updating client status: $updatedClient" }
                clientRepository.save(updatedClient)
            }
            .map { clientMapper.entityToDto(it) }
            .doOnSuccess { logger.info { "Admin: Client status updated successfully for ID: $id to $newStatus" } }
            .doOnError { logger.error(it) { "Admin: Error updating client status for ID: $id" } }
    }
}