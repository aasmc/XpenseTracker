package ru.aasmc.xpensemanager.data.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.aasmc.xpensemanager.BuildConfig
import ru.aasmc.xpensemanager.data.network.API_KEY_HEADER
import javax.inject.Inject

class AuthenticationInterceptor @Inject constructor(): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val interceptedRequest = chain.request().newBuilder()
            .addHeader(API_KEY_HEADER, BuildConfig.API_KEY)
            .build()
        return chain.proceed(interceptedRequest)
    }
}