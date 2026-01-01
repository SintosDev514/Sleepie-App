package com.example.sleepie.weather.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val current: CurrentWeather,
    val daily: List<DailyWeather>
)

@Serializable
data class CurrentWeather(
    val temp: Double,
    val weather: List<WeatherDescription>
)

@Serializable
data class DailyWeather(
    @SerialName("dt") val timestamp: Long,
    val temp: Temperature,
    val weather: List<WeatherDescription>
)

@Serializable
data class Temperature(
    val day: Double,
    val min: Double,
    val max: Double
)

@Serializable
data class WeatherDescription(
    val main: String,
    val description: String,
    val icon: String
)
