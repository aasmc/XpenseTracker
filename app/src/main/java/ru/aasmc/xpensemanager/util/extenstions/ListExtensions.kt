package ru.aasmc.xpensemanager.util.extenstions

fun <E> List<E>.selectProportions(selector: (E) -> Float): List<Float> {
    val total = this.sumOf { selector(it).toDouble() }
    return this.map { (selector(it) / total).toFloat() }
}