package cz.ememsoft.minibank.validation

/**
 * Factory pattern for creating request validators.
 * Follows Factory Pattern and Dependency Inversion Principle.
 */
object RequestValidatorFactory {

    /**
     * Creates a validator for account creation requests.
     */
    fun createAccountRequestValidator(): ValidationRequestValidator {
        return ValidationRequestValidator(
            MoneyValidationStrategy(allowZero = true),
            CurrencyValidationStrategy(),
            AccountTypeValidationStrategy()
        )
    }
}