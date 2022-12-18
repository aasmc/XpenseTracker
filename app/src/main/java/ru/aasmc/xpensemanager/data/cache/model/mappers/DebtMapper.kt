package ru.aasmc.xpensemanager.data.cache.model.mappers

import ru.aasmc.xpensemanager.data.cache.model.DBDebt
import ru.aasmc.xpensemanager.domain.model.Debt
import ru.aasmc.xpensemanager.domain.model.Mapper
import java.util.*
import javax.inject.Inject

class DebtMapper @Inject constructor(): Mapper<Debt, DBDebt> {
    override fun toDomain(dto: DBDebt): Debt = Debt(
        id = dto.id,
        name = dto.name,
        amount = dto.amount,
        currency = Currency.getInstance(dto.currencyCode),
        dueDate = Date(dto.dueDate)
    )

    override fun toDto(domain: Debt): DBDebt = DBDebt(
        id = domain.id,
        name = domain.name,
        amount = domain.amount,
        currencyCode = domain.currency.currencyCode,
        dueDate = domain.dueDate.time
    )
}