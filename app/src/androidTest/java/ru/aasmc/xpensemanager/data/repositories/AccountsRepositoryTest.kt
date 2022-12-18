package ru.aasmc.xpensemanager.data.repositories

import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
import ru.aasmc.xpensemanager.domain.model.Account
import ru.aasmc.xpensemanager.domain.model.AccountType
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.domain.repositories.AccountsRepository
import ru.aasmc.xpensemanager.utils.cardAccount
import ru.aasmc.xpensemanager.utils.cardAccountId
import ru.aasmc.xpensemanager.utils.cashAccountId
import ru.aasmc.xpensemanager.utils.initialTotalAmount
import ru.aasmc.xpensemanager.utils.insertAccounts
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject
import kotlin.NoSuchElementException

@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class)
@UninstallModules(DatabaseModuleBinds::class, DatabaseModule::class)
class AccountsRepositoryTest : DatabaseTest() {
    @Inject
    lateinit var database: XpenseDatabase

    @Inject
    lateinit var accountsDao: AccountsDao

    @Inject
    lateinit var repo: AccountsRepository

    @Before
    fun setup() {
        hiltRule.inject()
        runBlocking {
            insertAccounts(database)
        }
    }

    @After
    fun tearDown() {
        database.clearAllTables()
    }

    @Test
    fun testGetAllAccounts_returnsTwoInitialAccounts() = runTest {
        val accountRes = repo.getAllAccounts()
        assertTrue(accountRes is Result.Success)
        val accounts = (accountRes as Result.Success).data.sortedBy { it.id }
        assertEquals(2, accounts.size)
        assertEquals("Cash", accounts[0].name)
        assertEquals("Card", accounts[1].name)
    }

    @Test
    fun getAccountsForType_happyPath() = runTest {
        val res = repo.getAccountsForType(AccountType.CARD)
        assertTrue(res is Result.Success)
        val accounts = (res as Result.Success).data
        assertEquals(1, accounts.size)
        assertEquals("Card", accounts[0].name)
        assertEquals(BigDecimal.TEN, accounts[0].amount)
        assertEquals(Currency.getInstance("RUB"), accounts[0].currency)
        assertEquals(cardAccountId, accounts[0].id)
    }

    @Test
    fun getAccountsForType_EmptyResult() = runTest {
        val res = repo.getAccountsForType(AccountType.BANK_ACCOUNT)
        assertTrue(res is Result.Success)
        val emptyList = (res as Result.Success).data
        assertTrue(emptyList.isEmpty())
    }

    @Test
    fun getAccountById_happyPath() = runTest {
        val res = repo.getAccountById(cardAccountId)
        assertTrue(res is Result.Success)
        val acc = (res as Result.Success).data
        assertEquals(cardAccount.id, acc.id)
        assertEquals(cardAccount.name, acc.name)
        assertEquals(cardAccount.amount, acc.amount)
        assertEquals(AccountType.CARD, acc.type)
        assertEquals(Currency.getInstance("RUB"), acc.currency)
    }

    @Test
    fun getAccountById_NoSuchElement() = runTest {
        val res = repo.getAccountById(100)
        assertTrue(res is Result.Error)
        val err = (res as Result.Error).exception
        assertTrue(err is NoSuchElementException)
        assertEquals("No account with id: 100 is stored in the database", err.message)
    }

    @Test
    fun addNewAccount_successfully_addsAccount_increases_totalAmount() = runTest {
        val newAccount = Account(
            type = AccountType.BANK_ACCOUNT,
            amount = BigDecimal.TEN,
            currency = Currency.getInstance("RUB"),
            name = "Bank"
        )
        val addRes = repo.addNewAccount(newAccount)
        assertTrue(addRes is Result.Success)

        val accRes = repo.getAllAccounts()
        assertTrue(accRes is Result.Success)
        val accs = (accRes as Result.Success).data
        assertEquals(3, accs.size)
        val newTotalRes = repo.getTotalAmount()
        assertTrue(newTotalRes is Result.Success)
        val newTotal = (newTotalRes as Result.Success).data
        assertEquals(initialTotalAmount.plus(BigDecimal.TEN), newTotal)
    }

