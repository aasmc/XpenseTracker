package ru.aasmc.xpensemanager.utils

import ru.aasmc.xpensemanager.data.cache.database.DatabaseTransactionRunner

object TestTransactionRunner: DatabaseTransactionRunner {
    override suspend fun <T> invoke(block: suspend () -> T): T = block()
}