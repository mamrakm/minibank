package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.client.request.CreateClientRequestDto
import cz.ememsoft.minibank.client.response.CreateClientResponseDto
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

    /**
     * Saves a new client to the database and returns the created client response.
     *
     * This method ensures that no duplicate email exists before saving the client.
     * If a client with the same email is found, an error is returned.
     * Otherwise, the client is saved, and the generated ID is used to create a response DTO.
     *
     * ## Process:
     * 1. Check if a client with the given email already exists.
     * 2. If a duplicate exists, return an error.
     * 3. If no duplicate exists, persist the new client and return a response DTO.
     *
     * @param clientDto The DTO containing client information to save.
     * @return A [Mono] emitting the saved [CreateClientResponseDto], or an error if a duplicate exists.
     */
    override fun createClient(clientDto: CreateClientRequestDto): Mono<CreateClientResponseDto> {
        logger.info { "Saving new client: $clientDto" }

        return clientRepository.findByEmail(clientDto.email)
            .flatMap {
                logger.warn { "Client already exists: ${clientDto.email}" }
                Mono.error<CreateClientResponseDto>(DuplicateClientException("Client with email ${clientDto.email} already exists"))
            }
            .switchIfEmpty(
                Mono.defer {
                    val clientEntity = clientMapper.requestToDto(clientDto)
                    logger.debug { "Persisting new client entity: $clientEntity" }

                    clientRepository.saveAndReturnId(
                        clientEntity.firstName,
                        clientEntity.lastName,
                        clientEntity.email,
                        clientEntity.phone,
                        clientEntity.address
                    ).map { clientMapper.entityToCreateResponse(it) }
                }
            )
            .doOnSuccess { logger.info { "Client saved with ID: $it" } }
            .doOnError { logger.error(it) { "Error saving client" } }
    }

    /**
     * Updates an existing client's information.
     *
     * @param updatedClientDto The updated client data.
     * @return A [Mono] emitting the updated [ClientDto], or an error if the client is not found.
     */
    override fun updateClient(updatedClientDto: ClientDto): Mono<ClientDto> {
        val id = updatedClientDto.id
        logger.info { "Updating client with ID: $id" }
        return clientRepository.findById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .flatMap { existingClient ->
                val updatedEntity = ClientEntity(
                    id = existingClient.id,
                    firstName = updatedClientDto.firstName,
                    lastName = updatedClientDto.lastName,
                    email = updatedClientDto.email,
                    phone = updatedClientDto.phone,
                    address = updatedClientDto.address
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
            .flatMap { clientRepository.deleteById(it.id).then(Mono.empty<Void>()) } // Ensure deletion completes
            .doOnSuccess { logger.info { "Client with ID $id deleted successfully" } }
            .doOnError { logger.error(it) { "Error deleting client with ID: $id" } }
    }

    /**
     * Searches for a client by their email.
     *
     * @param email The email address to search for.
     * @return A [Mono] emitting the corresponding [ClientDto], or an empty Mono if not found.
     */
    override fun findClientByEmail(email: String): Mono<ClientDto> {
        logger.info { "Searching for client with email: $email" }
        return clientRepository.findByEmail(email)
            .map { clientMapper.entityToDto(it) }
            .doOnSuccess { logger.info { "Client with email $email found" } }
            .switchIfEmpty(Mono.defer {
                logger.warn { "Client with email $email not found" }
                Mono.empty()
            })
            .doOnError { logger.error(it) { "Error searching for client with email: $email" } }
    }

    /**
     * Searches for clients by their first name.
     *
     * @param firstName The first name of the client(s) to search for.
     * @return A [Flux] emitting clients matching the first name as [ClientDto].
     */
    override fun findClientsByFirstName(firstName: String): Flux<ClientDto> {
        logger.info { "Searching for clients with first name: $firstName" }
        return clientRepository.findByFirstNameIgnoreCase(firstName)
            .map { clientMapper.entityToDto(it) }
            .doOnComplete { logger.debug { "Completed searching for clients with first name: $firstName" } }
            .doOnError { logger.error(it) { "Error searching for clients with first name: $firstName" } }
    }
}
