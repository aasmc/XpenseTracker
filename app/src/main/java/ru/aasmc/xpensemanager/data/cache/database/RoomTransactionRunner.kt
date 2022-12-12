package ru.aasmc.xpensemanager.data.cache.database

import androidx.room.withTransaction


class RoomTransactionRunner(
    private val db: XpenseDatabase
): DatabaseTransactionRunner {
    override suspend fun <T> invoke(block: suspend () -> T): T {
        return db.withTransaction {
            block()
        }
    }

}