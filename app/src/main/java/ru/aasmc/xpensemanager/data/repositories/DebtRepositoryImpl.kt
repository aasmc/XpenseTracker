package ru.aasmc.xpensemanager.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aasmc.xpensemanager.data.cache.dao.DebtDao
import ru.aasmc.xpensemanager.data.cache.model.mappers.DebtMapper
import ru.aasmc.xpensemanager.domain.model.Debt
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.domain.repositories.DebtRepository
import java.util.*

class DebtRepositoryImpl(
    private val debtDao: DebtDao,
    private val mapper: DebtMapper
) : DebtRepository {
    override suspend fun getAllDebts(): Result<List<Debt>> {
        return safeCacheCall {
            debtDao.getAllDebts().map { mapper.toDomain(it) }
        }
    }

    override fun observeAllDebts(): Flow<List<Debt>> {
        return debtDao.observeAllDebts().map { debts ->
            debts.map { mapper.toDomain(it) }
        }
    }

    override suspend fun addDebt(debt: Debt): Result<Unit> {
        return safeCacheCall {
            debtDao.addDebt(mapper.toDto(debt))
        }
    }

    override suspend fun getAllDebtsForPeriod(from: Date, to: Date): Result<List<Debt>> {
        return safeCacheCall {
            debtDao.getAllDebtsForPeriod(from.time, to.time).map {
                mapper.toDomain(it)
            }
        }
    }

    override suspend fun deleteDebt(id: Long): Result<Unit> {
        return safeCacheCall {
            debtDao.deleteDebt(id)
        }
    }

    override suspend fun clearAllDebts(): Result<Unit> {
        return safeCacheCall {
            debtDao.clearAllDebts()
        }
    }
}