package com.example.sleepie.screens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val viewModel: SleepViewModel = viewModel(factory = SleepViewModelFactory(application))
    var selectedQuality by remember { mutableStateOf<String?>(null) }

    val durationInMillis = (System.currentTimeMillis() - startTime).coerceAtLeast(0)
    val hours = TimeUnit.MILLISECONDS.toHours(durationInMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % 60
    val durationString = "%dh %02dm".format(hours, minutes)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sleep Summary") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Total Sleep", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(durationString, style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            
            Spacer(modifier = Modifier.height(48.dp))

            Text("How was your sleep?", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))

            QualitySelectionRow(selectedQuality) { quality ->
                selectedQuality = quality
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedQuality?.let { quality ->
                        val date = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
                        val session = SleepSession(date = date, duration = durationString, quality = quality)
                        viewModel.insertSleepSession(session)
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                },
                enabled = selectedQuality != null,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text("Save and Finish")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QualitySelectionRow(selectedQuality: String?, onQualitySelected: (String) -> Unit) {
    val qualities = listOf("Excellent", "Good", "Poor")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        qualities.forEach { quality ->
            val isSelected = quality == selectedQuality
            FilterChip(
                selected = isSelected,
                onClick = { onQualitySelected(quality) },
                label = { Text(quality) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}
