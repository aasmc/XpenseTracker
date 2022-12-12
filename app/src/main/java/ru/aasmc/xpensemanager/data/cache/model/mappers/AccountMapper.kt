package ru.aasmc.xpensemanager.data.cache.model.mappers

import ru.aasmc.xpensemanager.data.cache.model.DBAccount
import ru.aasmc.xpensemanager.data.cache.model.DBAccountType
import ru.aasmc.xpensemanager.domain.model.Account
import ru.aasmc.xpensemanager.domain.model.AccountType
import ru.aasmc.xpensemanager.domain.model.Mapper
import java.util.*

class AccountMapper: Mapper<Account, DBAccount> {

    override fun toDomain(dto: DBAccount): Account = Account(
        id = dto.id,
        type = mapDtoToDomainType(dto.type),
        amount = dto.amount,
        currency = Currency.getInstance(dto.currencyCode),
        name = dto.name
    )

    private fun mapDtoToDomainType(type: DBAccountType): AccountType {
        return DBAccountType.toDomainAccountType(type)
    }

    override fun toDto(domain: Account): DBAccount = DBAccount(
        id = domain.id,
        type = mapDomainToDtoType(domain.type),
        amount = domain.amount,
        currencyCode = domain.currency.currencyCode,
        name = domain.name
    )

    private fun mapDomainToDtoType(type: AccountType): DBAccountType {
        return AccountType.toDbAccountType(type)
    }

}