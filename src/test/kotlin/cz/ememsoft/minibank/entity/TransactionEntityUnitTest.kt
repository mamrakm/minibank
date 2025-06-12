package cz.ememsoft.minibank.entity

import cz.ememsoft.minibank.enumeration.CurrencyEnum
import cz.ememsoft.minibank.enumeration.TransactionStatusEnum
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime

class TransactionEntityUnitTest {

    @Test
    fun `should create transaction with valid enum ordinals`() {
        // Arrange & Act
        val transaction = TransactionEntity.create(
            id = 1L,
            sourceAccountId = 100L,
            targetAccountId = 200L,
            amount = BigDecimal("150.0000"),
            currency = CurrencyEnum.USD,
            timestamp = LocalDateTime.now(),
            status = TransactionStatusEnum.PENDING,
            reference = "Test transaction"
        )

        // Assert
        assertThat(transaction.currency).isEqualTo(CurrencyEnum.USD)
        assertThat(transaction.status).isEqualTo(TransactionStatusEnum.PENDING)
        assertThat(transaction.currency.ordinal).isEqualTo(CurrencyEnum.USD.ordinal)
        assertThat(transaction.status.ordinal).isEqualTo(TransactionStatusEnum.PENDING.ordinal)
    }

    @Test
    fun `should create transaction from string currency`() {
        // Arrange & Act
        val transaction = TransactionEntity.create(
            id = 1L,
            sourceAccountId = 100L,
            targetAccountId = 200L,
            amount = BigDecimal("150.0000"),
            currency = "EUR",
            timestamp = LocalDateTime.now(),
            status = TransactionStatusEnum.COMPLETED,
            reference = "String currency test"
        )

        // Assert
        assertThat(transaction.currency).isEqualTo(CurrencyEnum.EUR)
        assertThat(transaction.currency.ordinal).isEqualTo(CurrencyEnum.EUR.ordinal)
    }

    @Test
    fun `should reject invalid currency string`() {
        // Act & Assert
        assertThrows<IllegalArgumentException> {
            TransactionEntity.create(
                id = 1L,
                sourceAccountId = 100L,
                targetAccountId = 200L,
                amount = BigDecimal("150.0000"),
                currency = "INVALID",
                timestamp = LocalDateTime.now(),
                status = TransactionStatusEnum.PENDING,
                reference = "Invalid currency test"
            )
        }
    }

    @Test
    fun `should reject same source and target accounts`() {
        // Act & Assert
        assertThrows<IllegalArgumentException> {
            TransactionEntity.create(
                id = 1L,
                sourceAccountId = 100L,
                targetAccountId = 100L, // Same as source
                amount = BigDecimal("150.0000"),
                currency = CurrencyEnum.USD,
                timestamp = LocalDateTime.now(),
                status = TransactionStatusEnum.PENDING,
                reference = "Same account test"
            )
        }
    }

    @Test
    fun `should validate currency through factory method`() {
        // Test that invalid currency values are caught during creation
        assertThrows<IllegalArgumentException> {
            TransactionEntity.create(
                id = 1L,
                sourceAccountId = 100L,
                targetAccountId = 200L,
                amount = BigDecimal("150.0000"),
                currency = "INVALID_CURRENCY",
                timestamp = LocalDateTime.now(),
                status = TransactionStatusEnum.PENDING,
                reference = "Invalid currency test"
            )
        }
    }

    @Test
    fun `should update status correctly`() {
        // Arrange
        val originalTransaction = TransactionEntity.create(
            id = 1L,
            sourceAccountId = 100L,
            targetAccountId = 200L,
            amount = BigDecimal("150.0000"),
            currency = CurrencyEnum.USD,
            timestamp = LocalDateTime.now(),
            status = TransactionStatusEnum.PENDING,
            reference = "Status update test"
        )

        // Act
        val updatedTransaction = originalTransaction.withStatus(TransactionStatusEnum.COMPLETED)

        // Assert
        assertThat(updatedTransaction.status).isEqualTo(TransactionStatusEnum.COMPLETED)
        assertThat(updatedTransaction.status.ordinal).isEqualTo(TransactionStatusEnum.COMPLETED.ordinal)
        assertThat(updatedTransaction.id).isEqualTo(originalTransaction.id)
    }

