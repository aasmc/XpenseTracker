package ru.aasmc.xpensemanager.data.cache.model

import androidx.room.Embedded
import androidx.room.Relation

data class DBCategoryExpenses(
    @Embedded
    val category: DBCategory,
    @Relation(
        parentColumn = "id",
        entityColumn = "category_id"
    )
    val expenses: List<DBExpense>
)