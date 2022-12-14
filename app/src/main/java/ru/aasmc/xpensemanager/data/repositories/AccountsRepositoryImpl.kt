package ru.aasmc.xpensemanager.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aasmc.xpensemanager.data.cache.dao.AccountsDao
import ru.aasmc.xpensemanager.data.cache.database.DatabaseTransactionRunner
import ru.aasmc.xpensemanager.data.cache.model.DBAccount
import ru.aasmc.xpensemanager.data.cache.model.DBTotalAmount
import ru.aasmc.xpensemanager.data.cache.model.mappers.AccountMapper
import ru.aasmc.xpensemanager.domain.model.Account
import ru.aasmc.xpensemanager.domain.model.AccountType
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.domain.repositories.AccountsRepository
import java.math.BigDecimal

class AccountsRepositoryImpl constructor(
    private val accountsDao: AccountsDao,
    private val transactionRunner: DatabaseTransactionRunner,
    private val accountMapper: AccountMapper
) : AccountsRepository {

    override suspend fun getAccountsForType(type: AccountType): Result<List<Account>> {
        return safeCacheCall {
            accountsDao.getAccountsForType(AccountType.toDbAccountType(type))
                .map(accountMapper::toDomain)
        }
    }

    override suspend fun getAccountById(id: Long): Result<Account> {
        return safeCacheCall {
            val dbAccount = accountsDao.getAccountById(id)
            if (dbAccount != null) {
                accountMapper.toDomain(dbAccount)
            } else {
                throw NoSuchElementException("No account with id: $id is stored in the database")
            }
        }
    }

    override suspend fun addNewAccount(account: Account): Result<Unit> {
        return safeCacheCall {
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
            }
        }
    }

    override fun observeTotalAmount(): Flow<Result<BigDecimal>> {
        return accountsDao.observeTotalAmount()
            .map { total ->
                if (total != null) {
                    Result.Success(total.amount)
                } else {
                    Result.Success(BigDecimal.ZERO)
                }
            }
    }

    override suspend fun getTotalAmount(): Result<BigDecimal> {
        return safeCacheCall {
            accountsDao.getTotalAmount()?.amount ?: BigDecimal.ZERO
        }
    }

    override suspend fun getAllAccounts(): Result<List<Account>> {
        return safeCacheCall {
            accountsDao.getAllAccounts()
                .map { accountMapper.toDomain(it) }
        }
    }

    override suspend fun getAmountsGroupedByAccounts(): Result<Map<Account, BigDecimal>> {
        return safeCacheCall {
            val accounts = accountsDao.getAllAccounts().map {
                accountMapper.toDomain(it)
            }
            val map = accounts.associateWith { account ->
                account.amount
            }
            map
        }
    }

    override fun observeAmountsGroupedByAccounts(): Flow<Map<Account, BigDecimal>> {
        return accountsDao.observeAllAccounts().map { list ->
            list.map { accountMapper.toDomain(it) }.associateWith { account ->
                account.amount
            }
        }
    }

    override suspend fun deleteAccount(id: Long): Result<Unit> {
        return safeCacheCall {
            transactionRunner {
                val dbAccount = accountsDao.getAccountById(id)
                    ?: throw NoSuchElementException(
                        "Method transferMoney is called for account with ID: $id," +
                                " but no account is in the DB"
                    )
                val prevTotalAmount = accountsDao.getTotalAmount()
                    ?: throw NoSuchElementException(
                        "Method transferMoney is called but no total amount is in the DB."
                    )

                accountsDao.updateTotalAmount(
                    DBTotalAmount(
                        id = prevTotalAmount.id,
                        amount = prevTotalAmount.amount.minus(dbAccount.amount)
                    )
                )
                accountsDao.deleteAccount(id)
            }
        }
    }

    override suspend fun clearAllAccounts(): Result<Unit> {
        return safeCacheCall {
            transactionRunner {
                val prevTotalAmount = accountsDao.getTotalAmount()
                    ?: throw NoSuchElementException(
                        "Method transferMoney is called but no total amount is in the DB."
                    )
                accountsDao.updateTotalAmount(
                    DBTotalAmount(
                        id = prevTotalAmount.id,
                        amount = BigDecimal.ZERO
                    )
                )
                accountsDao.deleteAllAccounts()
            }
        }
    }

    override suspend fun transferMoney(
        fromAccount: Account,
        toAccount: Account,
        amount: BigDecimal
    ): Result<Unit> {
        return safeCacheCall {
            transactionRunner {
                val fromDBAccount = accountsDao.getAccountById(fromAccount.id)
                    ?: throw NoSuchElementException(
                        "Method transferMoney is called for account: $fromAccount," +
                                " but no account is in the DB"
                    )

                val toDBAccount = accountsDao.getAccountById(toAccount.id)
                    ?: throw NoSuchElementException(
                        "Method transferMoney is called for account: $toAccount," +
                                " but no account is in the DB"
                    )
                updateAccountAmount(
                    prevAccount = fromDBAccount,
                    newAmount = fromDBAccount.amount.minus(amount)
                )
                updateAccountAmount(
                    prevAccount = toDBAccount,
                    newAmount = toDBAccount.amount.plus(amount)
                )
            }
        }
    }

    private suspend fun updateAccountAmount(
        prevAccount: DBAccount,
        newAmount: BigDecimal
    ) {
        accountsDao.updateAccount(
            DBAccount(
                id = prevAccount.id,
                type = prevAccount.type,
                amount = newAmount,
                currencyCode = prevAccount.currencyCode,
                name = prevAccount.name
            )
        )
    }
}