package ru.aasmc.xpensemanager.data.cache.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "currency_rates",
    primaryKeys = ["from_currency", "to_currency"]
)
data class DBCurrencyRate(
    @ColumnInfo(name = "from_currency")
    val from: String,
    @ColumnInfo(name = "to_currency")
    val to: String,
    @ColumnInfo(name = "rate")
    val rate: Double
)
