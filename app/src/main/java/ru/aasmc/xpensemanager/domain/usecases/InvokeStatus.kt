package ru.aasmc.xpensemanager.domain.usecases

sealed interface InvokeStatus

object InvokeStarted: InvokeStatus

object InvokeSuccess: InvokeStatus

data class InvokeError(val throwable: Throwable): InvokeStatus
