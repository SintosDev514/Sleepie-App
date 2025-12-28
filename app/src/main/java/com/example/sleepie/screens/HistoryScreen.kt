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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sleepie.data.db.SleepSession
import com.example.sleepie.viewModel.SleepViewModel
import com.example.sleepie.viewModel.SleepViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {

    val application = LocalContext.current.applicationContext as Application
    val viewModel: SleepViewModel =
        viewModel(factory = SleepViewModelFactory(application))

    val sleepHistory by viewModel.allSleepSessions.collectAsState(emptyList())
    val density = LocalDensity.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sleep History") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.History,
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
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart ||
                            value == SwipeToDismissBoxValue.StartToEnd
                        ) {
                            viewModel.deleteSleepSession(session)
                            true
                        } else false
                    },
                    positionalThreshold = {
                        with(density) { 150.dp.toPx() }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {

                        val bgColor by animateColorAsState(
                            targetValue =
                                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled)
                                    Color.Red.copy(alpha = 0.85f)
                                else Color.Transparent,
                            label = ""
                        )

                        val scale by animateFloatAsState(
                            targetValue =
                                if (dismissState.targetValue == SwipeToDismissBoxValue.Settled)
                                    0.8f else 1.2f,
                            label = ""
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(bgColor, RoundedCornerShape(16.dp))
                                .padding(horizontal = 20.dp),
                            contentAlignment =
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                    else -> Alignment.CenterEnd
                                }

                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.scale(scale)
                            )
                        }
                    }
                ) {
                    SleepHistoryCard(session)
                }
            }
        }
    }
}

@Composable
fun SleepHistoryCard(record: SleepSession) {

    val formatter = remember {
        SimpleDateFormat("hh:mm a", Locale.US)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = record.date,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = record.duration,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formatter.format(Date(record.startTime))} - ${
                        formatter.format(Date(record.endTime))
                    }",
                    fontSize = 14.sp
                )

                Text(
                    text = record.quality,
                    fontWeight = FontWeight.Medium,
                    color = when (record.quality) {
                        "Excellent" -> Color(0xFF4CAF50)
                        "Good" -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }
                )
            }
        }
    }
}
