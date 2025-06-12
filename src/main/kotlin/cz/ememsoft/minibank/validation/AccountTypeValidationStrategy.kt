
package cz.ememsoft.minibank.validation

import cz.ememsoft.minibank.enumeration.AccountTypeEnum
import cz.ememsoft.minibank.exception.ValidationException

/**
 * Validation strategy for account types.
 * Follows Single Responsibility Principle.
 */
class AccountTypeValidationStrategy : ValidationStrategy<String> {

    override fun validate(input: String) {
        val normalizedType = input.uppercase().trim()

        if (normalizedType.isBlank()) {
            throw ValidationException("Account type cannot be blank")
        }

        val validTypes = AccountTypeEnum.values().map { it.name }
        if (normalizedType !in validTypes) {
            throw ValidationException(
                "Invalid account type '$normalizedType'. Valid types: ${validTypes.joinToString()}"
            )
        }
    }
}