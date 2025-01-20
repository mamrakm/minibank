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

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class ClientServiceImpl(
    private val clientRepository: ClientRepository,
    private val clientMapper: ClientMapper
) : ClientService {

    /**
     * Retrieves a client by their ID.
     *
     * @param id The ID of the client to retrieve.
     * @return The corresponding [ClientDto] for the given ID.
     * @throws ClientNotFoundException If no client is found with the given ID.
     */
    override fun getClient(id: Long): ClientDto {
        logger.info { "Fetching client with ID: $id" }
        val clientEntity = clientRepository.findById(id).orElseThrow {
            logger.warn { "Client with ID $id not found" }
            ClientNotFoundException("Client with ID $id not found")
        }
        return clientMapper.toDtoFromEntity(clientEntity)
    }

    /**
     * Retrieves all clients in the system.
     *
     * @return A list of all clients as [ClientDto].
     */
    override fun getAllClients(): List<ClientDto> {
        logger.info { "Fetching all clients" }
        return clientRepository.findAll().map { clientMapper.toDtoFromEntity(it) }
    }

    /**
     * Saves a new client to the database.
     *
     * @param clientDto The DTO containing client information to save.
     * @return The ID of the saved client.
     * @throws DuplicateClientException If a client with the same email already exists.
     */
    override fun saveClient(clientDto: ClientDto): Long {
        logger.info { "Saving new client: $clientDto" }
        if (clientRepository.findByEmail(clientDto.email) != null) {
            throw DuplicateClientException("Client with email ${clientDto.email} already exists")
        }
        val clientEntity = clientMapper.toEntity(clientDto)
        return clientRepository.save(clientEntity).id.also {
            logger.info { "Client saved with ID: $it" }
        }
    }

    /**
     * Updates an existing client's information.
     *
     * @param id The ID of the client to update.
     * @param updatedClientDto The updated client data.
     * @return The updated [ClientDto].
     * @throws ClientNotFoundException If no client is found with the given ID.
     */
    override fun updateClient(id: Long, updatedClientDto: ClientDto): ClientDto {
        logger.info { "Updating client with ID: $id" }

        val existingClient = clientRepository.findById(id).orElseThrow {
            logger.warn { "Client with ID $id not found for update" }
            ClientNotFoundException("Client with ID $id not found")
        }

        // Create a new instance with updated fields
        val updatedEntity = ClientEntity(
            id = existingClient.id,
            firstName = updatedClientDto.firstName,
            lastName = updatedClientDto.lastName,
            email = updatedClientDto.email,
            phone = updatedClientDto.phone,
            address = updatedClientDto.address
        )

        // Save updated entity and return the updated DTO
        return clientMapper.toDtoFromEntity(clientRepository.save(updatedEntity)).also {
            logger.info { "Client with ID $id updated successfully" }
        }
    }
    
    /**
     * Deletes a client by their ID.
     *
     * @param id The ID of the client to delete.
     * @throws ClientNotFoundException If no client is found with the given ID.
     */
    override fun deleteClient(id: Long) {
        logger.info { "Deleting client with ID: $id" }
        if (!clientRepository.existsById(id)) {
            logger.warn { "Client with ID $id not found for deletion" }
            throw ClientNotFoundException("Client with ID $id not found")
        }
        clientRepository.deleteById(id)
        logger.info { "Client with ID $id deleted successfully" }
    }

    /**
     * Searches for a client by their email.
     *
     * @param email The email address to search for.
     * @return The corresponding [ClientDto] if found, or null otherwise.
     */
    override fun findClientByEmail(email: String): ClientDto? {
        logger.info { "Searching for client with email: $email" }
        return clientRepository.findByEmail(email)?.let {
            clientMapper.toDtoFromEntity(it)
        }.also {
            if (it == null) logger.warn { "Client with email $email not found" }
        }
    }

    /**
     * Searches for clients by their first name.
     *
     * @param firstName The first name of the client(s) to search for.
     * @return A list of clients matching the first name as [ClientDto].
     */
    override fun findClientsByFirstName(firstName: String): List<ClientDto> {
        logger.info { "Searching for clients with first name: $firstName" }
        return clientRepository.findAll()
            .filter { it.firstName.equals(firstName, ignoreCase = true) }
            .map { clientMapper.toDtoFromEntity(it) }
    }
}
