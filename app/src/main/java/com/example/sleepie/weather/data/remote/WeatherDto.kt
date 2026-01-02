package com.example.sleepie.weather.data.remote

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for the OpenWeatherMap Current Weather API (data/2.5/weather).
 * This structure matches the JSON response from the correct endpoint.
 */
@Serializable
data class WeatherResponse(
    val main: Main,
    val weather: List<WeatherDescription>,
    val name: String
)

@Serializable
data class Main(
    val temp: Double
)

@Serializable
data class WeatherDescription(
    val main: String,
    val description: String,
    val icon: String
)
