package com.example.sleepie.screens

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sleepie.data.db.SleepSession
import com.example.sleepie.viewModel.SleepViewModel
import com.example.sleepie.viewModel.SleepViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: SleepViewModel =
        viewModel(factory = SleepViewModelFactory(application))

    val sleepHistory by viewModel.allSleepSessions.collectAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sleep History") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sleepHistory, key = { it.id }) { session ->

                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            viewModel.deleteSleepSession(session)
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color by animateColorAsState(
                            if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                Color.Red
                            else Color.Transparent,
                            label = ""
                        )

                        val scale by animateFloatAsState(
                            if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                1.2f else 0.8f,
                            label = ""
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color, RoundedCornerShape(16.dp))
                                .padding(end = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.scale(scale)
                            )
                        }
                    },
                    content = {
                        SleepHistoryCard(session)
                    }
                )
            }
        }
    }
}

@Composable
fun SleepHistoryCard(record: SleepSession) {
    val qualityColor = when (record.quality) {
        "Excellent" -> Color(0xFF4CAF50)
        "Good" -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = record.date,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sleep: ${record.duration}")

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (record.quality == "Excellent" || record.quality == "Good")
                            Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = qualityColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = record.quality,
                        color = qualityColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
