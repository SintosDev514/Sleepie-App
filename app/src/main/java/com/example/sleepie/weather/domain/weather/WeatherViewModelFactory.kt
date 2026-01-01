package com.example.sleepie.weather.domain.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sleepie.weather.data.repository.WeatherRepository
import com.example.sleepie.weather.domain.location.LocationTracker

class WeatherViewModelFactory(
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(repository, locationTracker) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
