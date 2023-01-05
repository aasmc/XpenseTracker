package ru.aasmc.xpensemanager.data.repositories

import ru.aasmc.xpensemanager.data.cache.dao.CurrencyRateDao
import ru.aasmc.xpensemanager.data.network.ExchangeRateAPI
import ru.aasmc.xpensemanager.domain.exceptions.NetworkException
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.domain.repositories.ExchangeRateRepository
import ru.aasmc.xpensemanager.domain.repositories.SettingsRepository
import ru.aasmc.xpensemanager.util.logging.Logger
import java.math.BigDecimal
import javax.inject.Inject

class ExchangeRateRepositoryImpl @Inject constructor(
    private val currencyRateDao: CurrencyRateDao,
    private val api: ExchangeRateAPI,
    private val settingsRepository: SettingsRepository,
    private val logger: Logger
) : ExchangeRateRepository {

    /**
     * 1. Retrieves current rate for given currencies from DB.
     * 2. Converts given amount according to the retrieved rate.
     */
    override suspend fun convert(
        fromCurrency: String,
        toCurrency: String,
        amount: BigDecimal
    ): Result<BigDecimal> {
        val cacheResult = safeCacheCall {
            currencyRateDao.getRate(fromCurrency, toCurrency)
        }
        val rate =
            (cacheResult as? Result.Success)?.data ?: return cacheResult as Result.Error
        return Result.Success(data = amount.multiply(BigDecimal.valueOf(rate)))
    }

    /**
     * 1. Checks if forceSync or enough time has passed since previous sync (default - 24 hours)
     * 2. If true, makes an API call to get [ApiConvertResponse].
     * 3. If success - updates DB and last sync time.
     * @return Result.Success either if no need to sync or in case of successful sync,
     *         otherwise returns Result.Error with NetworkException.
     */
    override suspend fun syncExchangeRate(
        fromCurrency: String,
        toCurrency: String,
        force: Boolean
    ): Result<Unit> {
        if (force || settingsRepository.shouldSyncCurrencyRates()) {
            val response = api.convert(toCurrency, fromCurrency, 1.0)
            if (response.isSuccessful) {
                val apiResult = response.body() ?: return Result.Error(
                    NetworkException(
                        "Network request for currencies: $fromCurrency, $toCurrency returned empty response $response"
                    )
                )
                val dbResult = safeCacheCall {
                    currencyRateDao.upsertRate(apiResult.toDBEntity())
                }
                settingsRepository.setLastSyncTime(System.currentTimeMillis())
                return dbResult
            } else {
                val message =
                    getErrorMessageString(response.code(), fromCurrency, toCurrency)
                return Result.Error(
                    NetworkException(
                        message
                    )
                )
            }
        }
        return Result.Success(Unit)
    }

    /**
     * 1. Checks if forceSync or enough time has passed since previous sync (default - 24 hours)
     * 2. Retrieves all DB currency rates for base currency (stored in shared preferences)
     * 3. Makes an API call for every currency (TODO consider optimization)
     * 4. Saves the result in DB
     * 5. If sync of all currencies is a success then update last sync time in shared preferences.
     * 6. Error handling strategy:
     *   - If an error occurred while saving to DB, then log it and continue
     *   - If an error occurred while making an API call, then log it and return Result.Error
     *     with NetworkException
     */
    override suspend fun syncAllExchangeRates(force: Boolean): Result<Unit> {
        if (force || settingsRepository.shouldSyncCurrencyRates()) {
            val baseCurrency = settingsRepository.currencyCode
            val dbResult = safeCacheCall {
                currencyRateDao.getRatesForCurrency(baseCurrency)
            }
            val dbCurrencies =
                (dbResult as? Result.Success)?.data ?: return dbResult as Result.Error
            for (currency in dbCurrencies) {
                val apiResponse = api.convert(currency.to, currency.from, 1.0)
                if (apiResponse.isSuccessful) {
                    apiResponse.body()?.let { apiResult ->
                        val dbCurrencyRate = apiResult.toDBEntity()
                        val saveAttemptResult = safeCacheCall {
                            currencyRateDao.upsertRate(dbCurrencyRate)
                        }
                        if (saveAttemptResult is Result.Error) {
                            logger.e(
                                saveAttemptResult.exception,
                                "Error occurred while trying to save currency rate from ${dbCurrencyRate.from} to ${dbCurrencyRate.to}"
                            )
                        }
                    }
                } else {
                    val message = getErrorMessageString(
                        apiResponse.code(),
                        currency.from,
                        currency.to
                    )
                    logger.e(message)
                    return Result.Error(
                        NetworkException(
                            message
                        )
                    )
                }
            }
            settingsRepository.setLastSyncTime(System.currentTimeMillis())
        }
        return Result.Success(Unit)
    }

    private fun getErrorMessageString(
        code: Int,
        fromCurrency: String,
        toCurrency: String
    ): String {
        return if (code == 429) {
            "Sorry, current plan doesn't allow so many requests. Please try again in a month :("
        } else {
            "Error happened while retrieving data for currencies: $fromCurrency, $toCurrency from network"
        }
    }
}