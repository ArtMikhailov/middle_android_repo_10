package ru.yandex.buggyweatherapp.di

import android.app.Application
import ru.yandex.buggyweatherapp.repository.LocationRepository
import ru.yandex.buggyweatherapp.repository.WeatherRepository

class AppContainer(
    val application: Application,
) {
    val locationRepository by lazy { LocationRepository(application) }
    val weatherRepository by lazy { WeatherRepository() }
}