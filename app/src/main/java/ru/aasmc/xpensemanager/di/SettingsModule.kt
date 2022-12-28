package ru.aasmc.xpensemanager.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.aasmc.xpensemanager.data.repositories.SettingsRepositoryImpl
import ru.aasmc.xpensemanager.domain.repositories.SettingsRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
internal abstract class SettingsModuleBids {
    @Singleton
    @Binds
    abstract fun provideSharedPreferences(impl: SettingsRepositoryImpl): SettingsRepository
}

@InstallIn(SingletonComponent::class)
@Module
object SettingsModule {

    @Provides
    @Singleton
    fun provideAppPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private const val SHARED_PREFS_NAME = "app_shared_preferences"
}