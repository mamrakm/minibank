package cz.ememsoft.minibank.entity

import jakarta.persistence.Id
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table

/** R2DBC Entity
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
//@Entity(name = "client")
@Table(name = "client", schema = "bank")
data class ClientEntity(
    @Id
    @Column("id")
    val id: Long,

    @Column("first_name")
    @NotNull
    val firstName: String,

    @Column("last_name")
    @NotNull
    val lastName: String,

    @Column("email")
    @Email(message = "Invalid email")
    val email: String,

    @Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}\$",
        message = "Invalid phone number"
    )

    @Column("phone")
    val phone: String,

    @Column("address")
    val address: String,

    @Transient
    @MappedCollection(idColumn = "client_id")
    val accountEntities: MutableSet<AccountEntity> = mutableSetOf()
)