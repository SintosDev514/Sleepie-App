package com.example.sleepie.weather.di

import android.app.Application
import com.example.sleepie.weather.data.repository.WeatherRepository
import com.example.sleepie.weather.data.repository.WeatherRepositoryImpl
import com.example.sleepie.weather.domain.location.LocationTracker
import com.example.sleepie.weather.domain.location.LocationTrackerImpl
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }
        }
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(app: Application): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }

    @Provides
    @Singleton
    fun provideLocationTracker(fusedLocationProviderClient: FusedLocationProviderClient, app: Application): LocationTracker {
        return LocationTrackerImpl(fusedLocationProviderClient, app)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(client: HttpClient): WeatherRepository {
        return WeatherRepositoryImpl(client)
    }
}
