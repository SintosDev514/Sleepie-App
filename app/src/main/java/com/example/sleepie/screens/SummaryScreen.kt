package com.example.sleepie.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sleepie.data.db.SleepSession
import com.example.sleepie.viewModel.SleepViewModel
import com.example.sleepie.viewModel.SleepViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(navController: NavController, startTime: Long) {

    val application = LocalContext.current.applicationContext as Application
    val viewModel: SleepViewModel =
        viewModel(factory = SleepViewModelFactory(application))

    var selectedQuality by remember { mutableStateOf<String?>(null) }

    val endTime = System.currentTimeMillis()
    val durationInMillis = (endTime - startTime).coerceAtLeast(0)

    val days = TimeUnit.MILLISECONDS.toDays(durationInMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(durationInMillis) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % 60

    val durationString = when {
        days > 0 -> String.format("%dd %02dh %02dm", days, hours, minutes)
        hours > 0 -> String.format("%dh %02dm %02ds", hours, minutes, seconds)
        minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
        else -> String.format("%ds", seconds)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sleep Summary",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            // Sleep Duration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Total Sleep",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        durationString,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                "How was your sleep?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            QualitySelectionRow(
                selectedQuality = selectedQuality,
                onQualitySelected = { selectedQuality = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedQuality?.let { quality ->
                        val date = SimpleDateFormat(
                            "MMM dd, yyyy",
                            Locale.US
                        ).format(Date())

                        val session = SleepSession(
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            duration = durationString,
                            quality = quality
                        )

                        viewModel.insertSleepSession(session)

                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                },
                enabled = selectedQuality != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Save & Finish",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QualitySelectionRow(
    selectedQuality: String?,
    onQualitySelected: (String) -> Unit
) {
    val qualities = listOf("Excellent", "Good", "Poor")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        qualities.forEach { quality ->

            val selected = quality == selectedQuality

            FilterChip(
                selected = selected,
                onClick = { onQualitySelected(quality) },
                label = {
                    Text(
                        quality,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                leadingIcon = if (selected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
