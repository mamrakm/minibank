package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.dto.request.ClientSaveRequest
import cz.ememsoft.minibank.api.dto.response.ClientSaveResponse
import cz.ememsoft.minibank.mapper.ClientMapper
import cz.ememsoft.minibank.service.ClientService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

/**
 * REST controller for managing customer-related operations.
 *
 * This controller provides endpoints for creating, updating, deleting, and retrieving customer
 * information, as well as their associated accounts and transactions.
 *
 * @property clientServiceImpl The service layer responsible for business logic.
 * @property clientMapper The mapper for converting between request/response objects and DTOs.
 */
@RestController
@RequestMapping("/customers")
class ClientController(
    val clientServiceImpl: ClientService,
    val clientMapper: ClientMapper
) {

    /**
     * Retrieves customer details.
     *
     * **Note:** This method currently has no implementation.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("produces = [MediaType.APPLICATION_JSON_VALUE]")
    fun getCustomer() {
    }

    /**
     * Creates a new customer.
     *
     * @param customerRequest The request body containing customer information to be saved.
     * @return A response containing the ID of the newly created customer.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/save", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun saveCustomer(@RequestBody customerRequest: ClientSaveRequest): ClientSaveResponse {
        val customerDto = clientMapper.toDto(customerRequest)
        val id = clientServiceImpl.saveCustomer(customerDto)
        return ClientSaveResponse(id)
    }

    /**
     * Updates an existing customer's information.
     *
     * **Note:** This method currently has no implementation.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PatchMapping("/{id}")
    fun updateCustomer() {
        // Update customer
    }

    /**
     * Deletes a customer by their ID.
     *
     * **Note:** This method currently has no implementation.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}, produces = [MediaType.APPLICATION_JSON_VALUE]")
    fun deleteCustomer() {
        // Delete customer
    }

    /**
     * Retrieves accounts associated with a customer.
     *
     * **Note:** This method currently has no implementation.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}/accounts")
    fun getCustomerAccounts() {
        // Get customer accounts
    }

    /**
     * Retrieves transactions associated with a customer.
     *
     * **Note:** This method currently has no implementation.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @GetMapping("/{id}/transactions")
    fun getCustomerTransactions() {
        // Get customer transactions
    }
}
