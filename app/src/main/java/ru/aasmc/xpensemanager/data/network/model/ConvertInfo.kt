package ru.aasmc.xpensemanager.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConvertInfo(
    @field:Json(name = "rate") val rate: Double,
    @field:Json(name = "timestamp") val timestamp: Long
)
