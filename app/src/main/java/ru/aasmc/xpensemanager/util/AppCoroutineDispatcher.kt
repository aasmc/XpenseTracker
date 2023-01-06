package ru.aasmc.xpensemanager.util

import kotlinx.coroutines.CoroutineDispatcher

data class AppCoroutineDispatcher(
    val io: CoroutineDispatcher,
    val computation: CoroutineDispatcher,
    val main: CoroutineDispatcher
)
