package ru.aasmc.xpensemanager.domain.model

import ru.aasmc.xpensemanager.data.cache.model.DBAccountType
import java.math.BigDecimal
import java.util.Currency

data class Account(
    val id: Long = 0,
    val type: AccountType,
    val amount: BigDecimal,
    val currency: Currency,
    val name: String
)

enum class AccountType {
    CASH,
    CARD,
    BANK_ACCOUNT;

    companion object {
        fun toDbAccountType(type: AccountType): DBAccountType {
            return when(type) {
                CASH -> DBAccountType.CASH
                CARD -> DBAccountType.CARD
                BANK_ACCOUNT -> DBAccountType.BANK_ACCOUNT
            }
        }
    }
}
