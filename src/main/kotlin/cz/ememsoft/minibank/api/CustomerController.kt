package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.dto.request.CustomerSaveRequest
import cz.ememsoft.minibank.mapper.CustomerMapper
import cz.ememsoft.minibank.service.CustomerService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/save", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun saveCustomer(@RequestBody customerRequest: CustomerSaveRequest) {
        // Save customer
        val customerDto = customerMapper.toDto(customerRequest)
        customerService.saveCustomer(customerDto)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PatchMapping("/{id}")
    fun updateCustomer() {
        // Update customer
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}, produces = [MediaType.APPLICATION_JSON_VALUE]")
    fun deleteCustomer() {
        // Delete customer
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}/accounts")
    fun getCustomerAccounts() {
        // Get customer accounts
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @GetMapping("/{id}/transactions")
    fun getCustomerTransactions() {
        // Get customer transactions
    }
}
