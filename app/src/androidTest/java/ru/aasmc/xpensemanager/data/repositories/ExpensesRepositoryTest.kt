package ru.aasmc.xpensemanager.data.repositories

import android.database.sqlite.SQLiteConstraintException
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.aasmc.xpensemanager.data.DatabaseTest
import ru.aasmc.xpensemanager.data.cache.dao.AccountsDao
import ru.aasmc.xpensemanager.data.cache.dao.ExpenseDao
import ru.aasmc.xpensemanager.data.cache.database.XpenseDatabase
import ru.aasmc.xpensemanager.di.DatabaseModule
import ru.aasmc.xpensemanager.di.DatabaseModuleBinds
import ru.aasmc.xpensemanager.domain.exceptions.InsufficientFundsException
import ru.aasmc.xpensemanager.domain.model.Account
import ru.aasmc.xpensemanager.domain.model.AccountType
import ru.aasmc.xpensemanager.domain.model.Expense
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.domain.repositories.ExpenseRepository
import ru.aasmc.xpensemanager.utils.carCategoryEntity
import ru.aasmc.xpensemanager.utils.carCategoryId
import ru.aasmc.xpensemanager.utils.cardAccountEntity
import ru.aasmc.xpensemanager.utils.cardAccountId
import ru.aasmc.xpensemanager.utils.cashAccountEntity
import ru.aasmc.xpensemanager.utils.cashAccountId
import ru.aasmc.xpensemanager.utils.domainEarningToCash
import ru.aasmc.xpensemanager.utils.domainExpenseFromCard
import ru.aasmc.xpensemanager.utils.domainExpenseFromCash
import ru.aasmc.xpensemanager.utils.earningToCash
import ru.aasmc.xpensemanager.utils.expenseFromCard
import ru.aasmc.xpensemanager.utils.expenseFromCash
import ru.aasmc.xpensemanager.utils.getTestDate
import ru.aasmc.xpensemanager.utils.insertAccounts
import ru.aasmc.xpensemanager.utils.insertCategories
import ru.aasmc.xpensemanager.utils.invalidAccount
import ru.aasmc.xpensemanager.utils.salaryCategoryEntity
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject
import kotlin.NoSuchElementException

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

    @Inject
    lateinit var expensesDao: ExpenseDao

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

    @Test
    fun getExpensesAndEarningsForPeriod_validPeriod_notEmptyList() = runTest {
        addExpensesToDB()

        val fullList = repo.getExpensesAndEarningsForPeriod(
            getTestDate(-1),
            getTestDate(5)
        )
        assertTrue(fullList is Result.Success)
        val e1 = (fullList as Result.Success).data
        assertEquals(3, e1.size)

        val partialList = repo.getExpensesAndEarningsForPeriod(
            getTestDate(2),
            getTestDate(5)
        )
        assertTrue(partialList is Result.Success)
        val e2 = (partialList as Result.Success).data
        assertEquals(1, e2.size)
    }

    @Test
    fun getExpensesAndEarningsForPeriod_invalidPeriod_emptyList() = runTest {
        addExpensesToDB()

        val fullList = repo.getExpensesAndEarningsForPeriod(
            getTestDate(3),
            getTestDate(5)
        )
        assertTrue(fullList is Result.Success)
        val e1 = (fullList as Result.Success).data
        assertEquals(0, e1.size)
    }

    @Test
    fun getExpensesAndEarningsForCategory_validCategory() = runTest {
        addExpensesToDB()

        val result = repo.getExpensesAndEarningsForCategory(carCategoryEntity)
        checkResultListSize(result, 3)
    }

    @Test
    fun getExpensesAndEarningsForCategory_emptyList() = runTest {
        addExpensesToDB()

        val result = repo.getExpensesAndEarningsForCategory(salaryCategoryEntity)
        checkResultListSize(result, 0)
    }

    @Test
    fun getExpensesAndEarningsForAccount_validAccount() = runTest {
        addExpensesToDB()

        var result = repo.getExpensesAndEarningsForAccount(cardAccountEntity)
        checkResultListSize(result, 1)

        result = repo.getExpensesAndEarningsForAccount(cashAccountEntity)
        checkResultListSize(result, 2)
    }

    @Test
    fun getExpensesAndEarningsForAccount_invalidAccount() = runTest {
        addExpensesToDB()

        val result = repo.getExpensesAndEarningsForAccount(invalidAccount)
        checkResultListSize(result, 0)
    }

    @Test
    fun getAllEarningsForAccount_validAccount() = runTest {
        addExpensesToDB()

        val result = repo.getAllEarningsForAccount(cashAccountEntity)
        checkResultListSize(result, 1)
    }

    @Test
    fun getAllEarningsForAccount_invalidAccount() = runTest {
        addExpensesToDB()

        val result = repo.getAllEarningsForAccount(cardAccountEntity)
        checkResultListSize(result, 0)
    }

    @Test
    fun getAllExpensesAndEarnings_emptyList_whenEmptyDB() = runTest {
        val result = repo.getAllExpensesAndEarnings()
        checkResultListSize(result, 0)
    }

    @Test
    fun getAllExpensesAndEarnings_correctList_whenNotEmptyDB() = runTest {
        addExpensesToDB()
        val result = repo.getAllExpensesAndEarnings()
        checkResultListSize(result, 3)
    }

    @Test
    fun getAllExpensesOnly_emptyList_whenEmptyDB() = runTest {
        val result = repo.getAllExpensesOnly()
        checkResultListSize(result, 0)
    }

    @Test
    fun getAllExpensesOnly_notEmptyList_whenNotEmptyDB() = runTest {
        addExpensesToDB()
        val result = repo.getAllExpensesOnly()
        checkResultListSize(result, 2)
    }

    @Test
    fun getAllEarningsOnly_emptyList_whenEmptyDB() = runTest {
        val result = repo.getAllEarningsOnly()
        checkResultListSize(result, 0)
    }

    @Test
    fun getAllEarningsOnly_notEmptyList_whenNotEmptyDB() = runTest {
        addExpensesToDB()
        val result = repo.getAllEarningsOnly()
        checkResultListSize(result, 1)
    }

    @Test
    fun getAllExpensesAndEarnings_correctlyOrderedByDate() = runTest {
        addExpensesToDB()
        val result = repo.getAllExpensesAndEarnings()
        assertTrue(result is Result.Success)
        val list = (result as Result.Success).data
        assertEquals(domainEarningToCash, list[0])
        assertEquals(domainExpenseFromCard, list[1])
        assertEquals(domainExpenseFromCash, list[2])
    }

    private fun checkResultListSize(result: Result<List<Expense>>, expectedSize: Int) {
        assertTrue(result is Result.Success)
        val list = (result as Result.Success).data
        assertEquals(expectedSize, list.size)
    }

    private suspend fun addExpensesToDB() {
        expensesDao.addExpenseOrEarning(earningToCash)
        expensesDao.addExpenseOrEarning(expenseFromCard)
        expensesDao.addExpenseOrEarning(expenseFromCash)
    }
}
























