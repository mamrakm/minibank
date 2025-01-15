package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.dto.CustomerDto
import cz.ememsoft.minibank.mapper.CustomerMapper
import cz.ememsoft.minibank.repository.CustomerRepository
import org.springframework.stereotype.Service

@Service
class CustomerService(val customerRepository: CustomerRepository, val customerMapper: CustomerMapper) {
    fun getCustomer() {
        // Get customer
    }

    fun saveCustomer(customerDto: CustomerDto) {
        // Map DTO to Entity
        val customerEntity = customerMapper.toEntity(customerDto)
        customerRepository.save(customerEntity)
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
