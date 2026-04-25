package com.example.pruningapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruningapp.App
import com.example.pruningapp.data.Task
import com.example.pruningapp.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepository = TaskRepository((application as App).database)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val today: String = LocalDate.now().format(formatter)

    // Aktywne + przyszłe okna (endDate >= dziś)
    val upcomingTasks: StateFlow<List<Task>> = taskRepository.getUpcomingTasks(today, 20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Zadania, których okno zawiera daną datę
    fun getTasksForDate(date: String): Flow<List<Task>> =
        taskRepository.getTasksContainingDate(date)

    fun updateTaskStatus(task: Task, status: String) {
        viewModelScope.launch {
            taskRepository.updateTask(task.copy(status = status))
        }
    }
}
