package ru.aasmc.xpensemanager.utils

import ru.aasmc.xpensemanager.data.cache.database.XpenseDatabase
import ru.aasmc.xpensemanager.data.cache.model.DBAccount
import ru.aasmc.xpensemanager.data.cache.model.DBAccountType
import ru.aasmc.xpensemanager.data.cache.model.DBCategory
import ru.aasmc.xpensemanager.data.cache.model.DBDebt
import ru.aasmc.xpensemanager.data.cache.model.DBExpense
import ru.aasmc.xpensemanager.data.cache.model.DBTotalAmount
import ru.aasmc.xpensemanager.domain.model.Account
import ru.aasmc.xpensemanager.domain.model.AccountType
import ru.aasmc.xpensemanager.domain.model.Category
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

val initialTotalAmount = BigDecimal.valueOf(20L)


internal suspend fun insertAccounts(db: XpenseDatabase) {
    val accDao = db.accountsDao()
    accDao.addNewAccount(cardAccount)
    accDao.addNewAccount(cashAccount)
    accDao.addTotalAmount(DBTotalAmount(
        id = 1,
        amount = initialTotalAmount
    ))
}
val cashAccountId = 1L
val cashAccount = DBAccount(
    id = cashAccountId,
    type = DBAccountType.CASH,
    amount = BigDecimal.TEN,
    currencyCode = "RUB",
    name = "Cash"
)
val cashAccountEntity = Account(
    id = cashAccountId,
    type = AccountType.CASH,
    amount = BigDecimal.TEN,
    currency = Currency.getInstance("RUB"),
    name = "Cash"
)

val cardAccountId = 2L
val cardAccount = DBAccount(
    id = cardAccountId,
    type = DBAccountType.CARD,
    amount = BigDecimal.TEN,
    currencyCode = "RUB",
    name = "Card"
)
val cardAccountEntity = Account(
    id = cardAccountId,
    type = AccountType.CARD,
    amount = BigDecimal.TEN,
    currency = Currency.getInstance("RUB"),
    name = "Card"
)

val invalidAccount = Account(
    id = 100,
    type = AccountType.CARD,
    amount = BigDecimal.ONE,
    currency = Currency.getInstance("RUB"),
    name = "Invalid"
)

internal suspend fun insertCategories(db: XpenseDatabase) {
    db.categoryDao().apply {
        addCategory(storeCategory)
        addCategory(carCategory)
        addCategory(salaryCategory)
    }
}

val storeCategoryId = 1L
val storeCategory = DBCategory(
    id = storeCategoryId,
    name = "Store"
)

val storeCategoryEntity = Category(
    id = storeCategoryId,
    name = storeCategory.name
)

val carCategoryId = 2L
val carCategory = DBCategory(
    id = carCategoryId,
    name = "Car"
)

val carCategoryEntity = Category(
    id = carCategoryId,
    name = carCategory.name
)

val salaryCategoryId = 3L
val salaryCategory = DBCategory(
    id = salaryCategoryId,
    name = "salary"
)

val salaryCategoryEntity = Category(
    id = salaryCategoryId,
    name = salaryCategory.name
)

internal fun addDays(date: Date, days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.add(Calendar.DATE, days)
    return calendar.time
}

val sdf = SimpleDateFormat("yyyy-MM-dd")
val testDateStr = "2022-12-18"

fun getTestDateStr(value: Long): String {
    val d = Date(value)
    return sdf.format(d)
}

internal fun getTestDate(plusDays: Int): Date {
    val date = sdf.parse(testDateStr)
    if (date != null) {
        return addDays(date, plusDays)
    } else {
        throw IllegalStateException("Date conversion error!")
    }
}

val debtId = 1L
val debt = DBDebt(
    id = debtId,
    name = "Debt",
    amount = BigDecimal.TEN,
    currencyCode = "RUB",
    dueDate = getTestDate(1).time
)

val debt2Id = 2L
val debt2 = DBDebt(
    id = debt2Id,
    name = "Debt2",
    amount = BigDecimal.TEN,
    currencyCode = "RUB",
    dueDate = getTestDate(2).time
)

val debt3Id = 3L
val debt3 = DBDebt(
    id = debt3Id,
    name = "Debt3",
    amount = BigDecimal.TEN,
    currencyCode = "RUB",
    dueDate = getTestDate(10).time
)

val debt4Id = 4L
val debt4 = DBDebt(
    id = debt4Id,
    name = "Debt4",
    amount = BigDecimal.TEN,
    currencyCode = "RUB",
    dueDate = getTestDate(15).time
)

internal suspend fun insertDebts(db: XpenseDatabase) {
    db.debtDao().apply {
        addDebt(debt)
        addDebt(debt2)
        addDebt(debt3)
        addDebt(debt4)
    }
}

val earningToCashId = 1L
val earningToCash = DBExpense(
    id = earningToCashId,
    date = getTestDate(0).time,
    amount = BigDecimal.ONE,
    accountId = cashAccountId,
    categoryId = carCategoryId,
    isEarning = true
)

val domainEarningToCash = earningToCash.toDomain()

val expenseFromCardId = 2L
val expenseFromCard = DBExpense(
    id = expenseFromCardId,
    date = getTestDate(1).time,
    amount = BigDecimal.ONE,
    accountId = cardAccountId,
    categoryId = carCategoryId,
    isEarning = false
)
val domainExpenseFromCard = expenseFromCard.toDomain()

val expenseFromCashId = 3L
val expenseFromCash = DBExpense(
    id = expenseFromCashId,
    date = getTestDate(2).time,
    amount = BigDecimal.ONE,
    accountId = cashAccountId,
    categoryId = carCategoryId,
    isEarning = false
)
val domainExpenseFromCash = expenseFromCash.toDomain()











