package com.example.pruningapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruningapp.App
import com.example.pruningapp.data.Task
import com.example.pruningapp.data.TaskStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepository = (application as App).taskRepository
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val today: String = LocalDate.now().format(formatter)
    private val windowCutoff: String = LocalDate.now().plusDays(90).format(formatter)

    val upcomingTasks: StateFlow<List<Task>> =
        taskRepository.getUpcomingTasksInWindow(today, windowCutoff)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateTaskStatus(task: Task, status: TaskStatus) {
        viewModelScope.launch {
            taskRepository.updateTask(task.copy(status = status.value))
        }
    }

}
