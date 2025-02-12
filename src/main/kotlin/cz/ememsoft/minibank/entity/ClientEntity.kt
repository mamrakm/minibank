package cz.ememsoft.minibank.entity

import jakarta.persistence.Id
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * Represents a client entity within the system.
 *
 * This entity is mapped to the `client` table in the `bank` schema and is used to store client information.
 *
 * <p><strong>Database Mapping:</strong></p>
 * <ul>
 *   <li>Table: `bank.client`</li>
 *   <li>Primary Key: `id`</li>
 * </ul>
 *
 * <p><strong>Fields:</strong></p>
 * <ul>
 *   <li>{@code id} - The unique identifier for the client.</li>
 *   <li>{@code firstName} - The first name of the client. This field is required.</li>
 *   <li>{@code lastName} - The last name of the client. This field is required.</li>
 *   <li>{@code email} - The email address of the client. This field is required and must be a valid email format.</li>
 *   <li>{@code phone} - The phone number of the client. This field is required and must match a valid phone number pattern.</li>
 *   <li>{@code address} - The address of the client. This field is required.</li>
 * </ul>
 *
 * @see jakarta.validation.constraints.Email
 * @see jakarta.validation.constraints.Pattern
 */
@Table(name = "client", schema = "bank")
data class ClientEntity(
    /** Unique identifier for the client */
    @Id
    @Column("id")
    val id: Long,

    /** The first name of the client. This field is required. */
    @Column("first_name")
    @NotNull
    val firstName: String,

    /** The last name of the client. This field is required. */
    @Column("last_name")
    @NotNull
    val lastName: String,

    /** The email address of the client. This field is required and must be unique. */
    @Column("email")
    @Email(message = "Invalid email")
    val email: String,

    /**
     * The phone number of the client.
     * This field is required and must conform to the E.164 phone number format.
     */
    @Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}\$",
        message = "Invalid phone number"
    )
    @Column("phone")
    val phone: String,

    /** The address of the client. This field is required. */
    @Column("address")
    val address: String,
)
