package com.example.sleepie.weather.data.repository

import com.example.sleepie.weather.data.remote.WeatherResponse
import io.ktor.client.* 
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class WeatherRepositoryImpl(private val client: HttpClient) : WeatherRepository {

    override suspend fun getWeatherData(latitude: Double, longitude: Double): Result<WeatherResponse> {
        return try {
            // ✅ FIX: Use the correct, more compatible API endpoint
            val response: HttpResponse = client.get("https://api.openweathermap.org/data/2.5/weather") {
                parameter("lat", latitude)
                parameter("lon", longitude)
                parameter("units", "metric")
                parameter("appid", "e1698b1ea27dd357cb3181e74724f8f4")
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("API Error: ${response.status.value} - $errorBody"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
