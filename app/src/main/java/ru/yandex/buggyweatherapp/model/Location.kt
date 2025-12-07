package ru.yandex.buggyweatherapp.model

/*
Удалил переопределение equals() и toString() по причине:
- не был переопределен hashCode(), что нарушает контракт между equals() и hashCode()
- в написании своих реализаций в данном случае нет необходимости,
data class автоматически генерирует для нас реализации этих методов.
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
)