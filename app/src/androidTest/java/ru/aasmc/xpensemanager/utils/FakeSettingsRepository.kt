package ru.aasmc.xpensemanager.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.aasmc.xpensemanager.domain.repositories.SettingsRepository

class FakeSettingsRepository(
    private val syncDelay: Long = 1000L
) : SettingsRepository {

    private var lastSyncTime = 0L

    private val themeFlow: MutableStateFlow<SettingsRepository.Theme> =
        MutableStateFlow(SettingsRepository.Theme.SYSTEM)
    private val currencyFlow: MutableStateFlow<String> =
        MutableStateFlow("RUB")

    override fun setup() {
        // No op
    }

    override var theme: SettingsRepository.Theme = SettingsRepository.Theme.SYSTEM
        set(value) {
            field = value
            themeFlow.tryEmit(value)
        }

    override fun observeTheme(): Flow<SettingsRepository.Theme> = themeFlow

    override var currencyCode: String = "RUB"
        set(value) {
            field = value
            currencyFlow.tryEmit(value)
        }

    override fun observeCurrencyCode(): Flow<String> = currencyFlow

    override fun shouldSyncCurrencyRates(): Boolean {
        return System.currentTimeMillis() - lastSyncTime > syncDelay
    }

    override fun setLastSyncTime(timeMs: Long) {
        lastSyncTime = timeMs
    }
}