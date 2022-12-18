package ru.aasmc.xpensemanager.data.repositories

import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.aasmc.xpensemanager.data.DatabaseTest
import ru.aasmc.xpensemanager.data.cache.database.XpenseDatabase
import ru.aasmc.xpensemanager.di.DatabaseModule
import ru.aasmc.xpensemanager.di.DatabaseModuleBinds
import ru.aasmc.xpensemanager.domain.model.Debt
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.domain.repositories.DebtRepository
import ru.aasmc.xpensemanager.utils.debt3Id
import ru.aasmc.xpensemanager.utils.getTestDate
import ru.aasmc.xpensemanager.utils.insertDebts
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@UninstallModules(DatabaseModuleBinds::class, DatabaseModule::class)
class DebtRepositoryTest: DatabaseTest() {
    @Inject
    lateinit var repo: DebtRepository

    @Inject
    lateinit var db: XpenseDatabase

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        db.clearAllTables()
    }

    @Test
    fun addDebt_correctlyAddsNewDebt() = runTest {
        val debt = Debt(
            name = "Name",
            amount = BigDecimal.TEN,
            currency = Currency.getInstance("RUB"),
            dueDate = getTestDate(1)
        )
        val res = repo.addDebt(debt)
        assertTrue(res is Result.Success)
        val allDebtsRes = repo.getAllDebts()
        assertTrue(allDebtsRes is Result.Success)
        val debts = (allDebtsRes as Result.Success).data
        assertEquals(1, debts.size)
    }

    @Test
    fun getAllDebts_emptyDB_emptyList() = runTest {
        val allDebtsRes = repo.getAllDebts()
        assertTrue(allDebtsRes is Result.Success)
        val debts = (allDebtsRes as Result.Success).data
        assertEquals(0, debts.size)
    }

    @Test
    fun observeAllDebts_correctlyObservesWhenDebtsAreInDB() = runTest {
        insertDebts(db)
        val debts = repo.observeAllDebts().first()
        assertEquals(4, debts.size)
    }

    @Test
    fun getAllDebtsForPeriod_emptyDB_emptyList() = runTest {
        val from = Date()
        val to = getTestDate(10)
        val res = repo.getAllDebtsForPeriod(from, to)
        assertTrue(res is Result.Success)
        val debts = (res as Result.Success).data
        assertEquals(0, debts.size)
    }

    @Test
    fun getAllDebtsForPeriod_correctlyRetrievesDebts() = runTest {
        insertDebts(db)
        val from = Date()
        val to = getTestDate(10)
        val res = repo.getAllDebtsForPeriod(from, to)
        assertTrue(res is Result.Success)
        val debts = (res as Result.Success).data
        assertEquals(3, debts.size)
    }

    @Test
    fun deleteDebt_correctlyDeletesDebt() = runTest {
        insertDebts(db)
        val res = repo.deleteDebt(debt3Id)
        assertTrue(res is Result.Success)
        val allDebtsRes = repo.getAllDebts()
        assertTrue(allDebtsRes is Result.Success)
        val debts = (allDebtsRes as Result.Success).data
        assertEquals(3, debts.size)
        assertFalse(debts.map { it.id }.contains(debt3Id))
    }

    @Test
    fun clearAllDebts_deletesAllDebtsFromDB() = runTest {
        insertDebts(db)
        val res = repo.clearAllDebts()
        assertTrue(res is Result.Success)
        val allDebtsRes = repo.getAllDebts()
        assertTrue(allDebtsRes is Result.Success)
        val debts = (allDebtsRes as Result.Success).data
        assertEquals(0, debts.size)
    }
}
