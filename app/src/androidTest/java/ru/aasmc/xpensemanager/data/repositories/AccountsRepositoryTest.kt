package ru.aasmc.xpensemanager.data.repositories

import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import ru.aasmc.xpensemanager.data.DatabaseTest
import ru.aasmc.xpensemanager.data.cache.dao.AccountsDao
import ru.aasmc.xpensemanager.data.cache.database.XpenseDatabase
import ru.aasmc.xpensemanager.di.DatabaseModule
import ru.aasmc.xpensemanager.di.DatabaseModuleBinds
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.domain.repositories.AccountsRepository
import ru.aasmc.xpensemanager.utils.insertAccounts
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class)
@UninstallModules(DatabaseModuleBinds::class, DatabaseModule::class)
class AccountsRepositoryTest: DatabaseTest() {
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

    @Test
    fun testGetAllAccounts_returnsTwoInitialAccounts() = runTest {
        val accountRes = repo.getAllAccounts()
        assertTrue(accountRes is Result.Success)
        val accounts = (accountRes as Result.Success).data.sortedBy { it.id }
        assertEquals(2, accounts.size)
        assertEquals("Cash", accounts[0].name)
        assertEquals("Card", accounts[1].name)
    }
}






