    @Test
    fun addNewAccount_whenDB_isEmpty_createsAccount_and_totalAmount() = runTest {
        database.clearAllTables()
        val acc = Account(
            type = AccountType.BANK_ACCOUNT,
            amount = BigDecimal.TEN,
            currency = Currency.getInstance("RUB"),
            name = "Bank"
        )
        val res = repo.addNewAccount(acc)
        assertTrue(res is Result.Success)
        val accRes = repo.getAllAccounts()
        assertTrue(accRes is Result.Success)
        val accs = (accRes as Result.Success).data
        assertEquals(1, accs.size)

        val amountRes = repo.getTotalAmount()
        assertTrue(amountRes is Result.Success)
        val amount = (amountRes as Result.Success).data
        assertEquals(BigDecimal.TEN, amount)
    }

    @Test
    fun observeTotalAmount_correctlyObservesInitialAmount() = runTest {
        val amountFlow = repo.observeTotalAmount()
        val amountRes = amountFlow.first()
        assertTrue(amountRes is Result.Success)
        val amount = (amountRes as Result.Success).data
        assertEquals(initialTotalAmount, amount)
    }

    @Test
    fun observeTotalAmount_emptyDB_observesZero() = runTest {
        database.clearAllTables()
        val amountFlow = repo.observeTotalAmount()
        val amountRes = amountFlow.first()
        assertTrue(amountRes is Result.Success)
        val amount = (amountRes as Result.Success).data
        assertEquals(BigDecimal.ZERO, amount)
    }

    @Test
    fun getTotalAmount_retrievesInitialAmount() = runTest {
        val amountRes = repo.getTotalAmount()
        assertTrue(amountRes is Result.Success)
        val amount = (amountRes as Result.Success).data
        assertEquals(initialTotalAmount, amount)
    }

    @Test
    fun getTotalAmount_emptyDB_returnsZero() = runTest {
        database.clearAllTables()
        val amountRes = repo.getTotalAmount()
        assertTrue(amountRes is Result.Success)
        val amount = (amountRes as Result.Success).data
        assertEquals(BigDecimal.ZERO, amount)
    }

    @Test
    fun getAllAccounts_emptyDB_returnsEmptyList() = runTest {
        database.clearAllTables()
        val res = repo.getAllAccounts()
        assertTrue(res is Result.Success)
        val accs = (res as Result.Success).data
        assertTrue(accs.isEmpty())
    }

    @Test
    fun getAmountsGroupedByAccounts_retrievesMapWithTwoInitialAccounts() = runTest {
        val mapRes = repo.getAmountsGroupedByAccounts()
        assertTrue(mapRes is Result.Success)
        val map = (mapRes as Result.Success).data
        val keys = map.keys.sortedBy { it.id }
        assertEquals(2, keys.size)
        assertEquals(BigDecimal.TEN, map[keys[0]])
        assertEquals(BigDecimal.TEN, map[keys[1]])
    }

    @Test
    fun getAmountsGroupedByAccounts_emptyDB_emptyMap() = runTest {
        database.clearAllTables()
        val mapRes = repo.getAmountsGroupedByAccounts()
        assertTrue(mapRes is Result.Success)
        val map = (mapRes as Result.Success).data
        assertEquals(0, map.size)
    }

    @Test
    fun getAmountsGroupedByAccounts_returnsCorrectMap_whenAddingMoreAccounts() = runTest {
        val acc1 = Account(
            type = AccountType.BANK_ACCOUNT,
            amount = BigDecimal.ONE,
            currency = Currency.getInstance("RUB"),
            name = "Acc1"
        )

        val acc2 = Account(
            type = AccountType.CARD,
            amount = BigDecimal.valueOf(40),
            currency = Currency.getInstance("RUB"),
            name = "Acc2"
        )
        repo.addNewAccount(acc1)
        repo.addNewAccount(acc2)

        val mapRes = repo.getAmountsGroupedByAccounts()
        assertTrue(mapRes is Result.Success)
        val map = (mapRes as Result.Success).data

        assertEquals(4, map.size)
        val keys = map.keys.sortedBy { it.id }
        assertEquals(BigDecimal.TEN, map[keys[0]])
        assertEquals(BigDecimal.TEN, map[keys[1]])
        assertEquals(BigDecimal.ONE, map[keys[2]])
        assertEquals(BigDecimal.valueOf(40), map[keys[3]])
    }

