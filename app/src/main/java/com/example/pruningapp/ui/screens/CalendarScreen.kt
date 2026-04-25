package com.example.pruningapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pruningapp.viewmodel.PlantViewModel
import com.example.pruningapp.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    taskViewModel: TaskViewModel = viewModel(),
    plantViewModel: PlantViewModel = viewModel()
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val allTasks by taskViewModel.allTasks.collectAsState()
    val plants by plantViewModel.allPlants.collectAsState()

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val tasksForSelectedDate = allTasks.filter { it.date == selectedDate.format(dateFormatter) }
    val datesWithTasks = allTasks.map { it.date }.toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalendarz") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, "Poprzedni miesiąc")
                }
                Text(
                    text = "${displayedMonth.month.getDisplayName(TextStyle.FULL, Locale("pl"))
                        .replaceFirstChar { it.uppercase() }} ${displayedMonth.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, "Następny miesiąc")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            CalendarGrid(
                yearMonth = displayedMonth,
                selectedDate = selectedDate,
                datesWithTasks = datesWithTasks,
                onDateSelected = { selectedDate = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            val displayFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("pl"))
            Text(
                text = "Zadania: ${selectedDate.format(displayFormatter)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (tasksForSelectedDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Brak zadań na ten dzień",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(tasksForSelectedDate) { task ->
                        val plant = plants.firstOrNull { it.id == task.plantId }
                        TaskCard(
                            task = task,
                            plantName = plant?.name ?: "Nieznana",
                            onDone = { taskViewModel.updateTaskStatus(task, "done") },
                            onClick = {
                                plant?.let { navController.navigate("plant_detail/${it.id}") }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    datesWithTasks: Set<String>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOffset = (yearMonth.atDay(1).dayOfWeek.value - 1) % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()
    val totalCells = firstDayOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val dayNumber = row * 7 + col - firstDayOffset + 1
                    if (dayNumber in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayNumber)
                        val dateStr = date.format(formatter)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val hasTasks = dateStr in datesWithTasks

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    },
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold
                                    else FontWeight.Normal
                                )
                                if (hasTasks) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.primary,
                                                shape = MaterialTheme.shapes.extraSmall
                                            )
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}
