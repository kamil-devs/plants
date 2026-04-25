package com.example.pruningapp.repository

import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val db: AppDatabase) {

    fun getTasksByDate(date: String): Flow<List<Task>> = db.taskDao().getTasksByDate(date)

    fun getUpcomingTasks(fromDate: String, limit: Int = 20): Flow<List<Task>> =
        db.taskDao().getUpcomingTasks(fromDate, limit)

    suspend fun getPendingTasksForDate(date: String): List<Task> =
        db.taskDao().getPendingTasksForDate(date)

    suspend fun insertTask(task: Task) = db.taskDao().insertTask(task)

    suspend fun updateTask(task: Task) = db.taskDao().updateTask(task)

    fun getAllTasks(): Flow<List<Task>> = db.taskDao().getAllTasks()

    fun getTasksInRange(startDate: String, endDate: String): Flow<List<Task>> =
        db.taskDao().getTasksInRange(startDate, endDate)
}
