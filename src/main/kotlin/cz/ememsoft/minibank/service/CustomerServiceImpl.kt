package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.dto.CustomerDto
import cz.ememsoft.minibank.exception.CustomerNotFoundException
import cz.ememsoft.minibank.mapper.CustomerMapper
import cz.ememsoft.minibank.repository.CustomerRepository
import org.springframework.stereotype.Service

@Service
class CustomerServiceImpl(val customerRepository: CustomerRepository, val customerMapper: CustomerMapper) : CustomerService {
    override fun getCustomer(id: Long): CustomerDto {
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

    override fun saveCustomer(customerDto: CustomerDto):Long {
        // Map DTO to Entity
        val customerEntity = customerMapper.toEntity(customerDto)
        val id = customerRepository.save(customerEntity).id
        return id
    }

    override fun updateCustomer() {
        // Update customer
    }

    override fun deleteCustomer() {
        // Delete customer
    }

    override fun getCustomerAccounts() {
        // Get customer accounts
    }

    override fun getCustomerTransactions() {
        // Get customer transactions
    }
}
