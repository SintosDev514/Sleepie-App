package com.example.sleepie.weather.domain.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sleepie.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeatherCard(state: WeatherState, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Today's Weather", fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null) {
                Text(state.error, color = MaterialTheme.colorScheme.error)
            } else if (state.weatherInfo != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = getWeatherIcon(state.weatherInfo.current.weather.first().icon)),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            "${state.weatherInfo.current.temp.toInt()}°C",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(state.weatherInfo.current.weather.first().main)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        state.weatherInfo.daily.take(7).forEach {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(SimpleDateFormat("EEE", Locale.getDefault()).format(Date(it.timestamp * 1000)))
                                Text("${it.temp.max.toInt()}° / ${it.temp.min.toInt()}°")
                            }
                        }
                    }
                }
            } else {
                Text("No weather data available.")
            }
        }
    }
}

fun getWeatherIcon(icon: String): Int {
    return when (icon) {
        "01d" -> R.drawable.ic_clear_sky
        "01n" -> R.drawable.ic_clear_sky_night
        "02d" -> R.drawable.ic_few_clouds
        "02n" -> R.drawable.ic_few_clouds_night
        "03d", "03n" -> R.drawable.ic_scattered_clouds
        "04d", "04n" -> R.drawable.ic_broken_clouds
        "09d", "09n" -> R.drawable.ic_shower_rain
        "10d" -> R.drawable.ic_rain
        "10n" -> R.drawable.ic_rain_night
        "11d", "11n" -> R.drawable.ic_thunderstorm
        "13d", "13n" -> R.drawable.ic_snow
        "50d", "50n" -> R.drawable.ic_mist
        else -> R.drawable.ic_clear_sky
    }
}
