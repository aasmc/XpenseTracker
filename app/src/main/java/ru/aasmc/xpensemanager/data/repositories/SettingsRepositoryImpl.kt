package ru.aasmc.xpensemanager.data.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import ru.aasmc.xpensemanager.R
import ru.aasmc.xpensemanager.domain.repositories.SettingsRepository
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    private val defaultThemeValue = context.getString(R.string.pref_theme_default_value)
    private val defaultCurrencyValue =
        context.getString(R.string.pref_currency_default_value)

    private val preferenceKeyChangeFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 1
    )

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        preferenceKeyChangeFlow.tryEmit(key)
    }

    override fun setup() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override var theme: SettingsRepository.Theme
        get() = getThemeForStorageValue(
            sharedPreferences.getString(
                KEY_THEME,
                defaultThemeValue
            )!!
        )
        set(value) = sharedPreferences.edit {
            putString(KEY_THEME, value.storageKey)
        }

    override fun observeTheme(): Flow<SettingsRepository.Theme> {
        return preferenceKeyChangeFlow
            .onStart { emit(KEY_THEME) }
            .filter { it == KEY_THEME }
            .map { theme }
            .distinctUntilChanged()
    }

    override var currencyCode: String
        get() = sharedPreferences.getString(KEY_CURRENCY, defaultThemeValue)!!
        set(value) = sharedPreferences.edit {
            putString(KEY_CURRENCY, value)
        }

    override fun observeCurrencyCode(): Flow<String> {
        return preferenceKeyChangeFlow
            .onStart { emit(KEY_CURRENCY) }
            .filter { it == KEY_CURRENCY }
            .map { currencyCode }
            .distinctUntilChanged()
    }

    private val SettingsRepository.Theme.storageKey: String
        get() = when (this) {
            SettingsRepository.Theme.LIGHT -> context.getString(R.string.pref_theme_light_value)
            SettingsRepository.Theme.DARK -> context.getString(R.string.pref_theme_dark_value)
            SettingsRepository.Theme.SYSTEM -> context.getString(R.string.pref_theme_system_value)
        }

    private fun getThemeForStorageValue(value: String) = when (value) {
        context.getString(R.string.pref_theme_light_value) -> SettingsRepository.Theme.LIGHT
        context.getString(R.string.pref_theme_dark_value) -> SettingsRepository.Theme.DARK
        else -> SettingsRepository.Theme.SYSTEM
    }

    companion object {
        const val KEY_THEME = "pref_theme"
        const val KEY_CURRENCY = "pref_currency"
    }
}
