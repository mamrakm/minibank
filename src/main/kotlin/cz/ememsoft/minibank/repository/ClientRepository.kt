package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.ClientEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ClientRepository : JpaRepository<ClientEntity, Long>  {

    // Custom query method to find a customer by email
    fun findByEmail(email: String): ClientEntity?

    // Custom query method to find a customer by phone number
    fun findByPhone(phone: String): ClientEntity?
}
