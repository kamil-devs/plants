package com.example.pruningapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pruningapp.data.Task
import com.example.pruningapp.data.WeatherData
import com.example.pruningapp.viewmodel.PlantViewModel
import com.example.pruningapp.viewmodel.TaskViewModel
import com.example.pruningapp.viewmodel.WeatherViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    taskViewModel: TaskViewModel = viewModel(),
    plantViewModel: PlantViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = viewModel()
) {
    val upcomingTasks by taskViewModel.upcomingTasks.collectAsState()
    val plants by plantViewModel.allPlants.collectAsState()
    val weather by weatherViewModel.weather.collectAsState()
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl"))

    val ownedPlants = remember(plants) { plants.filter { it.owned } }
    val ownedIds = remember(ownedPlants) { ownedPlants.map { it.id }.toSet() }
    val visibleTasks = upcomingTasks.filter { it.plantId in ownedIds }
    val activeTasksCount = visibleTasks.count { task ->
        val start = runCatching { LocalDate.parse(task.date) }.getOrNull()
        val end = runCatching { LocalDate.parse(task.endDate) }.getOrNull()
        task.status != "done" && start != null && end != null && (!today.isBefore(start) && !today.isAfter(end))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pulpit", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("stats") }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Statystyki")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ustawienia")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = today.format(dateFormatter).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Witaj w ogrodzie!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = "Twoje rośliny",
                            value = ownedPlants.size.toString(),
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        StatCard(
                            label = "Zadania dziś",
                            value = activeTasksCount.toString(),
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                }
            }

            if (weather != null) {
                item {
                    if (weather.hasWarning) {
                        WeatherWarningCard(weather)
                    } else {
                        WeatherInfoCard(weather)
                    }
                }
            }

            item {
                Text(
                    text = "Nadchodzące cięcia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (ownedIds.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Dodaj posiadane rośliny, aby zobaczyć harmonogram",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (visibleTasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Wszystkie rośliny zadbane! Brak zadań.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(visibleTasks, key = { it.id }) { task ->
                    val plant = plants.firstOrNull { it.id == task.plantId }
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        TaskCard(
                            task = task,
                            plantName = plant?.name ?: "Nieznana",
                            onCheckedChange = { done ->
                                taskViewModel.updateTaskStatus(task, if (done) "done" else "pending")
                            },
                            onClick = { plant?.let { navController.navigate("plant_detail/${it.id}") } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherWarningCard(weather: WeatherData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${weather.temp.toInt()}°C · ${weather.description.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = weather.warningText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun WeatherInfoCard(weather: WeatherData) {
    val icon = when {
        weather.conditionId in 600..622 -> Icons.Default.AcUnit
        weather.conditionId in 200..531 -> Icons.Default.Grain
        weather.conditionId in 801..804 -> Icons.Default.Cloud
        else                            -> Icons.Default.WbSunny
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "${weather.temp.toInt()}°C · ${weather.description.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = weather.city,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    plantName: String,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val startDate = runCatching { LocalDate.parse(task.date) }.getOrNull()
    val endDate = runCatching { LocalDate.parse(task.endDate) }.getOrNull()
    val isActive = startDate != null && endDate != null &&
            !today.isBefore(startDate) && !today.isAfter(endDate)
    val isDone = task.status == "done"
    val displayFmt = DateTimeFormatter.ofPattern("dd.MM")

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDone -> MaterialTheme.colorScheme.surfaceVariant
                isActive -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plantName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val range = if (startDate != null && endDate != null)
                    "${startDate.format(displayFmt)} – ${endDate.format(displayFmt)}"
                else task.date
                Text(
                    text = "${task.type.replaceFirstChar { it.uppercase() }} • $range",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isActive && !isDone) {
                    Text(
                        text = "Aktywne teraz",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                } else if (!isActive && startDate != null) {
                    Text(
                        text = "Od ${startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Checkbox(
                checked = isDone,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
