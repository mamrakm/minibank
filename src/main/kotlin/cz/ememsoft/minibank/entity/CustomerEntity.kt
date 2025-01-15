package cz.ememsoft.minibank.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

@Entity
@Table(name = "customer")
class CustomerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private val id: Long = 0,

    @NotNull
    @Column(name = "first_name", nullable = false)
    val firstName: String,

    @NotNull
    @Column(name = "last_name", nullable = false)
    val lastName: String,

    @Email(message = "Invalid email")
    @Column(name = "email", unique = true, nullable = false)
    val email: String,

    @Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}\$",
        message = "Invalid phone number"
    )
    @Column(name = "phone", nullable = false)
    val phone: String,

    @Column(name = "address", nullable = false)
    val address: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CustomerEntity) return false

        if (id != other.id) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (email != other.email) return false
        if (phone != other.phone) return false
        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + address.hashCode()
        return result
    }
}
