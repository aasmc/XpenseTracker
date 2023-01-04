package ru.aasmc.xpensemanager.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConvertQuery(
    @field:Json(name = "amount") val amount: Double,
    @field:Json(name = "from") val from: String,
    @field:Json(name = "to") val to: String
)
