package com.example.sleepie.weather.domain.weather

import com.example.sleepie.weather.data.remote.WeatherResponse

data class WeatherState(
    val weatherInfo: WeatherResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
