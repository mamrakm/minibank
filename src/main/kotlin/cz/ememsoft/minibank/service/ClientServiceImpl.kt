package cz.ememsoft.minibank.service

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

@Service
@Transactional
class ClientServiceImpl(
    private val clientRepository: ClientRepository,
    private val clientMapper: ClientMapper,
) : ClientService {

    /**
     * Retrieves a client by their ID in a reactive manner.
     *
     * @param id The ID of the client to retrieve.
     * @return A [Mono] emitting the corresponding [ClientDto], or an error if the client is not found.
     */
    override fun getClient(id: Long): Mono<ClientDto> {
        logger.info { "Fetching client with ID: $id" }
        return clientRepository.findById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .map { clientMapper.entityToDto(it) }
    }

    /**
     * Retrieves all clients in the system reactively.
     *
     * @return A [Flux] emitting all clients as [ClientDto].
     */
    override fun getAllClients(): Flux<ClientDto> {
        logger.info { "Fetching all clients" }
        return clientRepository.findAll()
            .map { clientMapper.entityToDto(it) }
    }

    /**
     * Saves a new client to the database reactively.
     *
     * @param clientDto The DTO containing client information to save.
     * @return A [Mono] emitting the ID of the saved client.
     */
    override fun saveClient(clientDto: ClientDto): Mono<Long> {
        logger.info { "Saving new client: $clientDto" }

        return clientRepository.findByEmail(clientDto.email)
            .flatMap {
                Mono.error<Long>(DuplicateClientException("Client with email ${clientDto.email} already exists"))
            }
            .switchIfEmpty(
                Mono.defer {
                    val clientEntity = clientMapper.dtoToEntity(clientDto)
                    clientRepository.save(clientEntity).map { it.id }
                }
            )
            .doOnSuccess { logger.info { "Client saved with ID: $it" } }
    }

    /**
     * Updates an existing client's information reactively.
     *
     * @param id The ID of the client to update.
     * @param updatedClientDto The updated client data.
     * @return A [Mono] emitting the updated [ClientDto], or an error if the client is not found.
     */
    override fun updateClient(id: Long, updatedClientDto: ClientDto): Mono<ClientDto> {
        logger.info { "Updating client with ID: $id" }

        return clientRepository.findById(id)
            .switchIfEmpty(Mono.error(ClientNotFoundException("Client with ID $id not found")))
            .flatMap { existingClient ->
                val updatedEntity = ClientEntity(  // Manually create a new instance
                    id = existingClient.id,  // Preserve the existing ID
                    firstName = updatedClientDto.firstName,
                    lastName = updatedClientDto.lastName,
                    email = updatedClientDto.email,
                    phone = updatedClientDto.phone,
                    address = updatedClientDto.address
                )
                clientRepository.save(updatedEntity)
            }
            .map { clientMapper.entityToDto(it) }
            .doOnSuccess { logger.info { "Client with ID $id updated successfully" } }
    }


    /**
     * Deletes a client by their ID reactively.
     *
     * @param id The ID of the client to delete.
     * @return A [Mono] signaling completion, or an error if the client is not found.
     */
    override fun deleteClient(id: Long): Mono<Void> {
        logger.info { "Deleting client with ID: $id" }
        return clientRepository.existsById(id)
            .flatMap {
                if (it) clientRepository.deleteById(id)
                else Mono.error(ClientNotFoundException("Client with ID $id not found"))
            }
            .doOnSuccess { logger.info { "Client with ID $id deleted successfully" } }
    }

    /**
     * Searches for a client by their email reactively.
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
    }

    /**
     * Searches for clients by their first name reactively.
     *
     * @param firstName The first name of the client(s) to search for.
     * @return A [Flux] emitting clients matching the first name as [ClientDto].
     */
    override fun findClientsByFirstName(firstName: String): Flux<ClientDto> {
        logger.info { "Searching for clients with first name: $firstName" }
        return clientRepository.findAll()
            .filter { it.firstName.equals(firstName, ignoreCase = true) }
            .map { clientMapper.entityToDto(it) }
    }
}
