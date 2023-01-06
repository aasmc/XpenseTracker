package ru.aasmc.xpensemanager.data.repositories

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import ru.aasmc.xpensemanager.data.network.NETWORK_TIMEOUT
import ru.aasmc.xpensemanager.domain.exceptions.NetworkException
import ru.aasmc.xpensemanager.domain.exceptions.NoSuchElementException
import ru.aasmc.xpensemanager.domain.model.Result
import ru.aasmc.xpensemanager.util.AppCoroutineDispatcher

suspend fun <T> safeCacheCall(
    block: suspend () -> T?
): Result<T> {
    return try {
        val res = block()
            ?: return Result.Error(NoSuchElementException("No such element in the database."))
        Result.Success(res)
    } catch (t: Throwable) {
        if (t is CancellationException) {
            throw t
        }
        Result.Error(t)
    }
}

suspend fun <T> safeApiCall(
    dispatcher: AppCoroutineDispatcher,
    apiCall: suspend () -> T?
): Result<T?> {
    return try {
        withContext(dispatcher.io) {
            Result.Success(apiCall.invoke())
        }
    } catch (t: Throwable) {
        if (t is CancellationException) {
            throw t
        }
        Result.Error(NetworkException(t.localizedMessage ?: "Unknown network error"))
    }

}