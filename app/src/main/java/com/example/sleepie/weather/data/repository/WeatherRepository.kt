package com.example.sleepie.weather.data.repository

import com.example.sleepie.weather.data.remote.WeatherResponse

interface WeatherRepository {
    suspend fun getWeatherData(latitude: Double, longitude: Double): WeatherResponse?
}
