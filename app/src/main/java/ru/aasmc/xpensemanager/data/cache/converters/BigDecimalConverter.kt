package ru.aasmc.xpensemanager.data.cache.converters

import androidx.room.TypeConverter
import java.math.BigDecimal

class BigDecimalConverter {
    @TypeConverter
    fun fromString(value: String?): BigDecimal {
        return value?.toBigDecimalOrNull() ?: BigDecimal.ZERO
    }

    @TypeConverter
    fun amountToString(amount: BigDecimal?): String? {
        return amount?.toPlainString()
    }
}