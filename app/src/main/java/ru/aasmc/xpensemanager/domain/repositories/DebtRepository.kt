package ru.aasmc.xpensemanager.domain.repositories

import ru.aasmc.xpensemanager.domain.model.Debt
import ru.aasmc.xpensemanager.domain.model.Result
import java.util.Date

interface DebtRepository {

    fun getAllDebts(): Result<List<Debt>>

    fun addDebt(debt: Debt): Result<Unit>

    fun getAllDebtsForPeriod(from: Date, to: Date): Result<List<Debt>>

    fun deleteDebt(id: Long): Result<Unit>

    fun clearAllDebts(): Result<Unit>

}