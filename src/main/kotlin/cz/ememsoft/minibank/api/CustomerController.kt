package cz.ememsoft.minibank.api

import CustomerMapper
import cz.ememsoft.minibank.api.dto.request.CustomerSaveRequest
import cz.ememsoft.minibank.service.CustomerService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/customers")
class CustomerController(val customerService: CustomerService, val customerMapper: CustomerMapper) {
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("produces = [MediaType.APPLICATION_JSON_VALUE]")
    fun getCustomer() {
        // Get customer
    }

    @PostMapping("/save, produces = [MediaType.APPLICATION_JSON_VALUE]")
    @ResponseStatus(HttpStatus.CREATED)
    fun saveCustomer(customerRequest: CustomerSaveRequest) {

        // Save customer
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    fun updateCustomer() {
        // Update customer
    }

    @DeleteMapping("/{id}, produces = [MediaType.APPLICATION_JSON_VALUE]")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCustomer() {
        // Delete customer
    }

    @GetMapping("/{id}/accounts")
    @ResponseStatus(HttpStatus.OK)
    fun getCustomerAccounts() {
        // Get customer accounts
    }

    @GetMapping("/{id}/transactions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun getCustomerTransactions() {
        // Get customer transactions
    }
}
