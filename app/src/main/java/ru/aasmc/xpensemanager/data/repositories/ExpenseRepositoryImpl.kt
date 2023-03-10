package ru.aasmc.xpensemanager.data.repositories

import ru.aasmc.xpensemanager.data.cache.dao.AccountsDao
import ru.aasmc.xpensemanager.data.cache.dao.ExpenseDao
import ru.aasmc.xpensemanager.data.cache.database.DatabaseTransactionRunner
import ru.aasmc.xpensemanager.data.cache.model.DBAccount
import ru.aasmc.xpensemanager.data.cache.model.DBExpense
import ru.aasmc.xpensemanager.data.cache.model.DBTotalAmount
import ru.aasmc.xpensemanager.domain.exceptions.InsufficientFundsException
import ru.aasmc.xpensemanager.domain.model.Account
import ru.aasmc.xpensemanager.domain.model.Category
import ru.aasmc.xpensemanager.domain.model.Expense
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.domain.repositories.ExpenseRepository
import ru.aasmc.xpensemanager.util.AppCoroutineDispatcher
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject
import kotlin.NoSuchElementException

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val transactionRunner: DatabaseTransactionRunner,
    private val accountsDao: AccountsDao,
) : ExpenseRepository {

    override suspend fun spendMoney(expense: Expense): Result<Unit> {
        return safeCacheCall {
            transactionRunner {
                val dbAccount = accountsDao.getAccountById(expense.fromAccountId)
                    ?: throw NoSuchElementException(
                        "Method spendMoney is called for expense: $expense," +
                                " but not account associated with the expense is in the DB"
                    )
                val prevAmount = dbAccount.amount
                if (prevAmount < expense.amount) {
                    throw InsufficientFundsException(
                        "Account with id: ${dbAccount.id} has insufficient funds." +
                                "\nNeed: ${expense.amount}. Available: $prevAmount"
                    )
                }
                accountsDao.updateAccount(
                    DBAccount(
                        id = dbAccount.id,
                        type = dbAccount.type,
                        amount = prevAmount.minus(expense.amount),
                        currencyCode = dbAccount.currencyCode,
                        name = dbAccount.name
                    )
                )
                updateTotalAmount(expense.amount, false)
                val dto = DBExpense.fromDomain(expense)
                expenseDao.addExpenseOrEarning(dto)
            }
        }
    }

    override suspend fun addMoney(expense: Expense): Result<Unit> {
        return safeCacheCall {
            transactionRunner {
                val dbAccount = accountsDao.getAccountById(expense.fromAccountId)
                    ?: throw NoSuchElementException(
                        "Method spendMoney is called for expense: $expense," +
                                " but not account associated with the expense is in the DB"
                    )
                val prevAmount = dbAccount.amount
                accountsDao.updateAccount(
                    DBAccount(
                        id = dbAccount.id,
                        type = dbAccount.type,
                        amount = prevAmount.add(expense.amount),
                        currencyCode = dbAccount.currencyCode,
                        name = dbAccount.name
                    )
                )
                updateTotalAmount(expense.amount, true)
                val dto = DBExpense.fromDomain(expense)
                expenseDao.addExpenseOrEarning(dto)
            }
        }
    }

    override suspend fun deleteExpense(
        expenseId: Long,
        fromAccountId: Long,
        amount: BigDecimal,
        isEarning: Boolean
    ): Result<Unit> {
        return safeCacheCall {
            transactionRunner {
                val dbAccount = accountsDao.getAccountById(fromAccountId)
                    ?: throw NoSuchElementException(
                        "Method deleteExpense is called for expense with ID: $expenseId," +
                                " but not account associated with the expense is in the DB"
                    )

                updateAccountAmount(
                    prevAccount = dbAccount,
                    newAmount = if (isEarning) {
                        dbAccount.amount.minus(amount)
                    } else {
                        dbAccount.amount.add(amount)
                    }
                )
                updateTotalAmount(amount, !isEarning)
                expenseDao.deleteExpenseOrEarning(expenseId)
            }
        }
    }

    override suspend fun clearAllExpensesAndEarnings(): Result<Unit> {
        return safeCacheCall {
            transactionRunner {
                val expenses = expenseDao.getAllExpensesAndEarnings()
                val prevTotalAmount = accountsDao.getTotalAmount()
                    ?: throw NoSuchElementException("Error. No total amount is stored in the DB!")
                var amountForTotal = prevTotalAmount.amount

                for (expense in expenses) {
                    val dbAccount = accountsDao.getAccountById(expense.accountId)
                        ?: throw NoSuchElementException(
                            "Method clearAllExpensesAndEarnings is called for expense: $expense," +
                                    " but not account associated with the expense is in the DB"
                        )
                    amountForTotal = if (expense.isEarning) {
                        amountForTotal.minus(expense.amount)
                    } else {
                        amountForTotal.add(expense.amount)
                    }

                    updateAccountAmount(
                        prevAccount = dbAccount,
                        newAmount = if (expense.isEarning) {
                            dbAccount.amount.minus(expense.amount)
                        } else {
                            dbAccount.amount.add(expense.amount)
                        }
                    )
                }
                accountsDao.updateTotalAmount(
                    DBTotalAmount(id = prevTotalAmount.id, amount = amountForTotal)
                )
            }
        }
    }

    override suspend fun getExpensesAndEarningsForPeriod(
        from: Date,
        to: Date
    ): Result<List<Expense>> {
        return safeCacheCall {
            expenseDao.getExpensesAndEarningsForPeriod(from.time, to.time)
                .map { dbExpense ->
                    dbExpense.toDomain()
                }
        }
    }

    override suspend fun getExpensesAndEarningsForCategory(categoryId: Long): Result<List<Expense>> {
        return safeCacheCall {
            expenseDao.getExpensesAndEarningsForCategory(categoryId).map { dbExpense ->
                dbExpense.toDomain()
            }
        }
    }

    override suspend fun getExpensesAndEarningsForAccount(accountId: Long): Result<List<Expense>> {
        return safeCacheCall {
            expenseDao.getExpensesAndEarningsForAccount(accountId).map { dbExpense ->
                dbExpense.toDomain()
            }
        }
    }

    override suspend fun getAllEarningsForAccount(accountId: Long): Result<List<Expense>> {
        return safeCacheCall {
            expenseDao.getAllEarningsForAccount(accountId).map { dbExpense ->
                dbExpense.toDomain()
            }
        }
    }

    override suspend fun getAllExpensesAndEarnings(): Result<List<Expense>> {
        return safeCacheCall {
            expenseDao.getAllExpensesAndEarnings().map { dbExpense ->
                dbExpense.toDomain()
            }
        }
    }

    override suspend fun getAllExpensesOnly(): Result<List<Expense>> {
        return safeCacheCall {
            expenseDao.getAllExpensesOnly().map { dbExpense ->
                dbExpense.toDomain()
            }
        }
    }

    override suspend fun getAllEarningsOnly(): Result<List<Expense>> {
        return safeCacheCall {
            expenseDao.getAllEarningsOnly().map { dbExpense ->
                dbExpense.toDomain()
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

    private suspend fun updateTotalAmount(amount: BigDecimal, add: Boolean) {
        val prevTotal = accountsDao.getTotalAmount()
            ?: throw NoSuchElementException(
                "Error. No total amount is stored in the DB!"
            )
        val newAmount = if (add) {
            prevTotal.amount.add(amount)
        } else {
            prevTotal.amount.minus(amount)
        }
        accountsDao.updateTotalAmount(
            DBTotalAmount(
                id = prevTotal.id,
                amount = newAmount
            )
        )
    }
}