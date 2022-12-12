package ru.aasmc.xpensemanager.data.repositories

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aasmc.xpensemanager.data.cache.dao.AccountsDao
import ru.aasmc.xpensemanager.data.cache.dao.ExpenseDao
import ru.aasmc.xpensemanager.data.cache.database.DatabaseTransactionRunner
import ru.aasmc.xpensemanager.data.cache.model.DBTotalAmount
import ru.aasmc.xpensemanager.data.cache.model.mappers.AccountMapper
import ru.aasmc.xpensemanager.domain.model.Account
import ru.aasmc.xpensemanager.domain.model.AccountType
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.domain.repositories.AccountsRepository
import java.math.BigDecimal

class AccountsRepositoryImpl(
    private val accountsDao: AccountsDao,
    private val expenseDao: ExpenseDao,
    private val transactionRunner: DatabaseTransactionRunner,
    private val accountMapper: AccountMapper
) : AccountsRepository {

    override suspend fun getAccountsForType(type: AccountType): Result<List<Account>> {
        return try {
            val accounts =
                accountsDao.getAccountsForType(AccountType.toDbAccountType(type))
                    .map(accountMapper::toDomain)
            Result.Success(accounts)
        } catch (t: Throwable) {
            if (t is CancellationException) {
                throw t
            }
            Result.Error(t)
        }
    }

    override suspend fun getAccountById(id: Long): Result<Account> {
        return try {
            val dbAccount = accountsDao.getAccountById(id)
            if (dbAccount != null) {
                Result.Success(data = accountMapper.toDomain(dbAccount))
            } else {
                Result.Error(NoSuchElementException("No account with id: $id is stored in the database"))
            }
        } catch (t: Throwable) {
            if (t is CancellationException) {
                throw t
            }
            Result.Error(t)
        }
    }

    override suspend fun addNewAccount(account: Account): Result<Unit> {
        return try {
            val dbAccount = accountMapper.toDto(account)
            transactionRunner {
                val oldTotal = accountsDao.getTotalAmount()
                if (oldTotal != null) {
                    val newTotal = DBTotalAmount(
                        id = oldTotal.id,
                        amount = oldTotal.amount.add(account.amount)
                    )
                    accountsDao.updateTotalAmount(newTotal)
                } else {
                    accountsDao.addTotalAmount(DBTotalAmount(amount = account.amount))
                }
                accountsDao.addNewAccount(dbAccount)
                Result.Success(Unit)
            }
        } catch (t: Throwable) {
            if (t is CancellationException) {
                throw t
            } else {
                Result.Error(t)
            }
        }
    }

    override fun observeTotalAmount(): Flow<BigDecimal> {
        return accountsDao.observeTotalAmount().map { it?.amount ?: BigDecimal.ZERO }
    }

    override suspend fun getTotalAmount(): Result<BigDecimal> {
        return try {
            val totalAmount = accountsDao.getTotalAmount()?.amount
            if (totalAmount != null) {
                Result.Success(totalAmount)
            } else {
                Result.Success(BigDecimal.ZERO)
            }
        } catch (t: Throwable) {
            if (t is CancellationException) {
                throw t
            }
            return Result.Error(t)
        }
    }

    override suspend fun getAllAccounts(): Result<List<Account>> {
        return try {
            val accounts = accountsDao.getAllAccounts()
                .map { accountMapper.toDomain(it) }
            return Result.Success(accounts)
        } catch (t: Throwable) {
            if (t is CancellationException) {
                throw t
            }
            Result.Error(t)
        }
    }

    override suspend fun getAmountsGroupedByAccounts(): Result<Map<Account, BigDecimal>> {
        return try {
            val accounts = accountsDao.getAllAccounts().map {
                accountMapper.toDomain(it)
            }
            val map = accounts.associateWith { account ->
                account.amount
            }
            Result.Success(map)
        } catch (t: Throwable) {
            if (t is CancellationException) {
                throw t
            }
            Result.Error(t)
        }
    }

    override suspend fun deleteAccount(id: Long): Result<Unit> {
        return try {
            accountsDao.deleteAccount(id)
            Result.Success(Unit)
        } catch (t: Throwable) {
            if (t is CancellationException) {
                throw t
            }
            return Result.Error(t)
        }
    }

    override suspend fun clearAllAccounts(): Result<Unit> {
        return try {
            accountsDao.deleteAllAccounts()
            Result.Success(Unit)
        } catch (t: Throwable) {
            if (t is CancellationException) {
                throw t
            }
            Result.Error(t)
        }
    }

}