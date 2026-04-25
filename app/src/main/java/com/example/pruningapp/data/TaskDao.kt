package com.example.pruningapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY type ASC")
    fun getTasksByDate(date: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date >= :fromDate ORDER BY date ASC LIMIT :limit")
    fun getUpcomingTasks(fromDate: String, limit: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date = :date AND status = 'pending'")
    suspend fun getPendingTasksForDate(date: String): List<Task>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY date ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getTasksInRange(startDate: String, endDate: String): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks WHERE plantId = :plantId AND date = :date AND type = :type")
    suspend fun countTaskForPlantAndDate(plantId: Long, date: String, type: String): Int
}
