package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.exception.ClientNotFoundException
import cz.ememsoft.minibank.mapper.ClientMapper
import cz.ememsoft.minibank.repository.ClientRepository
import org.springframework.stereotype.Service

/**
 * Implementation of the [ClientService] interface, providing operations for managing clients.
 *
 * @property clientRepository The repository for performing CRUD operations on client entities.
 * @property clientMapper The mapper for converting between client entities and DTOs.
 */
@Service
class ClientServiceImpl(
    val clientRepository: ClientRepository,
    val clientMapper: ClientMapper
) : ClientService {

    /**
     * Retrieves a client by their ID.
     *
     * @param id The ID of the client to retrieve.
     * @return The corresponding [ClientDto] for the given ID.
     * @throws clientNotFoundException If no client is found with the given ID.
     */
    override fun getclient(id: Long): ClientDto {
        val foundClient = clientRepository.findById(id)
        if (foundClient.isPresent) {
            val clientEntity = foundClient.get()
            val clientDto = clientMapper.toDtoFromEntity(clientEntity)
            return clientDto
        } else {
            throw ClientNotFoundException()
        }
    }

    /**
     * Retrieves accounts associated with clients.
     *
     * **Note:** This method currently has no implementation.
     */
    override fun getclientAccounts() {
        // Get client accounts
    }

    /**
     * Retrieves transactions associated with clients.
     *
     * **Note:** This method currently has no implementation.
     */
    override fun getclientTransactions() {
        // Get client transactions
    }

    /**
     * Retrieves a list of all clients.
     *
     * @return A list of all clients as [ClientDto].
     */
    override fun getAllclients(): List<ClientDto> {
        return clientRepository.findAll().map { clientMapper.toDtoFromEntity(it) }
    }

    /**
     * Saves a new client to the database.
     *
     * @param clientDto The DTO containing client information to save.
     * @return The ID of the saved client.
     */
    override fun saveclient(clientDto: ClientDto): Long {
        val clientEntity = clientMapper.toEntity(clientDto)
        val id = clientRepository.save(clientEntity).id
        return id
    }

    /**
     * Deletes a client by their ID.
     *
     * @param id The ID of the client to delete.
     */
    override fun deleteclient(id: Long) {
        clientRepository.deleteById(id)
    }

    /**
     * Updates an existing client's information.
     *
     * **Note:** This method currently has no implementation.
     */
    override fun updateclient() {
        // Update client
    }

}
