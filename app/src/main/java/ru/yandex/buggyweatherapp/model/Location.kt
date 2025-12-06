package ru.yandex.buggyweatherapp.model

/*
Удалил переопределение equals() и toString() по причине:
1. не был переопределен hashCode(), что нарушает контракт между equals() и hashCode()
2. в написании своих реализаций в данном случае нет необходимости,
data class автоматически генерирует для нас реализации этих методов.
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
)