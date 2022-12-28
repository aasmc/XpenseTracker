package ru.aasmc.xpensemanager.util.extenstions

import kotlinx.coroutines.flow.Flow
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.presentation.model.UiMessage
import ru.aasmc.xpensemanager.presentation.model.UiMessageManager

suspend fun <T> Flow<Result<T>>.collectErrorResult(
    uiMessageManager: UiMessageManager? = null
) = collect { result ->
    if (result is Result.Error) {
        uiMessageManager?.emitMessage(UiMessage(result.exception))
    }
}