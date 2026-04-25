package com.example.pruningapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pruningapp.data.Task
import com.example.pruningapp.viewmodel.PlantViewModel
import com.example.pruningapp.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    taskViewModel: TaskViewModel = viewModel(),
    plantViewModel: PlantViewModel = viewModel()
) {
    val upcomingTasks by taskViewModel.upcomingTasks.collectAsState()
    val plants by plantViewModel.allPlants.collectAsState()
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("pl"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plant Pruning") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = today.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Nadchodzące zadania",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
            }

            if (upcomingTasks.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Brak zaplanowanych zadań",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(upcomingTasks.take(10)) { task ->
                    val plant = plants.firstOrNull { it.id == task.plantId }
                    TaskCard(
                        task = task,
                        plantName = plant?.name ?: "Nieznana roślina",
                        onDone = { taskViewModel.updateTaskStatus(task, "done") },
                        onClick = { plant?.let { navController.navigate("plant_detail/${it.id}") } }
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    plantName: String,
    onDone: () -> Unit,
    onClick: () -> Unit
) {
    val taskDate = runCatching { LocalDate.parse(task.date) }.getOrNull()
    val today = LocalDate.now()
    val isToday = taskDate == today
    val isPast = taskDate?.isBefore(today) == true
    val displayFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                task.status == "done" -> MaterialTheme.colorScheme.surfaceVariant
                isToday -> MaterialTheme.colorScheme.primaryContainer
                isPast -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plantName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${task.type.replaceFirstChar { it.uppercase() }} • ${taskDate?.format(displayFormatter) ?: task.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isToday) {
                    Text(
                        text = "Dziś!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (task.status == "pending") {
                TextButton(onClick = onDone) { Text("Wykonano") }
            } else {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
