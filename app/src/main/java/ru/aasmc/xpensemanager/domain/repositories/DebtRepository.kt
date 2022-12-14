package ru.aasmc.xpensemanager.domain.repositories

import kotlinx.coroutines.flow.Flow
import ru.aasmc.xpensemanager.domain.model.Debt
import ru.aasmc.xpensemanager.domain.model.Result
import java.util.Date

interface DebtRepository {

    suspend fun getAllDebts(): Result<List<Debt>>

    fun observeAllDebts(): Flow<List<Debt>>

    suspend fun addDebt(debt: Debt): Result<Unit>

    suspend fun getAllDebtsForPeriod(from: Date, to: Date): Result<List<Debt>>

    suspend fun deleteDebt(id: Long): Result<Unit>

    suspend fun clearAllDebts(): Result<Unit>

}