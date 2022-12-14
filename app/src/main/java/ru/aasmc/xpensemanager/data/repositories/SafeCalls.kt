package ru.aasmc.xpensemanager.data.repositories

import kotlinx.coroutines.CancellationException
import ru.aasmc.xpensemanager.domain.model.Result

suspend fun <T> safeCacheCall(
    block: suspend () -> T
): Result<T> {
    return try {
        Result.Success(block())
    } catch (t: Throwable) {
        if (t is CancellationException) {
            throw t
        }
        Result.Error(t)
    }
}