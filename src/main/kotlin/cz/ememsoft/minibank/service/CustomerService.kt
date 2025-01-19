package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.dto.CustomerDto

interface CustomerService {
    fun getCustomer(id: Long): CustomerDto
    fun saveCustomer(customerDto: CustomerDto): Long
    fun updateCustomer()
    fun deleteCustomer()
    fun getCustomerAccounts()
    fun getCustomerTransactions()
}
