package ru.aasmc.xpensemanager.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.aasmc.xpensemanager.util.logging.Logger
import ru.aasmc.xpensemanager.util.logging.LoggerImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class LoggerModule {
    @Singleton
    @Binds
    abstract fun provideLogger(impl: LoggerImpl): Logger
}