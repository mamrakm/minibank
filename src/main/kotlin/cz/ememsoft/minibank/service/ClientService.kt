package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.dto.CustomerDto

interface ClientService {
    fun getCustomer(id: Long): CustomerDto
    fun saveCustomer(customerDto: CustomerDto): Long
    fun updateCustomer()
    fun deleteCustomer()
    fun getCustomerAccounts()
    fun getCustomerTransactions()
}
