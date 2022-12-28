package ru.aasmc.xpensemanager.util.extenstions

import kotlinx.coroutines.flow.Flow
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.presentation.model.UiMessage
import ru.aasmc.xpensemanager.presentation.model.UiMessageManager
import ru.aasmc.xpensemanager.util.logging.Logger

suspend fun <T> Flow<Result<T>>.collectErrorResult(
    uiMessageManager: UiMessageManager? = null,
    logger: Logger? = null
) = collect { result ->
    if (result is Result.Error) {
        logger?.i(result.exception)
        uiMessageManager?.emitMessage(UiMessage(result.exception))
    }
}