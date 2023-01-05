package ru.aasmc.xpensemanager.data.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.aasmc.xpensemanager.data.cache.model.DBCurrencyRate

@Dao
interface CurrencyRateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRate(rate: DBCurrencyRate)

    @Query("SELECT rate FROM currency_rates WHERE from_currency = :fromCurr and to_currency = :toCurr")
    suspend fun getRate(fromCurr: String, toCurr: String): Double

    @Query("SELECT * FROM currency_rates WHERE from_currency = :baseCurrency")
    suspend fun getRatesForCurrency(baseCurrency: String): List<DBCurrencyRate>
}
