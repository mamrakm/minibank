package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.CustomerEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CustomerRepository : JpaRepository<CustomerEntity, Long>  {

    // Custom query method to find a customer by email
    fun findByEmail(email: String): CustomerEntity?

    // Custom query method to find a customer by phone number
    fun findByPhone(phone: String): CustomerEntity?
}
