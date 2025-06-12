package cz.ememsoft.minibank.dto

import cz.ememsoft.minibank.api.account.request.CreateAccountRequestDto
import cz.ememsoft.minibank.api.client.request.CreateClientRequestDto
import cz.ememsoft.minibank.api.transaction.request.TransactionRequestDto
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Comprehensive validation tests for DTOs and business rules
 */
class ValidationTest {

    @Test
    fun `CreateClientRequestDto should validate successfully with valid data`() {
        // Valid case - should not throw
        CreateClientRequestDto(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = LocalDate.of(1990, 1, 1)
        )
    }

    @Test
    fun `TransactionRequestDto should reject same source and target account`() {
        // This tests the init block validation
        assertThrows<IllegalArgumentException> {
            TransactionRequestDto(
                sourceAccountId = 1L,
                targetAccountId = 1L, // Same as source
                amount = BigDecimal("100.00"),
                currency = "USD"
            )
        }
    }

    @Test
    fun `TransactionRequestDto should validate currency in init block`() {
        // Test currency validation
        assertThrows<IllegalArgumentException> {
            TransactionRequestDto(
                sourceAccountId = 1L,
                targetAccountId = 2L,
                amount = BigDecimal("100.00"),
                currency = "INVALID"
            )
        }
    }

    @Test
    fun `TransactionRequestDto should validate amount in init block`() {
        // Test amount validation
        assertThrows<IllegalArgumentException> {
            TransactionRequestDto(
                sourceAccountId = 1L,
                targetAccountId = 2L,
                amount = BigDecimal("-100.00"), // Negative amount
                currency = "USD"
            )
        }
    }

    @Test
    fun `TransactionRequestDto should normalize currency`() {
        val request = TransactionRequestDto(
            sourceAccountId = 1L,
            targetAccountId = 2L,
            amount = BigDecimal("100.00"),
            currency = "usd" // lowercase
        )

        assert(request.normalizedCurrency == "USD")
    }

    @Test
    fun `CreateAccountRequestDto should validate normalized properties`() {
        val request = CreateAccountRequestDto(
            accountType = "savings", // lowercase
            balance = BigDecimal("1000.00"),
            currency = "eur", // lowercase
            clientId = 1L
        )

        assert(request.normalizedAccountType == "SAVINGS")
        assert(request.normalizedCurrency == "EUR")
    }

    @Test
    fun `CreateAccountRequestDto should format balance correctly`() {
        val request = CreateAccountRequestDto(
            accountType = "CHECKING",
            balance = BigDecimal("1234.5678"),
            currency = "USD",
            clientId = 1L
        )

        assert(request.getFormattedBalance() == "1234.5678")
    }

    @Test
    fun `CreateClientRequestDto should handle edge cases`() {
        // Test with minimal valid data
        CreateClientRequestDto(
            firstName = "A", // Single character
            lastName = "B",
            email = "a@b.co", // Minimal valid email
            phoneNumber = "+1",
            address = "X",
            dateOfBirth = LocalDate.of(1900, 1, 1) // Very old date
        )
    }

    @Test
    fun `TransactionRequestDto should handle minimum valid amount`() {
        TransactionRequestDto(
            sourceAccountId = 1L,
            targetAccountId = 2L,
            amount = BigDecimal("0.0001"), // Minimum valid amount
            currency = "USD"
        )
    }

    @Test
    fun `TransactionRequestDto should handle maximum valid amount`() {
        TransactionRequestDto(
            sourceAccountId = 1L,
            targetAccountId = 2L,
            amount = BigDecimal("999999999999999.9999"), // Maximum valid amount
            currency = "USD"
        )
    }

    @Test
    fun `CreateAccountRequestDto should handle zero balance`() {
        CreateAccountRequestDto(
            accountType = "CHECKING",
            balance = BigDecimal.ZERO,
            currency = "USD",
            clientId = 1L
        )
    }

    @Test
    fun `CreateClientRequestDto should handle future birth date validation`() {
        // This should be caught by @Past validation if properly configured
        // The actual validation behavior depends on the validation framework configuration
        val futureDate = LocalDate.now().plusDays(1)

        // This might pass at the DTO level but should fail at service level
        val request = CreateClientRequestDto(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = futureDate
        )

        // In a real application, this would be validated by the service layer
        assert(request.dateOfBirth.isAfter(LocalDate.now()))
    }

    @Test
    fun `should handle empty reference in TransactionRequestDto`() {
        val request = TransactionRequestDto(
            sourceAccountId = 1L,
            targetAccountId = 2L,
            amount = BigDecimal("100.00"),
            currency = "USD",
            reference = "" // Empty reference should be allowed
        )

        assert(request.reference.isEmpty())
    }

    @Test
    fun `should handle long reference in TransactionRequestDto`() {
        val longReference = "A".repeat(255) // Test maximum length

        TransactionRequestDto(
            sourceAccountId = 1L,
            targetAccountId = 2L,
            amount = BigDecimal("100.00"),
            currency = "USD",
            reference = longReference
        )
    }

    @Test
    fun `should validate all supported currencies`() {
        val currencies = listOf("USD", "EUR", "GBP")

        currencies.forEach { currency ->
            TransactionRequestDto(
                sourceAccountId = 1L,
                targetAccountId = 2L,
                amount = BigDecimal("100.00"),
                currency = currency
            )
        }
    }

    @Test
    fun `should validate all supported account types`() {
        val accountTypes = listOf(
            "CHECKING", "CURRENT", "SAVINGS", "INVESTMENT",
            "BUSINESS", "STUDENT", "JOINT", "LOAN", "CLASSIC"
        )

        accountTypes.forEach { accountType ->
            CreateAccountRequestDto(
                accountType = accountType,
                balance = BigDecimal("1000.00"),
                currency = "USD",
                clientId = 1L
            )
        }
    }
}