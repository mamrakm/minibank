package cz.ememsoft.minibank.validation

/**
 * Composite pattern for combining multiple validation strategies.
 * Follows Composite Pattern and Single Responsibility Principle.
 */
class CompositeValidator<T>(
    private val strategies: List<ValidationStrategy<T>>
) : ValidationStrategy<T> {

    override fun validate(input: T) {
        strategies.forEach { strategy ->
            strategy.validate(input)
        }
    }

    companion object {
        /**
         * Builder pattern for creating composite validators.
         */
        fun <T> builder(): Builder<T> = Builder()

        class Builder<T> {
            private val strategies = mutableListOf<ValidationStrategy<T>>()

            fun addStrategy(strategy: ValidationStrategy<T>): Builder<T> {
                strategies.add(strategy)
                return this
            }

            fun build(): CompositeValidator<T> = CompositeValidator(strategies.toList())
        }
    }
}