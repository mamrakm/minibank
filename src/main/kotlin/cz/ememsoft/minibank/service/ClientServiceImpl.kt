package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.dto.CustomerDto
import cz.ememsoft.minibank.exception.CustomerNotFoundException
import cz.ememsoft.minibank.mapper.ClientMapper
import cz.ememsoft.minibank.repository.ClientRepository
import org.springframework.stereotype.Service

@Service
class ClientServiceImpl(val clientRepository: ClientRepository, val clientMapper: ClientMapper) : ClientService {
    override fun getCustomer(id: Long): CustomerDto {
        // Get customer
        val foundCustomer = clientRepository.findById(id)
        if (foundCustomer.isPresent) {
            val customerEntity = foundCustomer.get()
            val customerDto = clientMapper.toDtoFromEntity(customerEntity)
            return customerDto
        } else {
            throw CustomerNotFoundException()
        }
    }

    override fun saveCustomer(customerDto: CustomerDto):Long {
        // Map DTO to Entity
        val customerEntity = clientMapper.toEntity(customerDto)
        val id = clientRepository.save(customerEntity).id
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
