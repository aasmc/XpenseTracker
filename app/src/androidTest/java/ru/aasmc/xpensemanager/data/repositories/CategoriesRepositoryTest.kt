package ru.aasmc.xpensemanager.data.repositories

import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.aasmc.xpensemanager.data.DatabaseTest
import ru.aasmc.xpensemanager.data.cache.dao.CategoryDao
import ru.aasmc.xpensemanager.data.cache.database.XpenseDatabase
import ru.aasmc.xpensemanager.di.DatabaseModule
import ru.aasmc.xpensemanager.di.DatabaseModuleBinds
import ru.aasmc.xpensemanager.domain.model.Category
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.domain.repositories.CategoriesRepository
import ru.aasmc.xpensemanager.utils.carCategoryId
import ru.aasmc.xpensemanager.utils.insertCategories
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class)
@UninstallModules(DatabaseModuleBinds::class, DatabaseModule::class)
class CategoriesRepositoryTest: DatabaseTest() {
    @Inject
    lateinit var database: XpenseDatabase

    @Inject
    lateinit var categoryDao: CategoryDao

    @Inject
    lateinit var repo: CategoriesRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        database.clearAllTables()
    }

    @Test
    fun addCategory_correctlyAddsNewCategoryToDB() = runTest {
        val category = Category(name = "Category")
        val res = repo.addCategory(category)
        assertTrue(res is Result.Success)

        val catRes = repo.getAllCategories()
        assertTrue(catRes is Result.Success)
        val cats = (catRes as Result.Success).data
        assertEquals(1, cats.size)
        assertEquals("Category", cats[0].name)
    }

    @Test
    fun addCategory_sameId_updatesCategory() = runTest {
        val category = Category(name = "Category")
        val res = repo.addCategory(category)
        assertTrue(res is Result.Success)

        var catRes = repo.getAllCategories()
        assertTrue(catRes is Result.Success)
        var cats = (catRes as Result.Success).data
        val catId = cats[0].id
        val newCat = Category(id = catId, name = "New Name")
        repo.addCategory(newCat)
        catRes = repo.getAllCategories()
        cats = (catRes as Result.Success).data
        assertEquals(1, cats.size)
        assertEquals("New Name", cats[0].name)
    }

    @Test
    fun getAllCategories_emptyDB_emptyList() = runTest {
        val res = repo.getAllCategories()
        assertTrue(res is Result.Success)
        val cats = (res as Result.Success).data
        assertEquals(0, cats.size)
    }

    @Test
    fun observeAllCategories_correctlyRetrievesCategories() = runTest {
        insertCategories(database)
        val res = repo.observeAllCategories().first().sortedBy { it.id }
        assertEquals(3, res.size)
    }

    @Test
    fun deleteCategory_correctlyDeletesCategory() = runTest {
        insertCategories(database)
        val res = repo.deleteCategory(carCategoryId)
        assertTrue(res is Result.Success)
        val catRes = repo.getAllCategories()
        assertTrue(catRes is Result.Success)
        val cats = (catRes as Result.Success).data
        assertEquals(2, cats.size)
    }
}

