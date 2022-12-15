package ru.aasmc.xpensemanager.domain.repositories

import ru.aasmc.xpensemanager.domain.model.Account
import ru.aasmc.xpensemanager.domain.model.Category
import ru.aasmc.xpensemanager.domain.model.Expense
import ru.aasmc.xpensemanager.domain.model.Result
import java.util.*

/**
 * Interface describing actions taken with expenses and earnings.
 * Each expense MUST be associated with an account.
 * Each expense MUST be associated with a category.
 * TotalExpense MUST be present in the DB.
 */
interface ExpenseRepository {

    /**
     * 1. Saves [expense] which is not an earning to the DB.
     * 2. Subtracts the amount of the expense from the account it is associated with.
     * 3. Subtracts the amount of the expense from the totalAmount.
     */
    suspend fun spendMoney(expense: Expense): Result<Unit>

    /**
     * 1. Saves the [expense] which is an earning to the DB.
     * 2. Adds the amount of the expense to the account it is associated with.
     * 3. Adds the amount of the expense to the totalAmount.
     */
    suspend fun addMoney(expense: Expense): Result<Unit>

    /**
     * 1. Deletes the [expense] which is either an earning or the expense from the DB.
     * 2. If the expense is an earning it subtracts the amount from the account it is
     *    associated with, otherwise it adds the amount.
     * 3. The same logic applies to the totalAmount.
     */
    suspend fun deleteExpense(expense: Expense): Result<Unit>

    /**
     * 1. Deletes all expenses and earnings from the DB.
     * 2. Updates accounts, associated with the expenses accordingly.
     * 3. Updates totalAmount accordingly.
     * 4. After deleting all expenses and earnings, accounts still can have
     *    money, because they can be initially created with money. The same
     *    logic applies to the TotalAmount.
     */
    suspend fun clearAllExpensesAndEarnings(): Result<Unit>

    /**
     * 1. Retrieves all expenses and earnings for the specified period [from] and [to]
     * both inclusive.
     */
    suspend fun getExpensesAndEarningsForPeriod(from: Date, to: Date): Result<List<Expense>>

    /**
     * 1. Retrieves all expenses and earnings for the specified [category].
     */
    suspend fun getExpensesAndEarningsForCategory(category: Category): Result<List<Expense>>

    /**
     * 1. Retrieves all expenses and earnings for the specified [account].
     */
    suspend fun getExpensesAndEarningsForAccount(account: Account): Result<List<Expense>>

    /**
     * 1. Retrieves all earnings for the specified [account].
     */
    suspend fun getAllEarningsForAccount(account: Account): Result<List<Expense>>

    /**
     * 1. Retrieves all expenses and earnings stored in the DB.
     */
    suspend fun getAllExpensesAndEarnings(): Result<List<Expense>>

    /**
     * 1. Retrieves all expenses stored in the DB.
     */
    suspend fun getAllExpensesOnly(): Result<List<Expense>>

    /**
     * 1. Retrieves all earnings stored in the DB.
     */
    suspend fun getAllEarningsOnly(): Result<List<Expense>>
}