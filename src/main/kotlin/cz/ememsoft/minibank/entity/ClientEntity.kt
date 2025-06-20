package cz.ememsoft.minibank.entity

import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import cz.ememsoft.minibank.enumeration.UserRoleEnum
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Client entity representing a bank customer's profile.
 *
 * This entity is decoupled from authentication. It stores profile information
 * linked to a Keycloak user via the `keycloakUserId` field, which should
 * match the 'sub' (subject) claim of the user's JWT.
 */
@Table("client", schema = "bank")
data class ClientEntity(
    @Id
    @Column("id")
    val id: Long,

    /**
     * The unique identifier from Keycloak's JWT 'sub' claim. This links the
     * application profile to the identity provider's user record.
     */
    @Column("keycloak_user_id")
    @NotBlank(message = "Keycloak user ID must not be blank")
    val keycloakUserId: String,

    @Column("first_name")
    @NotBlank(message = "First name must not be blank")
    val firstName: String,

    @Column("last_name")
    @NotBlank(message = "Last name must not be blank")
    val lastName: String,

    @Column("email")
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "Email format is invalid"
    )
    val email: String,

    /**
     * User role determining access level, managed within this application.
     */
    @Column("role")
    val role: UserRoleEnum = UserRoleEnum.CLIENT,

    @Column("phone_number")
    val phoneNumber: String?,

    @Column("address")
    val address: String?,

    @Column("date_of_birth")
    @Past(message = "Date of birth must be in the past")
    val dateOfBirth: LocalDate?,

    @Column("status")
    val status: ClientStatusEnum = ClientStatusEnum.ACTIVE,

    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
)