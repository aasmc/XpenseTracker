package ru.aasmc.xpensemanager.data.cache.model

import androidx.room.Embedded
import androidx.room.Relation

data class DBAccountExpenses(
    @Embedded
    val account: DBAccount,
    @Relation(
        parentColumn = "id",
        entityColumn = "account_id"
    )
    val expenses: List<DBExpense>
)