    @Test
    fun observeAmountsGroupedByAccounts_correctlyObservesInitialAccounts() = runTest {
        val map = repo.observeAmountsGroupedByAccounts().first()
        assertEquals(2, map.size)
        val keys = map.keys.sortedBy { it.id }
        assertEquals(BigDecimal.TEN, map[keys[0]])
        assertEquals(BigDecimal.TEN, map[keys[1]])
    }

    @Test
    fun deleteAccount_deletesAccount_updatesTotalAmount() = runTest {
        val res = repo.deleteAccount(cashAccountId)
        assertTrue(res is Result.Success)
        val accRes = repo.getAllAccounts()
        assertTrue(accRes is Result.Success)
        val accs = (accRes as Result.Success).data
        assertEquals(1, accs.size)
        val fail = repo.getAccountById(cashAccountId)
        assertTrue(fail is Result.Error)
        val err = (fail as Result.Error).exception
        assertTrue(err is NoSuchElementException)

        val totalAmountRes = repo.getTotalAmount()
        assertTrue(totalAmountRes is Result.Success)
        val amount = (totalAmountRes as Result.Success).data
        assertEquals(BigDecimal.TEN, amount)
    }

    @Test
    fun deleteAccount_NoSuchElement_forIncorrectId() = runTest {
        val res = repo.deleteAccount(100)
        assertTrue(res is Result.Error)
        val err = (res as Result.Error).exception
        assertTrue(err is NoSuchElementException)
    }

    @Test
    fun clearAllAccounts_correctlyDeletesAllAccounts_UpdatesTotalAmountToZero() = runTest {
        val res = repo.clearAllAccounts()
        assertTrue(res is Result.Success)
        val accRes = repo.getAllAccounts()
        assertTrue(accRes is Result.Success)
        val accs = (accRes as Result.Success).data
        assertEquals(0, accs.size)

        val totalAmountRes = repo.getTotalAmount()
        assertTrue(totalAmountRes is Result.Success)
        val amount = (totalAmountRes as Result.Success).data
        assertEquals(BigDecimal.ZERO, amount)
    }

    @Test
    fun transferMoney_correctlyTransfersMoneyBetweenTwoExistingAccounts() = runTest {
        val res = repo.transferMoney(cashAccountId, cardAccountId, BigDecimal.valueOf(5L))
        assertTrue(res is Result.Success)
        val cardAcc = repo.getAccountById(cardAccountId)
        val card = (cardAcc as Result.Success).data
        assertEquals(BigDecimal.valueOf(15), card.amount)

        val cashAcc = repo.getAccountById(cashAccountId)
        val cash = (cashAcc as Result.Success).data
        assertEquals(BigDecimal.valueOf(5), cash.amount)
    }

    @Test
    fun transferMoney_whenCancelled_noAccountsAreUpdated() = runTest {
        val job = launch {
            repo.transferMoney(cashAccountId, cardAccountId, BigDecimal.valueOf(5L))
        }
        job.cancelAndJoin()
        val cardAcc = repo.getAccountById(cardAccountId)
        val card = (cardAcc as Result.Success).data
        assertEquals(BigDecimal.valueOf(10), card.amount)

        val cashAcc = repo.getAccountById(cashAccountId)
        val cash = (cashAcc as Result.Success).data
        assertEquals(BigDecimal.valueOf(10), cash.amount)
    }

    @Test
    fun transferMoney_NoSuchElement_forWrongFromAccount() = runTest {
        val errRes = repo.transferMoney(100, cashAccountId, BigDecimal.ONE)
        assertTrue(errRes is Result.Error)
        val err = (errRes as Result.Error).exception
        assertTrue(err is NoSuchElementException)
    }

    @Test
    fun transferMoney_NoSuchElement_forWrongToAccount() = runTest {
        val errRes = repo.transferMoney(cashAccountId, 100, BigDecimal.ONE)
        assertTrue(errRes is Result.Error)
        val err = (errRes as Result.Error).exception
        assertTrue(err is NoSuchElementException)
    }

    @Test
    fun transferMoney_InsufficientFundsException_when_transfersTooMuch() = runTest {
        val res = repo.transferMoney(cashAccountId, cardAccountId, BigDecimal.valueOf(11))
        assertTrue(res is Result.Error)
        val err = (res as Result.Error).exception
        assertTrue(err is InsufficientFundsException)
    }
}
