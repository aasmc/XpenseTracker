package ru.aasmc.xpensemanager.domain.repositories

import ru.aasmc.xpensemanager.domain.model.Account
import ru.aasmc.xpensemanager.domain.model.Category
import ru.aasmc.xpensemanager.domain.model.Expense
import ru.aasmc.xpensemanager.domain.model.Result
import java.util.*

interface ExpenseRepository {

    suspend fun spendMoney(expense: Expense): Result<Unit>

    suspend fun addMoney(expense: Expense): Result<Unit>

    suspend fun deleteExpense(expense: Expense): Result<Unit>

    suspend fun clearAllExpensesAndEarnings(): Result<Unit>

    suspend fun getExpensesAndEarningsForPeriod(from: Date, to: Date): Result<List<Expense>>

    suspend fun getExpensesAndEarningsForCategory(category: Category): Result<List<Expense>>

    suspend fun getExpensesAndEarningsForAccount(account: Account): Result<List<Expense>>

    suspend fun getAllEarningsForAccount(account: Account): Result<List<Expense>>

    suspend fun getAllExpensesAndEarnings(): Result<List<Expense>>

    suspend fun getAllExpensesOnly(): Result<List<Expense>>

    suspend fun getAllEarningsOnly(): Result<List<Expense>>
}