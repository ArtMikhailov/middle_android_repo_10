package ru.yandex.buggyweatherapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.repository.LocationRepository
import ru.yandex.buggyweatherapp.repository.WeatherRepository

/*
1. Удалил activityContext - это утечка памяти, жизненный цикл ViewModel не совподает с
жизненным циклом Activity.
2. Заменил LiveData на StateFlow
3. Убрал объявление нового coroutineScope в поле. Во вью модели можно использовать viewModelScope,
он автоматически отменится при релизе вью модели
4. Заменил механизм периодического обновления погоды с Timer.scheduleAtFixedRate()
на корутины с delay в цикле while(isActive)
5. Сделал внедрение зависимостей через конструктор
 */
class WeatherViewModel(
    val locationRepository: LocationRepository,
    val weatherRepository: WeatherRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val weather: WeatherData): UiState
        data class Error(val message: String): UiState
    }

    sealed interface WeatherMode {
        data object Location : WeatherMode
        data class City(val city: String) : WeatherMode
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var weatherMode: WeatherMode = WeatherMode.Location

    private var autoRefreshJob: Job? = null
    private var fetchWeatherJob: Job? = null

    fun onLocationPermissionGranted() {
        if (weatherMode == WeatherMode.Location) {
            requestWeatherByLocation()
        }
    }

    fun requestWeatherByLocation() {
        cancelWeatherJobs()
        weatherMode = WeatherMode.Location
        locationRepository.addLocationListener(::onCurrentLocation)
        fetchCurrentLocationWeather()
        startAutoRefresh()
    }

    fun requestWeatherByCity(city: String) {
        releaseCurrentLocationUpdates()
        cancelWeatherJobs()
        weatherMode = WeatherMode.City(city)
        searchWeatherByCity(city)
        startAutoRefresh()
    }

    fun refreshWeather() {
        when(val mode = weatherMode) {
            is WeatherMode.Location -> {
                fetchCurrentLocationWeather()
            }
            is WeatherMode.City -> {
                searchWeatherByCity(mode.city)
            }
        }
    }
    
    private fun fetchCurrentLocationWeather() {
        _uiState.value = UiState.Loading
        locationRepository.getCurrentLocation()
    }

    private fun onCurrentLocation(location: Location?) {
        if (location != null) {
            getWeatherForLocation(location)
        } else {
            _uiState.value = UiState.Error("Unable to get current location")
        }
    }

    private fun getWeatherForLocation(location: Location) {
        _uiState.value = UiState.Loading
        fetchWeatherJob = viewModelScope.launch {
            weatherRepository.getWeatherData(location).fold(
                onSuccess = { data ->
                    _uiState.value = UiState.Success(data)
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
    
    private fun searchWeatherByCity(city: String) {
        if (city.isBlank()) {
            _uiState.value = UiState.Error("City name cannot be empty")
            return
        }
        _uiState.value = UiState.Loading
        fetchWeatherJob = viewModelScope.launch {
            weatherRepository.getWeatherByCity(city).fold(
                onSuccess = { data ->
                    _uiState.value = UiState.Success(data)
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
    
    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(60000)
                refreshWeather()
            }
        }
    }

    private fun releaseCurrentLocationUpdates() {
        locationRepository.removeLocationListener()
        locationRepository.cancelLocationUpdates()
    }

    private fun cancelWeatherJobs() {
        fetchWeatherJob?.cancel()
        autoRefreshJob?.cancel()
    }

    fun toggleFavorite() {
        val state = uiState.value
        if (state is UiState.Success) {
            val isFavorite = state.weather.isFavorite
            val newWeatherData = state.weather.copy(isFavorite = !isFavorite)
            _uiState.value = UiState.Success(newWeatherData)
        }
    }

    override fun onCleared() {
        super.onCleared()
        releaseCurrentLocationUpdates()
        cancelWeatherJobs()
    }
}