package cz.ememsoft.minibank.config

import cz.ememsoft.minibank.enumeration.CurrencyEnum
import cz.ememsoft.minibank.enumeration.TransactionStatusEnum
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.PostgresDialect

@Configuration
class DatabaseConfig(private val connectionFactory: ConnectionFactory) : AbstractR2dbcConfiguration() {

    override fun connectionFactory(): ConnectionFactory = connectionFactory

    @Bean
    override fun r2dbcCustomConversions(): R2dbcCustomConversions {
        val converters = listOf(
            IntegerToCurrencyEnumConverter(),
            CurrencyEnumToIntegerConverter(),
            IntegerToTransactionStatusEnumConverter(),
            TransactionStatusEnumToIntegerConverter()
        )
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters)
    }
}

// --- Converters ---

@ReadingConverter
class IntegerToCurrencyEnumConverter : Converter<Int, CurrencyEnum> {
    override fun convert(source: Int): CurrencyEnum = CurrencyEnum.entries[source]
}

@WritingConverter
class CurrencyEnumToIntegerConverter : Converter<CurrencyEnum, Int> {
    override fun convert(source: CurrencyEnum): Int = source.ordinal
}

@ReadingConverter
class IntegerToTransactionStatusEnumConverter : Converter<Int, TransactionStatusEnum> {
    override fun convert(source: Int): TransactionStatusEnum = TransactionStatusEnum.entries[source]
}

@WritingConverter
class TransactionStatusEnumToIntegerConverter : Converter<TransactionStatusEnum, Int> {
    override fun convert(source: TransactionStatusEnum): Int = source.ordinal
}