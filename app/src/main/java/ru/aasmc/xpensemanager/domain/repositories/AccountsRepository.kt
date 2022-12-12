package ru.aasmc.xpensemanager.domain.repositories

import kotlinx.coroutines.flow.Flow
import ru.aasmc.xpensemanager.domain.model.Account
import ru.aasmc.xpensemanager.domain.model.AccountType
import ru.aasmc.xpensemanager.domain.model.Result
import java.math.BigDecimal

interface AccountsRepository {

    suspend fun getAccountsForType(type: AccountType): Result<List<Account>>

    suspend fun getAccountById(id: Long): Result<Account>

    suspend fun addNewAccount(account: Account): Result<Unit>

    fun observeTotalAmount(): Flow<BigDecimal>

    suspend fun getTotalAmount(): Result<BigDecimal>

    suspend fun getAllAccounts(): Result<List<Account>>

    suspend fun getAmountsGroupedByAccounts(): Result<Map<Account, BigDecimal>>

    suspend fun deleteAccount(id: Long): Result<Unit>

    suspend fun clearAllAccounts(): Result<Unit>

}