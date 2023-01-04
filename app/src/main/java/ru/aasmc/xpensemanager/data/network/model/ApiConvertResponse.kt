package ru.aasmc.xpensemanager.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiConvertResponse(
    @field:Json(name = "date") val date: String,
    @field:Json(name = "info") val info: ConvertInfo,
    @field:Json(name = "query") val convertQuery: ConvertQuery,
    @field:Json(name = "result") val result: Double,
    @field:Json(name = "success") val status: Boolean
)
