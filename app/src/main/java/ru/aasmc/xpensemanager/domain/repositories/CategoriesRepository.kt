package ru.aasmc.xpensemanager.domain.repositories

import kotlinx.coroutines.flow.Flow
import ru.aasmc.xpensemanager.domain.model.Category
import ru.aasmc.xpensemanager.domain.model.Result

interface CategoriesRepository {
    suspend fun addCategory(category: Category): Result<Unit>

    suspend fun getAllCategories(): Result<List<Category>>

    fun observeAllCategories(): Flow<List<Category>>

    suspend fun deleteCategory(category: Category): Result<Unit>
}