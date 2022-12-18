package ru.aasmc.xpensemanager.data.cache.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.aasmc.xpensemanager.domain.model.AccountType
import java.math.BigDecimal

@Entity(
    tableName = "accounts"
)
data class DBAccount(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "type")
    val type: DBAccountType,
    @ColumnInfo(name = "amount")
    val amount: BigDecimal,
    @ColumnInfo(name = "currency_code")
    val currencyCode: String,
    @ColumnInfo(name = "account_name")
    val name: String
)

enum class DBAccountType {
    CASH,
    CARD,
    BANK_ACCOUNT;

    companion object {
        fun toDomainAccountType(type: DBAccountType): AccountType {
            return when(type) {
                CASH -> AccountType.CASH
                CARD -> AccountType.CARD
                BANK_ACCOUNT -> AccountType.BANK_ACCOUNT
            }
        }
    }
}
