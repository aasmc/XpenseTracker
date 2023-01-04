package ru.aasmc.xpensemanager.data.network.interceptors

import okhttp3.logging.HttpLoggingInterceptor
import ru.aasmc.xpensemanager.util.logging.Logger
import javax.inject.Inject

class LoggingInterceptor @Inject constructor(
    private val logger: Logger
) : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        logger.i(message)
    }
}