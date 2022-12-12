package ru.aasmc.xpensemanager.data.cache.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.aasmc.xpensemanager.domain.model.Account
import ru.aasmc.xpensemanager.domain.model.AccountType
import ru.aasmc.xpensemanager.domain.model.Category
import ru.aasmc.xpensemanager.domain.model.Expense
import java.math.BigDecimal
import java.util.*

@Entity(tableName = "expenses")
data class DBExpense(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "amount")
    val amount: BigDecimal,
    @ColumnInfo(name = "account_id")
    val accountId: Long,
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    @ColumnInfo(name = "is_earning")
    val isEarning: Boolean
) {

    fun toDomain(dbCategory: DBCategory, dbAccount: DBAccount): Expense = Expense(
        id = id,
        date = Date(date),
        amount = amount,
        category = Category(id = dbCategory.id, name = dbCategory.name),
        fromAccount = Account(
            id = dbAccount.id,
            type = DBAccountType.toDomainAccountType(dbAccount.type),
            amount = dbAccount.amount,
            currency = Currency.getInstance(dbAccount.currencyCode),
            name = dbAccount.name
        ),
        isEarning = isEarning
    )

    companion object {
        fun fromDomain(domain: Expense, categoryId: Long): DBExpense = DBExpense(
            id = domain.id,
            date = domain.date.time,
            amount = domain.amount,
            accountId = domain.fromAccount.id,
            categoryId = categoryId,
            isEarning = domain.isEarning
        )
    }
}
