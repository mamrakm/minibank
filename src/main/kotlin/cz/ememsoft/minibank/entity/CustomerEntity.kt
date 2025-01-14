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
    val id: Long = 0,

    @NotNull
    @Column(name = "first_name", nullable = false)
    val firstName: String,

    @NotNull
    @Column(name = "surname", nullable = false)
    val surname: String,

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
) {}
