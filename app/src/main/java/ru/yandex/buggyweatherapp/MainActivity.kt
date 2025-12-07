package ru.yandex.buggyweatherapp

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ru.yandex.buggyweatherapp.ui.screens.WeatherScreen
import ru.yandex.buggyweatherapp.ui.theme.BuggyWeatherAppTheme
import ru.yandex.buggyweatherapp.viewmodel.WeatherViewModel

/*
Изменения:

1. Сделал инициализацию weatherViewModel через viewModels store.
Иначе вью модель не будет переживать смену конфигурации и ее поведение будет не корректным
(onCleared не будет вызван, и др.)

2. Скорректировал работу с разрешением на локацию:
- Добавил показ rationale диалога с объяснением необходимости разрешения на локацию
- Проверка и запрос разрешения перенесены в onStart, на случай если пользователь
дал разрешение в настройках приложения.
- Добавил передачу информации о статусе разрешения во вью модель.
 */
class MainActivity : ComponentActivity() {

    private var isLocationPermissionRequested = false

    private val weatherViewModel: WeatherViewModel by viewModels {
        viewModelFactory {
            initializer {
                val appContainer = (this[APPLICATION_KEY] as WeatherApplication).appContainer
                WeatherViewModel(appContainer.locationRepository, appContainer.weatherRepository)
            }
        }
    }
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                onLocationPermissionGranted()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                onLocationPermissionGranted()
            }
            else -> {
                onLocationPermissionDenied()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuggyWeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen(
                        viewModel = weatherViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        actualizeLocationPermissionStatus()
    }

    private fun actualizeLocationPermissionStatus() {
        if (isLocationPermissionGranted()) {
            onLocationPermissionGranted()
        } else if (!isLocationPermissionRequested) {
            isLocationPermissionRequested = true
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showLocationPermissionRationale()
            } else {
                requestLocationPermission()
            }
        } else {
            onLocationPermissionDenied()
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return hasCoarseLocation || hasFineLocation
    }

    private fun showLocationPermissionRationale() {
        val dialog = AlertDialog.Builder(this)
            .setMessage(R.string.location_permission_rationale_message)
            .setPositiveButton(R.string.alert_dialog_positive_button) { _, _ ->
                requestLocationPermission()
            }
            .setNegativeButton(R.string.alert_dialog_negative_button) { _, _ ->
                onLocationPermissionGranted()
            }
            .setCancelable(false)
            .create()
        dialog.show()
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun onLocationPermissionGranted() = weatherViewModel.onLocationPermissionGranted()
    private fun onLocationPermissionDenied() = weatherViewModel.onLocationPermissionDenied()
}