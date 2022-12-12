package ru.aasmc.xpensemanager.data.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.aasmc.xpensemanager.data.cache.model.DBExpense

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addExpenseOrEarning(expense: DBExpense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseOrEarning(id: Long)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpensesAndEarnings()

    @Query("SELECT * FROM expenses WHERE date >= :from AND date <= :to ORDER BY date ASC")
    suspend fun getExpensesAndEarningsForPeriod(from: Long, to: Long): List<DBExpense>

    @Query("SELECT * FROM expenses WHERE category_id = :categoryId ORDER BY date ASC")
    suspend fun getExpensesAndEarningsForCategory(categoryId: Long): List<DBExpense>

    @Query("SELECT * FROM expenses WHERE account_id = :accountId ORDER BY date ASC")
    suspend fun getExpensesAndEarningsForAccount(accountId: Long): List<DBExpense>

    @Query("SELECT * FROM expenses WHERE is_earning = 1 AND account_id = :accountId ORDER BY date ASC")
    suspend fun getAllEarningsForAccount(accountId: Long): List<DBExpense>

    @Query("SELECT * FROM expenses ORDER BY date ASC")
    suspend fun getAllExpensesAndEarnings(): List<DBExpense>

    @Query("SELECT * FROM expenses WHERE is_earning = 0 ORDER BY date ASC")
    suspend fun getAllExpensesOnly(): List<DBExpense>

    @Query("SELECT * FROM expenses WHERE is_earning = 1 ORDER BY date ASC")
    suspend fun getAllEarningsOnly(): List<DBExpense>
}


















