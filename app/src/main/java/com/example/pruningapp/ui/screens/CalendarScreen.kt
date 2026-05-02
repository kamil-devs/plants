package com.example.pruningapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private enum class CalendarViewMode { Monthly, Weekly }

private fun LocalDate.startOfWeek(): LocalDate = minusDays((dayOfWeek.value - 1).toLong())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    taskViewModel: TaskViewModel = viewModel(),
    plantViewModel: PlantViewModel = viewModel()
) {
    var viewMode by remember { mutableStateOf(CalendarViewMode.Monthly) }
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    var currentWeekStart by remember { mutableStateOf(LocalDate.now().startOfWeek()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    var filterMyOnly by remember { mutableStateOf(false) }
    var filterFruitOnly by remember { mutableStateOf(false) }
    var filterActiveOnly by remember { mutableStateOf(false) }

    val allTasks by taskViewModel.allTasks.collectAsState()
    val plants by plantViewModel.allPlants.collectAsState()
    val plantMap = remember(plants) { plants.associateBy { it.id } }
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val filteredTasks = remember(allTasks, plantMap, filterMyOnly, filterFruitOnly, filterActiveOnly) {
        allTasks.filter { task ->
            val plant = plantMap[task.plantId]
            (!filterMyOnly || plant?.owned == true) &&
            (!filterFruitOnly || plant?.type == "owocowa") &&
            (!filterActiveOnly || task.status != "done")
        }
    }

    val datesInWindows: Set<String> = remember(filteredTasks) {
        buildSet {
            filteredTasks.forEach { task ->
                val start = runCatching { LocalDate.parse(task.date) }.getOrNull() ?: return@forEach
                val end = runCatching { LocalDate.parse(task.endDate) }.getOrNull() ?: return@forEach
                var cur = start
                while (!cur.isAfter(end)) { add(cur.format(fmt)); cur = cur.plusDays(1) }
            }
        }
    }

    val selectedStr = selectedDate.format(fmt)
    val tasksForSelected = filteredTasks.filter { task ->
        task.date <= selectedStr && task.endDate >= selectedStr
    }

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
            TabRow(selectedTabIndex = viewMode.ordinal) {
                Tab(
                    selected = viewMode == CalendarViewMode.Monthly,
                    onClick = { viewMode = CalendarViewMode.Monthly },
                    text = { Text("Miesiecznie") }
                )
                Tab(
                    selected = viewMode == CalendarViewMode.Weekly,
                    onClick = { viewMode = CalendarViewMode.Weekly },
                    text = { Text("Tygodniowo") }
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterMyOnly,
                        onClick = { filterMyOnly = !filterMyOnly },
                        label = { Text("Moje rosliny") }
                    )
                }
                item {
                    FilterChip(
                        selected = filterFruitOnly,
                        onClick = { filterFruitOnly = !filterFruitOnly },
                        label = { Text("Owocowe") }
                    )
                }
                item {
                    FilterChip(
                        selected = filterActiveOnly,
                        onClick = { filterActiveOnly = !filterActiveOnly },
                        label = { Text("Tylko aktywne") }
                    )
                }
            }

            HorizontalDivider()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (viewMode) {
                    CalendarViewMode.Monthly -> MonthlyView(
                        displayedMonth = displayedMonth,
                        selectedDate = selectedDate,
                        datesInWindows = datesInWindows,
                        tasksForSelected = tasksForSelected,
                        plantMap = plantMap,
                        fmt = fmt,
                        onPrevMonth = { displayedMonth = displayedMonth.minusMonths(1) },
                        onNextMonth = { displayedMonth = displayedMonth.plusMonths(1) },
                        onDateSelected = { selectedDate = it },
                        taskViewModel = taskViewModel,
                        navController = navController
                    )
                    CalendarViewMode.Weekly -> WeeklyView(
                        currentWeekStart = currentWeekStart,
                        filteredTasks = filteredTasks,
                        plantMap = plantMap,
                        fmt = fmt,
                        onPrevWeek = { currentWeekStart = currentWeekStart.minusWeeks(1) },
                        onNextWeek = { currentWeekStart = currentWeekStart.plusWeeks(1) },
                        taskViewModel = taskViewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyView(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    datesInWindows: Set<String>,
    tasksForSelected: List<com.example.pruningapp.data.Task>,
    plantMap: Map<Long, com.example.pruningapp.data.Plant>,
    fmt: DateTimeFormatter,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    taskViewModel: TaskViewModel,
    navController: NavController
) {
    val displayFmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("pl"))

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.Default.ChevronLeft, "Poprzedni miesiac")
            }
            Text(
                text = "${displayedMonth.month.getDisplayName(TextStyle.FULL, Locale("pl")).replaceFirstChar { it.uppercase() }} ${displayedMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, "Nastepny miesiac")
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            listOf("Pn", "Wt", "Sr", "Cz", "Pt", "So", "Nd").forEach { day ->
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
            datesInWindows = datesInWindows,
            onDateSelected = onDateSelected
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Okna cecia: ${selectedDate.format(displayFmt)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (tasksForSelected.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Brak aktywnych okien cecia", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasksForSelected, key = { it.id }) { task ->
                    val plant = plantMap[task.plantId]
                    TaskCard(
                        task = task,
                        plantName = plant?.name ?: "Nieznana",
                        onCheckedChange = { done ->
                            taskViewModel.updateTaskStatus(task, if (done) "done" else "pending")
                        },
                        onClick = {
                            plant?.let { navController.navigate("plant_detail/${it.id}") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyView(
    currentWeekStart: LocalDate,
    filteredTasks: List<com.example.pruningapp.data.Task>,
    plantMap: Map<Long, com.example.pruningapp.data.Plant>,
    fmt: DateTimeFormatter,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    taskViewModel: TaskViewModel,
    navController: NavController
) {
    val weekEnd = currentWeekStart.plusDays(6)
    val weekFmt = DateTimeFormatter.ofPattern("d MMM", Locale("pl"))
    val weekFmtYear = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("pl"))
    val weekLabel = if (currentWeekStart.year == weekEnd.year) {
        "${currentWeekStart.format(weekFmt)} - ${weekEnd.format(weekFmt)} ${weekEnd.year}"
    } else {
        "${currentWeekStart.format(weekFmtYear)} - ${weekEnd.format(weekFmtYear)}"
    }
    val dayHeaderFmt = DateTimeFormatter.ofPattern("d MMM", Locale("pl"))
    val today = LocalDate.now()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevWeek) {
                Icon(Icons.Default.ChevronLeft, "Poprzedni tydzien")
            }
            Text(weekLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = onNextWeek) {
                Icon(Icons.Default.ChevronRight, "Nastepny tydzien")
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            for (dayOffset in 0..6) {
                val date = currentWeekStart.plusDays(dayOffset.toLong())
                val dateStr = date.format(fmt)
                val isToday = date == today
                val dayTasks = filteredTasks.filter { task ->
                    task.date <= dateStr && task.endDate >= dateStr
                }
                val dayName = date.dayOfWeek
                    .getDisplayName(TextStyle.FULL, Locale("pl"))
                    .replaceFirstChar { it.uppercase() }

                item(key = "header_$dayOffset") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isToday) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isToday) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = date.format(dayHeaderFmt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (dayTasks.isEmpty()) {
                    item(key = "empty_$dayOffset") {
                        Text(
                            text = "Brak zadan",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(dayTasks, key = { "task_${dayOffset}_${it.id}" }) { task ->
                        val plant = plantMap[task.plantId]
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            TaskCard(
                                task = task,
                                plantName = plant?.name ?: "Nieznana",
                                onCheckedChange = { done ->
                                    taskViewModel.updateTaskStatus(task, if (done) "done" else "pending")
                                },
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
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    datesInWindows: Set<String>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOffset = (yearMonth.atDay(1).dayOfWeek.value - 1) % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()
    val rows = (firstDayOffset + daysInMonth + 6) / 7

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val dayNumber = row * 7 + col - firstDayOffset + 1
                    if (dayNumber in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayNumber)
                        val dateStr = date.format(fmt)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val inWindow = dateStr in datesInWindows

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        inWindow -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    },
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNumber.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    inWindow -> MaterialTheme.colorScheme.onSecondaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (isToday || isSelected) FontWeight.Bold
                                             else FontWeight.Normal
                            )
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}
