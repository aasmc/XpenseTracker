package ru.aasmc.xpensemanager.data.cache.database

interface DatabaseTransactionRunner {
    suspend operator fun <T> invoke(block: suspend () -> T) : T
}