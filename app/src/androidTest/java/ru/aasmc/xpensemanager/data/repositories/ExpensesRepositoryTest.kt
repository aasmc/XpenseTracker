package ru.aasmc.xpensemanager.data.repositories

import android.database.sqlite.SQLiteConstraintException
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.aasmc.xpensemanager.data.DatabaseTest
import ru.aasmc.xpensemanager.data.cache.dao.AccountsDao
import ru.aasmc.xpensemanager.data.cache.database.XpenseDatabase
import ru.aasmc.xpensemanager.di.DatabaseModule
import ru.aasmc.xpensemanager.di.DatabaseModuleBinds
import ru.aasmc.xpensemanager.domain.exceptions.InsufficientFundsException
import ru.aasmc.xpensemanager.domain.model.Expense
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.domain.repositories.ExpenseRepository
import ru.aasmc.xpensemanager.utils.carCategoryId
import ru.aasmc.xpensemanager.utils.cardAccountId
import ru.aasmc.xpensemanager.utils.cashAccountId
import ru.aasmc.xpensemanager.utils.domainEarningToCash
import ru.aasmc.xpensemanager.utils.domainExpenseFromCard
import ru.aasmc.xpensemanager.utils.domainExpenseFromCash
import ru.aasmc.xpensemanager.utils.getTestDate
import ru.aasmc.xpensemanager.utils.insertAccounts
import ru.aasmc.xpensemanager.utils.insertCategories
import java.math.BigDecimal
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(DatabaseModuleBinds::class, DatabaseModule::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesRepositoryTest: DatabaseTest() {

    @Inject
    lateinit var db: XpenseDatabase

    @Inject
    lateinit var repo: ExpenseRepository

    @Inject
    lateinit var accountsDao: AccountsDao

    @Before
    fun setup() {
        hiltRule.inject()
        runBlocking {
            insertCategories(db)
            insertAccounts(db)
        }
    }

    @After
    fun teardown() {
        db.clearAllTables()
    }

    @Test
    fun spendMoney_fromCash_happyPath_updatesAccountsAmountAndTotalAmount() = runTest {
        val res = repo.spendMoney(domainExpenseFromCash)
        assertTrue(res is Result.Success)
        val expense = (repo.getAllExpensesAndEarnings() as Result.Success).data
        assertEquals(1, expense.size)
        assertEquals(cashAccountId, expense[0].fromAccountId)

        val acc = accountsDao.getAccountById(cashAccountId)
        assertNotNull(acc)
        assertEquals(BigDecimal.valueOf(9), acc!!.amount)
        val total = accountsDao.getTotalAmount()
        assertNotNull(total)
        assertEquals(BigDecimal.valueOf(19), total!!.amount)
    }

    @Test
    fun spendMoney_NoSuchElement_forWrongAccountId() = runTest {
        val expense = Expense(
            date = getTestDate(0),
            amount = BigDecimal.ONE,
            categoryId = carCategoryId,
            fromAccountId = 100,
            isEarning = false
        )
        val res = repo.spendMoney(expense)
        assertTrue(res is Result.Error)
        val err = (res as Result.Error).exception
        assertTrue(err is NoSuchElementException)
    }

    @Test
    fun spendMoney_ErrorIfWrongCategoryId_accIsNotUpdated() = runTest {
        val expense = Expense(
            date = getTestDate(0),
            amount = BigDecimal.ONE,
            categoryId = 100,
            fromAccountId = cashAccountId,
            isEarning = false
        )
        val res = repo.spendMoney(expense)
        assertTrue(res is Result.Error)
        val err = (res as Result.Error).exception
        assertTrue(err is SQLiteConstraintException)

        val acc = accountsDao.getAccountById(cashAccountId)!!
        assertEquals(BigDecimal.TEN, acc.amount)
    }

    @Test
    fun spendMoney_InsufficientFunds_accIsNotUpdated() = runTest {
        val expense = Expense(
            date = getTestDate(0),
            amount = BigDecimal.valueOf(11),
            categoryId = carCategoryId,
            fromAccountId = cashAccountId,
            isEarning = false
        )
        val res = repo.spendMoney(expense)
        assertTrue(res is Result.Error)
        val err = (res as Result.Error).exception
        assertTrue(err is InsufficientFundsException)

        val acc = accountsDao.getAccountById(cashAccountId)!!
        assertEquals(BigDecimal.TEN, acc.amount)
    }

    @Test
    fun addMoney_happyPath_updatesAccountAndTotalAmount() = runTest {
        val res = repo.addMoney(domainEarningToCash)
        assertTrue(res is Result.Success)
        val expense = (repo.getAllExpensesAndEarnings() as Result.Success).data
        assertEquals(1, expense.size)
        assertEquals(cashAccountId, expense[0].fromAccountId)

        val acc = accountsDao.getAccountById(cashAccountId)
        assertNotNull(acc)
        assertEquals(BigDecimal.valueOf(11), acc!!.amount)
        val total = accountsDao.getTotalAmount()
        assertNotNull(total)
        assertEquals(BigDecimal.valueOf(21), total!!.amount)
    }

    @Test
    fun deleteExpense_happyPath_updatesAccountAmountAndTotalAmount() = runTest {
        val rrr = repo.spendMoney(domainExpenseFromCash) // Total: 20 -> 19
        assertTrue(rrr is Result.Success)
        val rr = repo.spendMoney(domainExpenseFromCard) // Total: 19 -> 18
        assertTrue(rr is Result.Success)
        val r = repo.addMoney(domainEarningToCash) // Total: 18 -> 19
        assertTrue(r is Result.Success)

        val res = repo.deleteExpense(domainExpenseFromCard) // Total: 19 -> 20
        assertTrue(res is Result.Success)

        var acc = accountsDao.getAccountById(cardAccountId)!!
        assertEquals(BigDecimal.TEN, acc.amount)

        var total = accountsDao.getTotalAmount()!!.amount
        assertEquals(BigDecimal.valueOf(20), total)

        repo.deleteExpense(domainEarningToCash)
        acc = accountsDao.getAccountById(cashAccountId)!!
        assertEquals(BigDecimal.valueOf(9), acc.amount)

        total = accountsDao.getTotalAmount()!!.amount
        assertEquals(BigDecimal.valueOf(19), total)

        repo.deleteExpense(domainExpenseFromCash)
        acc = accountsDao.getAccountById(cashAccountId)!!
        assertEquals(BigDecimal.valueOf(10), acc.amount)

        total = accountsDao.getTotalAmount()!!.amount
        assertEquals(BigDecimal.valueOf(20), total)
    }

    @Test
    fun clearAllExpensesAndEarnings_happyPath() = runTest {
        val r = repo.addMoney(domainEarningToCash)
        assertTrue(r is Result.Success)
        val rr = repo.spendMoney(domainExpenseFromCard)
        assertTrue(rr is Result.Success)
        val rrr = repo.spendMoney(domainExpenseFromCash)
        assertTrue(rrr is Result.Success)

        val res = repo.clearAllExpensesAndEarnings()
        assertTrue(res is Result.Success)

        val total = accountsDao.getTotalAmount()!!.amount
        assertEquals(BigDecimal.valueOf(20), total)

        var acc = accountsDao.getAccountById(cardAccountId)!!
        assertEquals(BigDecimal.TEN, acc.amount)

        acc = accountsDao.getAccountById(cashAccountId)!!
        assertEquals(BigDecimal.valueOf(10), acc.amount)
    }

}
























