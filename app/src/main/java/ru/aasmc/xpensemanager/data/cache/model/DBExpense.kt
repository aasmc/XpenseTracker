package ru.aasmc.xpensemanager.data.cache.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.aasmc.xpensemanager.domain.model.Account
import ru.aasmc.xpensemanager.domain.model.Category
import ru.aasmc.xpensemanager.domain.model.Expense
import java.math.BigDecimal
import java.util.*

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = DBAccount::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("account_id"),
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DBCategory::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("category_id"),
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index("account_id"),
        Index("category_id")
    ]
)
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

    fun toDomain(): Expense = Expense(
        id = id,
        date = Date(date),
        amount = amount,
        categoryId = categoryId,
        fromAccountId = accountId,
        isEarning = isEarning
    )

    companion object {
        fun fromDomain(domain: Expense): DBExpense = DBExpense(
            id = domain.id,
            date = domain.date.time,
            amount = domain.amount,
            accountId = domain.fromAccountId,
            categoryId = domain.categoryId,
            isEarning = domain.isEarning
        )
    }
}
