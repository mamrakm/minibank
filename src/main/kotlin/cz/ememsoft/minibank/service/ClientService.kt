package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.dto.ClientDto

interface ClientService {
    fun deleteCustomer(id: Long)
    fun getCustomer(id: Long): ClientDto
    fun getCustomerAccounts(): List<ClientDto>
    fun getCustomerTransactions()
    fun saveCustomer(clientDto: ClientDto): Long
    fun updateCustomer()
}
