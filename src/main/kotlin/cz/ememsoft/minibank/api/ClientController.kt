package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.dto.request.ClientSaveRequest
import cz.ememsoft.minibank.api.dto.response.ClientSaveResponse
import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.mapper.ClientMapper
import cz.ememsoft.minibank.service.ClientService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

/**
 * REST controller for managing client-related operations.
 *
 * This controller provides endpoints for creating, updating, deleting, and retrieving client
 * information, as well as their associated accounts and transactions.
 *
 * @property clientService The service layer responsible for business logic.
 * @property clientMapper The mapper for converting between request/response objects and DTOs.
 */
@RestController
@RequestMapping("/clients")
class ClientController(
    val clientService: ClientService,
    val clientMapper: ClientMapper
) {

    /**
     * Retrieves all clients stored in DB.
     *
     * **Note:** This method currently has no implementation.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getClients(): List<ClientDto> {
        return clientService.getAllclients()
    }

    /**
     * Creates a new client.
     *
     * @param clientRequest The request body containing client information to be saved.
     * @return A response containing the ID of the newly created client.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/save", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun saveclient(@RequestBody clientRequest: ClientSaveRequest): ClientSaveResponse {
        val clientDto = clientMapper.toDto(clientRequest)
        val id = clientService.saveclient(clientDto)
        return ClientSaveResponse(id)
    }

    /**
     * Updates an existing client's information.
     *
     * **Note:** This method currently has no implementation.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PatchMapping("/{id}")
    fun updateClient() {
        // Update client
    }

    /**
     * Deletes a client by their ID.
     *
     * **Note:** This method currently has no implementation.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}, produces = [MediaType.APPLICATION_JSON_VALUE]")
    fun deleteClient(@PathVariable id: Long) {
        clientService.deleteclient(id)
    }

    /**
     * Retrieves accounts associated with a client.
     *
     * **Note:** This method currently has no implementation.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}/accounts")
    fun getClientAccount() {
        // Get client accounts
    }

    /**
     * Retrieves transactions associated with a client.
     *
     * **Note:** This method currently has no implementation.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @GetMapping("/{id}/transactions")
    fun getClientTransactions() {
        // Get client transactions
    }
}
