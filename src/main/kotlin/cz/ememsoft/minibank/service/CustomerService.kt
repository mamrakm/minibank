package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.dto.CustomerDto
import cz.ememsoft.minibank.exception.CustomerNotFoundException
import cz.ememsoft.minibank.mapper.CustomerMapper
import cz.ememsoft.minibank.repository.CustomerRepository
import org.springframework.stereotype.Service

@Service
class CustomerService(val customerRepository: CustomerRepository, val customerMapper: CustomerMapper) {
    fun getCustomer(id: Long): CustomerDto {
        // Get customer
        val foundCustomer = customerRepository.findById(id)
        if (foundCustomer.isPresent) {
            val customerEntity = foundCustomer.get()
            val customerDto = customerMapper.toDtoFromEntity(customerEntity)
            return customerDto
        } else {
            throw CustomerNotFoundException()
        }
    }

    fun saveCustomer(customerDto: CustomerDto):Long {
        // Map DTO to Entity
        val customerEntity = customerMapper.toEntity(customerDto)
        val id = customerRepository.save(customerEntity).id
        return id
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
