
package cz.ememsoft.minibank.validation

/**
 * Strategy pattern for different validation approaches.
 * Follows Open/Closed Principle - open for extension, closed for modification.
 */
interface ValidationStrategy<T> {
    /**
     * Validates the given input according to this strategy.
     *
     * @param input The input to validate
     * @throws ValidationException if validation fails
     */
    fun validate(input: T)
}