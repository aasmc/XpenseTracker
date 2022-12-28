package ru.aasmc.xpensemanager.domain.repositories

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun setup()

    var theme: Theme
    fun observeTheme(): Flow<Theme>

    var currencyCode: String
    fun observeCurrencyCode(): Flow<String>

    enum class Theme {
        LIGHT,
        DARK,
        SYSTEM
    }
}