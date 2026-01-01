package com.example.sleepie.weather.data.repository

import com.example.sleepie.weather.data.remote.WeatherResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class WeatherRepositoryImpl(private val client: HttpClient) : WeatherRepository {

    override suspend fun getWeatherData(latitude: Double, longitude: Double): WeatherResponse? {
        return try {
            client.get("https://api.openweathermap.org/data/3.0/onecall") {
                parameter("lat", latitude)
                parameter("lon", longitude)
                parameter("exclude", "minutely,hourly,alerts")
                parameter("units", "metric")
                parameter("appid", "e1698b1ea27dd357cb3181e74724f8f4")
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
