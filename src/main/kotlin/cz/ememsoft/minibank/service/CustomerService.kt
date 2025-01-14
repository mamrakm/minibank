package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.dto.request.CustomerSaveRequest
import cz.ememsoft.minibank.repository.CustomerRepository

class CustomerService(CustomerRepository: CustomerRepository) {
    fun getCustomer() {
        // Get customer
    }

    fun saveCustomer(customerRequest: CustomerSaveRequest) {
        customerRequest.
        customerRequest.save(customerRequest)
    }

    fun updateCustomer() {
        // Update customer
    }

    fun deleteCustomer() {
        // Delete customer
    }

    fun getCustomerAccounts() {
        // Get customer accounts
    }

    fun getCustomerTransactions() {
        // Get customer transactions
    }
}
