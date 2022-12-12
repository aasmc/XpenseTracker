package ru.aasmc.xpensemanager.data.cache.database

import ru.aasmc.xpensemanager.data.cache.dao.AccountsDao
import ru.aasmc.xpensemanager.data.cache.dao.DebtDao
import ru.aasmc.xpensemanager.data.cache.dao.ExpenseDao

interface AppDatabase {
    fun accountsDao(): AccountsDao

    fun debtDao(): DebtDao

    fun expenseDao(): ExpenseDao
}