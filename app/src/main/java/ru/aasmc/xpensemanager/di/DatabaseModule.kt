package ru.aasmc.xpensemanager.di

import android.content.Context
import android.os.Debug
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.aasmc.xpensemanager.data.cache.dao.AccountsDao
import ru.aasmc.xpensemanager.data.cache.dao.CategoryDao
import ru.aasmc.xpensemanager.data.cache.dao.CurrencyRateDao
import ru.aasmc.xpensemanager.data.cache.dao.DebtDao
import ru.aasmc.xpensemanager.data.cache.dao.ExpenseDao
import ru.aasmc.xpensemanager.data.cache.database.AppDatabase
import ru.aasmc.xpensemanager.data.cache.database.DatabaseTransactionRunner
import ru.aasmc.xpensemanager.data.cache.database.RoomTransactionRunner
import ru.aasmc.xpensemanager.data.cache.database.XpenseDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): XpenseDatabase {
        val builder =
            Room.databaseBuilder(context, XpenseDatabase::class.java, "expenses.db")
                .fallbackToDestructiveMigration()
        if (Debug.isDebuggerConnected()) {
            builder.allowMainThreadQueries()
        }
        return builder.build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseDaoModule {
    @Provides
    fun provideAccountsDao(db: XpenseDatabase): AccountsDao = db.accountsDao()

    @Provides
    fun provideCategoryDao(db: XpenseDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideDebtDao(db: XpenseDatabase): DebtDao = db.debtDao()

    @Provides
    fun provideExpenseDao(db: XpenseDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideCurrencyRateDao(db: XpenseDatabase): CurrencyRateDao = db.currencyRateDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModuleBinds {
    @Binds
    abstract fun bindXpenseDatabase(impl: XpenseDatabase): AppDatabase

    @Binds
    abstract fun bindDBTransactionRunner(impl: RoomTransactionRunner): DatabaseTransactionRunner
}

















