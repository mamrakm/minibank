package cz.ememsoft.minibank.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

/**
 * Entity representing a client in the system.
 *
 * This entity maps to the "client" table in the "bank" schema.
 *
 * @property id The unique identifier for the client.
 * @property firstName The first name of the client. This field is required.
 * @property lastName The last name of the client. This field is required.
 * @property email The email address of the client. This field is required and must be unique.
 * @property phone The phone number of the client. This field is required and must match a valid phone number format.
 * @property address The address of the client. This field is required.
 */
@Entity
@Table(name = "client", schema = "bank")
class ClientEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    val id: Long,

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
    @OneToMany(mappedBy = "clientEntity", cascade = [CascadeType.DETACH], orphanRemoval = true)
    val accountEntities: Set<AccountEntity> = emptySet()
) {

    /**
     * Checks equality between two `ClientEntity` objects based on their properties.
     *
     * @param other The object to compare with.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClientEntity) return false

        if (id != other.id) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (email != other.email) return false
        if (phone != other.phone) return false
        if (address != other.address) return false

        return true
    }

    /**
     * Generates a hash code for the `ClientEntity` object.
     *
     * @return The hash code based on the object's properties.
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + address.hashCode()
        return result
    }

    /**
     * Returns a string representation of the `ClientEntity` object.
     *
     * @return A string containing the values of the entity's properties.
     */
    override fun toString(): String {
        return "ClientEntity(id=$id, firstName='$firstName', lastName='$lastName', email='$email', phone='$phone', address='$address')"
    }


}
