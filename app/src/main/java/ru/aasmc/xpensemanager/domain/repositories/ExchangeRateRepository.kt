package ru.aasmc.xpensemanager.domain.repositories

import ru.aasmc.xpensemanager.domain.model.Result
import java.math.BigDecimal

interface ExchangeRateRepository {
    suspend fun convert(fromCurrency: String, toCurrency: String, amount: BigDecimal): Result<BigDecimal>

    suspend fun syncExchangeRate(fromCurrency: String, toCurrency: String, force: Boolean = false): Result<Unit>

    suspend fun syncAllExchangeRates(force: Boolean = false): Result<Unit>
}