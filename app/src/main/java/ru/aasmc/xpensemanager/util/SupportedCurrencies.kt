package ru.aasmc.xpensemanager.util

import ru.aasmc.xpensemanager.R

val currencyToDarkIcon = hashMapOf<String, Int>(
    "RUB" to R.drawable.ruble_dark,
    "USD" to R.drawable.us_dollar_dark,
    "EUR" to R.drawable.euro_dark,
    "GBP" to R.drawable.british_pound_dark,
    "CNY" to R.drawable.yuan_dark
)

val currencyToLightIcon = hashMapOf<String, Int>(
    "RUB" to R.drawable.ruble_light,
    "USD" to R.drawable.us_dollar_light,
    "EUR" to R.drawable.euro_light,
    "GBP" to R.drawable.british_pound_light,
    "CNY" to R.drawable.yuan_light
)