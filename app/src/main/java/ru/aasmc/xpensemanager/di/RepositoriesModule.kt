package ru.aasmc.xpensemanager.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.aasmc.xpensemanager.data.repositories.AccountsRepositoryImpl
import ru.aasmc.xpensemanager.data.repositories.CategoriesRepositoryImpl
import ru.aasmc.xpensemanager.data.repositories.DebtRepositoryImpl
import ru.aasmc.xpensemanager.data.repositories.ExpenseRepositoryImpl
import ru.aasmc.xpensemanager.domain.repositories.AccountsRepository
import ru.aasmc.xpensemanager.domain.repositories.CategoriesRepository
import ru.aasmc.xpensemanager.domain.repositories.DebtRepository
import ru.aasmc.xpensemanager.domain.repositories.ExpenseRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {

    @Singleton
    @Binds
    abstract fun bindAccountsRepository(impl: AccountsRepositoryImpl): AccountsRepository

    @Singleton
    @Binds
    abstract fun bindCategoriesRepository(impl: CategoriesRepositoryImpl): CategoriesRepository

    @Singleton
    @Binds
    abstract fun bindDebtRepository(impl: DebtRepositoryImpl): DebtRepository

    @Singleton
    @Binds
    abstract fun bindExpensesRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

}