package ru.aasmc.xpensemanager.data.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.aasmc.xpensemanager.data.cache.model.DBDebt

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY due_date ASC")
    suspend fun getAllDebts(): List<DBDebt>

    @Query("SELECT * FROM debts ORDER BY due_date ASC")
    fun observeAllDebts(): Flow<List<DBDebt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDebt(debt: DBDebt)

    @Query("""
        SELECT * FROM debts WHERE due_date >= :from AND due_date <= :to 
        ORDER by due_date ASC
    """)
    suspend fun getAllDebtsForPeriod(from: Long, to: Long): List<DBDebt>

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebt(id: Long)

    @Query("DELETE FROM debts")
    suspend fun clearAllDebts()
}