package com.example.pruningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pruningapp.R
import com.example.pruningapp.data.Task
import com.example.pruningapp.navigation.Screen
import com.example.pruningapp.data.TaskStatus
import com.example.pruningapp.data.WeatherData
import com.example.pruningapp.data.isDone
import com.example.pruningapp.viewmodel.CollectionViewModel
import com.example.pruningapp.viewmodel.PlantViewModel
import com.example.pruningapp.viewmodel.TaskViewModel
import com.example.pruningapp.viewmodel.WeatherViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    navController: NavController,
    taskViewModel: TaskViewModel = viewModel(),
    plantViewModel: PlantViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = viewModel(),
    collectionViewModel: CollectionViewModel = viewModel()
) {
    val upcomingTasks by taskViewModel.upcomingTasks.collectAsState()
    val plants by plantViewModel.allPlants.collectAsState()
    val weather by weatherViewModel.weather.collectAsState()
    val collections by collectionViewModel.allCollectionsWithPlants.collectAsState()
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl"))

    val ownedPlants = remember(plants) { plants.filter { it.owned } }
    val ownedIds = remember(ownedPlants) { ownedPlants.map { it.id }.toSet() }
    val visibleTasks = upcomingTasks.filter { it.plantId in ownedIds }
    val activeTasksCount = visibleTasks.count { task ->
        val start = runCatching { LocalDate.parse(task.date) }.getOrNull()
        val end = runCatching { LocalDate.parse(task.endDate) }.getOrNull()
        !task.isDone && start != null && end != null && !today.isBefore(start) && !today.isAfter(end)
    }

    val greeting = remember {
        when (LocalTime.now().hour) {
            in 5..11 -> R.string.greeting_morning
            in 12..17 -> R.string.greeting_afternoon
            in 18..21 -> R.string.greeting_evening
            else -> R.string.greeting_night
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero header card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 28.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = today.format(dateFormatter).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(greeting),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            HeroStatBadge(
                                value = ownedPlants.size.toString(),
                                label = stringResource(R.string.dashboard_plants_label)
                            )
                            HeroStatBadge(
                                value = activeTasksCount.toString(),
                                label = stringResource(R.string.dashboard_tasks_today),
                                highlight = activeTasksCount > 0
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        IconButton(onClick = { navController.navigate(Screen.Stats.route) }) {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = stringResource(R.string.cd_stats),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(R.string.cd_settings),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        weather?.let { current ->
            item {
                if (current.hasWarning) WeatherWarningCard(current)
                else WeatherInfoCard(current)
            }
        }

        item {
            Card(
                onClick = { navController.navigate(Screen.Collections.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                "Moje kolekcje",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "${collections.size} ${if (collections.size == 1) "kolekcja" else "kolekcji"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.dashboard_upcoming_pruning),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        when {
            ownedIds.isEmpty() -> item {
                EmptyStateCard(
                    text = stringResource(R.string.dashboard_add_plants_hint),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            visibleTasks.isEmpty() -> item {
                EmptyStateCard(
                    text = stringResource(R.string.dashboard_all_done),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            else -> items(visibleTasks, key = { it.id }) { task ->
                val plant = plants.firstOrNull { it.id == task.plantId }
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    TaskCard(
                        task = task,
                        plantName = plant?.name ?: "Nieznana",
                        onCheckedChange = { done ->
                            taskViewModel.updateTaskStatus(
                                task,
                                if (done) TaskStatus.DONE else TaskStatus.PENDING
                            )
                        },
                        onClick = { plant?.let { navController.navigate(Screen.PlantDetail.route(it.id)) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStatBadge(value: String, label: String, highlight: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (highlight) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (highlight) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun EmptyStateCard(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeatherWarningCard(weather: WeatherData) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
        else -> Icons.Default.WbSunny
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
    val isDone = task.isDone
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
                    text = "${task.type.replaceFirstChar { it.uppercase() }} · $range",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isActive && !isDone) {
                    Text(
                        text = stringResource(R.string.dashboard_active_now),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                } else if (!isActive && startDate != null) {
                    Text(
                        text = stringResource(
                            R.string.dashboard_from_date,
                            startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Checkbox(checked = isDone, onCheckedChange = onCheckedChange)
        }
    }
}