    @Test
    fun `should update reference correctly`() {
        // Arrange
        val originalTransaction = TransactionEntity.create(
            id = 1L,
            sourceAccountId = 100L,
            targetAccountId = 200L,
            amount = BigDecimal("150.0000"),
            currency = CurrencyEnum.USD,
            timestamp = LocalDateTime.now(),
            status = TransactionStatusEnum.PENDING,
            reference = "Original reference"
        )

        // Act
        val updatedTransaction = originalTransaction.withReference("Updated reference")

        // Assert
        assertThat(updatedTransaction.reference).isEqualTo("Updated reference")
        assertThat(updatedTransaction.id).isEqualTo(originalTransaction.id)
    }

    @Test
    fun `should correctly identify if involves account`() {
        // Arrange
        val transaction = TransactionEntity.create(
            id = 1L,
            sourceAccountId = 100L,
            targetAccountId = 200L,
            amount = BigDecimal("150.0000"),
            currency = CurrencyEnum.USD,
            timestamp = LocalDateTime.now(),
            status = TransactionStatusEnum.COMPLETED,
            reference = "Account involvement test"
        )

        // Act & Assert
        assertThat(transaction.involvesAccount(100L)).isTrue()
        assertThat(transaction.involvesAccount(200L)).isTrue()
        assertThat(transaction.involvesAccount(300L)).isFalse()
    }

    @Test
    fun `should format amount correctly`() {
        // Arrange
        val transaction = TransactionEntity.create(
            id = 1L,
            sourceAccountId = 100L,
            targetAccountId = 200L,
            amount = BigDecimal("150.2500"),
            currency = CurrencyEnum.EUR,
            timestamp = LocalDateTime.now(),
            status = TransactionStatusEnum.COMPLETED,
            reference = "Format test"
        )

        // Act
        val formatted = transaction.getFormattedAmount()

        // Assert
        assertThat(formatted).contains("150.25")
        assertThat(formatted).contains("EUR")
    }

    @Test
    fun `should return currency as string`() {
        // Arrange
        val transaction = TransactionEntity.create(
            id = 1L,
            sourceAccountId = 100L,
            targetAccountId = 200L,
            amount = BigDecimal("150.0000"),
            currency = CurrencyEnum.GBP,
            timestamp = LocalDateTime.now(),
            status = TransactionStatusEnum.COMPLETED,
            reference = "Currency string test"
        )

        // Act & Assert
        assertThat(transaction.getCurrencyString()).isEqualTo("GBP")
    }

    @Test
    fun `should handle all currency types`() {
        // Test all enum values
        CurrencyEnum.values().forEach { currency ->
            val transaction = TransactionEntity.create(
                id = 1L,
                sourceAccountId = 100L,
                targetAccountId = 200L,
                amount = BigDecimal("100.0000"),
                currency = currency,
                timestamp = LocalDateTime.now(),
                status = TransactionStatusEnum.COMPLETED,
                reference = "Currency test: ${currency.name}"
            )

            assertThat(transaction.currency).isEqualTo(currency)
            assertThat(transaction.currency.ordinal).isEqualTo(currency.ordinal)
        }
    }

    @Test
    fun `should handle all status types`() {
        // Test all enum values
        TransactionStatusEnum.values().forEach { status ->
            val transaction = TransactionEntity.create(
                id = 1L,
                sourceAccountId = 100L,
                targetAccountId = 200L,
                amount = BigDecimal("100.0000"),
                currency = CurrencyEnum.USD,
                timestamp = LocalDateTime.now(),
                status = status,
                reference = "Status test: ${status.name}"
            )

            assertThat(transaction.status).isEqualTo(status)
            assertThat(transaction.status.ordinal).isEqualTo(status.ordinal)
        }
    }
}