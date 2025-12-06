package ru.yandex.buggyweatherapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.ui.components.LocationSearch
import ru.yandex.buggyweatherapp.utils.WeatherIconMapper
import ru.yandex.buggyweatherapp.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel, modifier: Modifier = Modifier) {

    val uiState = viewModel.uiState.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LocationSearch(
            onCitySearch = { viewModel.requestWeatherByCity(it) },
            onLocationRequest = { viewModel.requestWeatherByLocation() },
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        when(val state = uiState.value) {
            is WeatherViewModel.UiState.Loading -> {
                Text("Loading weather data...")
            }
            is WeatherViewModel.UiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
            is WeatherViewModel.UiState.Success -> {
                WeatherCard(
                    weather = state.weather,
                    onFavoriteClick = { viewModel.toggleFavorite() },
                    onRefreshClick = { viewModel.refreshWeather() }
                )
            }
        }
    }
}

@Composable
fun WeatherCard(
    weather: WeatherData,
    onFavoriteClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weather.cityName,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Row {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (weather.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite"
                        )
                    }
                    
                    IconButton(onClick = onRefreshClick) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            
            Text(
                text = "Temperature: " + weather.temperature.toString() + "°C",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = "Feels like: " + weather.feelsLike.toString() + "°C",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Description: " + weather.description.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Humidity: " + weather.humidity.toString() + "%",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Wind: " + weather.windSpeed.toString() + " m/s",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                
                Text(
                    text = "Sunrise: " + WeatherIconMapper.formatTimestamp(weather.sunriseTime),
                    style = MaterialTheme.typography.bodySmall
                )
                
                Text(
                    text = "Sunset: " + WeatherIconMapper.formatTimestamp(weather.sunsetTime),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRefreshClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Refresh Weather")
            }
        }
    }
}