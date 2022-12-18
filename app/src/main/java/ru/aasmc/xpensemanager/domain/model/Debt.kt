package ru.aasmc.xpensemanager.domain.model

import java.math.BigDecimal
import java.util.Currency
import java.util.Date

data class Debt(
    val id: Long = 0,
    val name: String,
    val amount: BigDecimal,
    val currency: Currency,
    val dueDate: Date
)
