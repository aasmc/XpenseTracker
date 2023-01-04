package ru.aasmc.xpensemanager.data.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.aasmc.xpensemanager.data.network.ConnectionManager
import ru.aasmc.xpensemanager.domain.exceptions.NetworkUnavailableException
import javax.inject.Inject

class NetworkStatusInterceptor @Inject constructor(
    private val connectionManager: ConnectionManager
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (connectionManager.isConnected) {
            chain.proceed(chain.request())
        } else {
            throw NetworkUnavailableException()
        }
    }
}