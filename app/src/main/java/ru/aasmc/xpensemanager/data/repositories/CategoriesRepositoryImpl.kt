package ru.aasmc.xpensemanager.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aasmc.xpensemanager.data.cache.dao.CategoryDao
import ru.aasmc.xpensemanager.data.cache.model.mappers.CategoryMapper
import ru.aasmc.xpensemanager.domain.model.Category
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.domain.repositories.CategoriesRepository
import javax.inject.Inject

class CategoriesRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val categoryMapper: CategoryMapper
) : CategoriesRepository {

    override suspend fun addCategory(category: Category): Result<Unit> {
        return safeCacheCall {
            categoryDao.addCategory(categoryMapper.toDto(category))
        }
    }

    override suspend fun getAllCategories(): Result<List<Category>> {
        return safeCacheCall {
            categoryDao.getAllCategories().map {
                categoryMapper.toDomain(it)
            }
        }
    }

    override fun observeAllCategories(): Flow<List<Category>> {
        return categoryDao.observeAllCategories().map { dbCategories ->
            dbCategories.map { categoryMapper.toDomain(it) }
        }
    }

    override suspend fun deleteCategory(category: Category): Result<Unit> {
        return safeCacheCall {
            categoryDao.deleteCategory(categoryMapper.toDto(category))
        }
    }
}