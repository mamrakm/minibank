package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.exception.CustomerNotFoundException
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
     * Retrieves a customer by their ID.
     *
     * @param id The ID of the customer to retrieve.
     * @return The corresponding [ClientDto] for the given ID.
     * @throws CustomerNotFoundException If no customer is found with the given ID.
     */
    override fun getCustomer(id: Long): ClientDto {
        val foundCustomer = clientRepository.findById(id)
        if (foundCustomer.isPresent) {
            val customerEntity = foundCustomer.get()
            val customerDto = clientMapper.toDtoFromEntity(customerEntity)
            return customerDto
        } else {
            throw CustomerNotFoundException()
        }
    }

    /**
     * Saves a new customer to the database.
     *
     * @param clientDto The DTO containing customer information to save.
     * @return The ID of the saved customer.
     */
    override fun saveCustomer(clientDto: ClientDto): Long {
        val customerEntity = clientMapper.toEntity(clientDto)
        val id = clientRepository.save(customerEntity).id
        return id
    }

    /**
     * Updates an existing customer's information.
     *
     * **Note:** This method currently has no implementation.
     */
    override fun updateCustomer() {
        // Update customer
    }

    /**
     * Deletes a customer by their ID.
     *
     * @param id The ID of the customer to delete.
     */
    override fun deleteCustomer(id: Long) {
        clientRepository.deleteById(id)
    }

    /**
     * Retrieves a list of all customers, mapped to [ClientDto] objects.
     *
     * @return A list of all customers as [ClientDto].
     */
    override fun getCustomerAccounts(): List<ClientDto> {
        return clientRepository.findAll().map { clientMapper.toDtoFromEntity(it) }
    }

    /**
     * Retrieves transactions associated with customers.
     *
     * **Note:** This method currently has no implementation.
     */
    override fun getCustomerTransactions() {
        // Get customer transactions
    }
}
