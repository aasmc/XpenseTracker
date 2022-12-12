package ru.aasmc.xpensemanager.data.cache.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.aasmc.xpensemanager.data.cache.converters.BigDecimalConverter
import ru.aasmc.xpensemanager.data.cache.model.*

@Database(
    entities = [
        DBAccount::class,
        DBCategory::class,
        DBDebt::class,
        DBExpense::class,
        DBTotalAmount::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(BigDecimalConverter::class)
abstract class XpenseDatabase : RoomDatabase(), AppDatabase