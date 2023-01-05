package ru.aasmc.xpensemanager.data.repositories

import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import ru.aasmc.xpensemanager.data.DatabaseTest
import ru.aasmc.xpensemanager.data.cache.dao.CurrencyRateDao
import ru.aasmc.xpensemanager.data.cache.database.XpenseDatabase
import ru.aasmc.xpensemanager.data.network.ExchangeRateAPI
import ru.aasmc.xpensemanager.di.DatabaseModule
import ru.aasmc.xpensemanager.di.DatabaseModuleBinds
import ru.aasmc.xpensemanager.di.SettingsModuleBids
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.domain.repositories.ExchangeRateRepository
import ru.aasmc.xpensemanager.domain.repositories.SettingsRepository
import ru.aasmc.xpensemanager.util.logging.Logger
import ru.aasmc.xpensemanager.utils.FakeServer
import ru.aasmc.xpensemanager.utils.rubEurDBRate
import ru.aasmc.xpensemanager.utils.rubUsdDBRate
import ru.aasmc.xpensemanager.utils.rubUsdJsonRate
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class)
@UninstallModules(
    DatabaseModuleBinds::class,
    DatabaseModule::class,
    SettingsModuleBids::class
)
class ExchangeRateRepositoryTest : DatabaseTest() {
    @Inject
    lateinit var database: XpenseDatabase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var retrofitBuilder: Retrofit.Builder

    @Inject
    lateinit var logger: Logger

    private val fakeServer = FakeServer()
    private lateinit var api: ExchangeRateAPI
    lateinit var repo: ExchangeRateRepository
    lateinit var dao: CurrencyRateDao

    @Before
    fun setup() {
        fakeServer.start()
        hiltRule.inject()
        dao = database.currencyRateDao()
        api = retrofitBuilder
            .baseUrl(fakeServer.baseEndPoint)
            .build()
            .create(ExchangeRateAPI::class.java)
        repo = ExchangeRateRepositoryImpl(
            currencyRateDao = dao,
            api = api,
            settingsRepository = settingsRepository,
            logger = logger
        )
        runBlocking {
            insertRatesToDB()
        }
    }

    @After
    fun tearDown() {
        fakeServer.shutDown()
        database.clearAllTables()
    }

    @Test
    fun syncExchangeRate_success_syncHappened() = runTest {
        fakeServer.setHappyPathDispatcher("rates_rub_usd")
        val result = repo.syncExchangeRate("USD", "RUB")
        assertTrue(result is Result.Success)
        val dbRate = dao.getRate("USD", "RUB")
        assertEquals(rubUsdJsonRate, dbRate, 0.0001)
    }

    @Test
    fun syncExchangeRate_success_syncNotHappened() = runTest {
        fakeServer.setHappyPathDispatcher("rates_rub_usd")
        settingsRepository.setLastSyncTime(System.currentTimeMillis() + 1000)
        val result = repo.syncExchangeRate("USD", "RUB")
        assertTrue(result is Result.Success)
        val dbRate = dao.getRate("USD", "RUB")
        assertEquals(rubUsdDBRate.rate, dbRate, 0.0001)
    }

    private suspend fun insertRatesToDB() {
        dao.upsertRate(rubUsdDBRate)
        dao.upsertRate(rubEurDBRate)
    }

}
























