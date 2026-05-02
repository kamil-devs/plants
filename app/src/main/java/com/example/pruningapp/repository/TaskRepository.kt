package com.example.pruningapp.repository

import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val db: AppDatabase) {

    fun getTasksContainingDate(date: String): Flow<List<Task>> =
        db.taskDao().getTasksContainingDate(date)

    fun getUpcomingTasks(today: String): Flow<List<Task>> =
        db.taskDao().getUpcomingTasks(today)

    suspend fun getActiveTasksForToday(today: String): List<Task> =
        db.taskDao().getActiveTasksForToday(today)

    suspend fun getTasksStartingOn(date: String): List<Task> =
        db.taskDao().getTasksStartingOn(date)

    suspend fun insertTask(task: Task) = db.taskDao().insertTask(task)

    suspend fun updateTask(task: Task) = db.taskDao().updateTask(task)

    fun getAllTasks(): Flow<List<Task>> = db.taskDao().getAllTasks()

    fun getTasksInRange(startDate: String, endDate: String): Flow<List<Task>> =
        db.taskDao().getTasksInRange(startDate, endDate)
}
