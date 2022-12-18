package ru.aasmc.xpensemanager.data.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.aasmc.xpensemanager.data.cache.model.DBAccount
import ru.aasmc.xpensemanager.data.cache.model.DBAccountType
import ru.aasmc.xpensemanager.data.cache.model.DBTotalAmount

@Dao
interface AccountsDao {

    @Query("SELECT * FROM accounts WHERE type= :type")
    suspend fun getAccountsForType(type: DBAccountType): List<DBAccount>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): DBAccount?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addNewAccount(account: DBAccount)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAccount(account: DBAccount)

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<DBAccount>

    @Query("SELECT * FROM accounts")
    fun observeAllAccounts(): Flow<List<DBAccount>>

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: Long)

    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()
    // we will have only one row with DBTotalAmount, need it because can't use
    // SQL SUM on strings - used to store BigDecimal
    @Query("SELECT * FROM total_amount LIMIT 1")
    fun observeTotalAmount(): Flow<DBTotalAmount?>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTotalAmount(newAmount: DBTotalAmount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTotalAmount(totalAmount: DBTotalAmount)

    @Query("SELECT * FROM total_amount WHERE id = 1")
    suspend fun getTotalAmount(): DBTotalAmount?

}