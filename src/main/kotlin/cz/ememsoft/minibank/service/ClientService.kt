package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.dto.ClientDto

/**
 * Service interface for managing clients in the application.
 */
interface ClientService {

    /**
     * Deletes a customer by their ID.
     *
     * @param id The ID of the customer to delete.
     */
    fun deleteCustomer(id: Long)

    /**
     * Retrieves a customer by their ID.
     *
     * @param id The ID of the customer to retrieve.
     * @return The corresponding [ClientDto] for the given ID.
     */
    fun getCustomer(id: Long): ClientDto

    /**
     * Retrieves a list of all customers, mapped to [ClientDto] objects.
     *
     * @return A list of all customers as [ClientDto].
     */
    fun getCustomerAccounts(): List<ClientDto>

    /**
     * Retrieves transactions associated with customers.
     *
     * **Note:** This method is intended to fetch customer transactions but has no detailed implementation here.
     */
    fun getCustomerTransactions()

    /**
     * Saves a new customer to the database.
     *
     * @param clientDto The DTO containing customer information to save.
     * @return The ID of the saved customer.
     */
    fun saveCustomer(clientDto: ClientDto): Long

    /**
     * Updates an existing customer's information.
     *
     * **Note:** This method is intended for updating customer details but has no detailed implementation here.
     */
    fun updateCustomer()
}
