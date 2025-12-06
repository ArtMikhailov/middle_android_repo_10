package ru.yandex.buggyweatherapp.api

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.yandex.buggyweatherapp.BuildConfig
import java.util.concurrent.TimeUnit

/*
Изменеия:
- Добавил таймауты для OkHttpClient
- Добавил логирование запросов и ответов для дебажного билда
 */
object RetrofitInstance {

    private val retrofit by lazy {
        val okHttpBuilder = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            okHttpBuilder.addInterceptor(loggingInterceptor)
        }

        val okHttpClient = okHttpBuilder.build()

        Retrofit.Builder()
            .baseUrl(WeatherApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
    }
    
    val weatherApi: WeatherApiService = retrofit.create(WeatherApiService::class.java)
}