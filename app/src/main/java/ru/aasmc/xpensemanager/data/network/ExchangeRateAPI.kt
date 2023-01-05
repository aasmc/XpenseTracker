package ru.aasmc.xpensemanager.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import ru.aasmc.xpensemanager.data.network.model.ApiConvertResponse

interface ExchangeRateAPI {
    @GET(CONVERT_ENDPOINT)
    suspend fun convert(
        @Query("to") to: String,
        @Query("from") from: String,
        @Query("amount") amount: Double
    ): Response<ApiConvertResponse>
}