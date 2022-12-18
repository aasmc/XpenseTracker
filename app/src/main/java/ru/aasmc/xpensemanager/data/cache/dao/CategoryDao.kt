package ru.aasmc.xpensemanager.data.cache.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.aasmc.xpensemanager.data.cache.model.DBCategory

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCategory(category: DBCategory)

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<DBCategory>

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: Long)

    @Query("SELECT * FROM categories")
    fun observeAllCategories(): Flow<List<DBCategory>>
}