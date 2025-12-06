package ru.yandex.buggyweatherapp

import android.app.Application
import ru.yandex.buggyweatherapp.di.AppContainer

/*
Изменения:

1. Удалил сохранение контекста в статическую переменную: потенцеальная утечка памяти.


2. Удалил ImageLoader, в нем были проблемы:
- Утечки памяти: ссылки на ImageView, которые никак не отчищаются,
- Статическая ссылка на application context
- Хранение битмапов в памяти без каких либо ограничений и механизма очищения кэша,
они могут занимать много памяти
- В целом сырая реализация механизма работы с изображенями

Для работы с изображенями из интернета в Android лучше использовать уже продуманные
и проверенные библиотеки. Будем использовать Coil, он хорошо подходит для Compose.
В нем уже реализовано кэширование изображений.


3. Удалил LocationTracker, c ним были следующие проблемы:

- утечка памяти: являясь синглтоном, он держал ссылку на контекст.

- В LocationRepository уже реализована логика получения обновлений локации. Логику
получения локации и получения обновлений локации разумно держать в одном классе.

 */
class WeatherApplication : Application() {
    lateinit var appContainer: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}