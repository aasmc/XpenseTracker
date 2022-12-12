package ru.aasmc.xpensemanager.domain.model

import java.math.BigDecimal
import java.util.Date

data class Expense(
    val id: Long,
    val date: Date,
    val amount: BigDecimal,
    val category: Category,
    val fromAccount: Account,
    val isEarning: Boolean
)
