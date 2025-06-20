package cz.ememsoft.minibank.dto.auth

import cz.ememsoft.minibank.enumeration.UserRoleEnum
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

/**
 * DTO for client registration requests.
 *
 * Contains all required information for creating a new client account
 * including authentication credentials.
 */
data class RegisterRequestDto(
    @field:NotBlank(message = "First name must not be blank")
    @field:Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    val firstName: String,

    @field:NotBlank(message = "Last name must not be blank")
    @field:Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    val lastName: String,

    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email must be valid")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "Email format is invalid"
    )
    val email: String,

    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]+$",
        message = "Password must contain at least one lowercase letter, one uppercase letter, one digit and one special character"
    )
    val password: String,

    @field:Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}\$",
        message = "Invalid phone number format"
    )
    val phoneNumber: String? = null,

    val address: String? = null,

    val dateOfBirth: LocalDate? = null,

    val role: UserRoleEnum = UserRoleEnum.CLIENT
)