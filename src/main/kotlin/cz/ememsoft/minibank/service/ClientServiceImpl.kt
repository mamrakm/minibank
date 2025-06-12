package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.client.request.CreateClientRequestDto
import cz.ememsoft.minibank.api.client.response.CreateClientResponseDto
import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.entity.ClientEntity
import cz.ememsoft.minibank.exception.ClientNotFoundException
import cz.ememsoft.minibank.exception.DuplicateClientException
import cz.ememsoft.minibank.mapper.ClientMapper
import cz.ememsoft.minibank.repository.ClientRepository
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
        return clientRepository.findById(id)
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
        logger.info { "Fetching all clients" }
        return clientRepository.findAll()
            .map { clientMapper.entityToDto(it) }
            .doOnComplete { logger.debug { "Finished fetching all clients" } }
            .doOnError { logger.error(it) { "Error fetching all clients" } }
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
                    personalNumber = existingClient.personalNumber
                )
                logger.debug { "Updating client entity: $updatedEntity" }
                clientRepository.save(updatedEntity)
            }
            .map { clientMapper.entityToDto(it) }
            .doOnSuccess { logger.info { "Client with ID $id updated successfully" } }
            .doOnError { logger.error(it) { "Error updating client with ID: $id" } }
    }

    override fun deleteClient(id: Long): Mono<Void> {
        logger.info { "Deleting client with ID: $id" }
        return clientRepository.findById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .flatMap { client ->
                client.id?.let { clientId ->
                    clientRepository.deleteById(clientId)
                } ?: Mono.empty()
            }
            .doOnSuccess { logger.info { "Client with ID $id deleted successfully" } }
            .doOnError { logger.error(it) { "Error deleting client with ID: $id" } }
    }

    override fun findClientByEmail(email: String): Mono<ClientDto> {
        logger.info { "Searching for client with email: $email" }
        return clientRepository.findByEmail(email)
            .map { clientMapper.entityToDto(it) }
            .doOnSuccess { it?.let { logger.info { "Client with email $email found" } } }
            .switchIfEmpty(Mono.defer {
                logger.warn { "Client with email $email not found" }
                Mono.empty()
            })
            .doOnError { logger.error(it) { "Error searching for client with email: $email" } }
    }

    override fun findClientsByFirstName(firstName: String): Flux<ClientDto> {
        logger.info { "Searching for clients with first name: $firstName" }
        return clientRepository.findByFirstNameIgnoreCase(firstName)
            .map { clientMapper.entityToDto(it) }
            .doOnComplete { logger.debug { "Completed searching for clients with first name: $firstName" } }
            .doOnError { logger.error(it) { "Error searching for clients with first name: $firstName" } }
    }
}