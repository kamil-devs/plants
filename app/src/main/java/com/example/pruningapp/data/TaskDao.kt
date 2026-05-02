package com.example.pruningapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Zadania, których okno zawiera podaną datę (date <= target <= endDate)
    @Query("SELECT * FROM tasks WHERE date <= :date AND endDate >= :date ORDER BY date ASC")
    fun getTasksContainingDate(date: String): Flow<List<Task>>

    // Aktywne lub przyszłe zadania (okno nie skończyło się przed today)
    @Query("SELECT * FROM tasks WHERE endDate >= :today ORDER BY date ASC LIMIT :limit")
    fun getUpcomingTasks(today: String, limit: Int): Flow<List<Task>>

    // Aktywne zadania na powiadomienia (okno otwarte dziś, status pending)
    @Query("SELECT * FROM tasks WHERE date <= :today AND endDate >= :today AND status = 'pending'")
    suspend fun getActiveTasksForToday(today: String): List<Task>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY date ASC")
    fun getAllTasks(): Flow<List<Task>>

    // Zadania zaczynające się dokładnie w podanym dniu (start okna == date)
    @Query("SELECT * FROM tasks WHERE date = :date AND status = 'pending'")
    suspend fun getTasksStartingOn(date: String): List<Task>

    @Query("SELECT COUNT(*) FROM tasks WHERE plantId = :plantId AND date = :date AND type = :type")
    suspend fun countTaskForPlantAndDate(plantId: Long, date: String, type: String): Int

    @Query("DELETE FROM tasks WHERE plantId = :plantId")
    suspend fun deleteTasksForPlant(plantId: Long)
}
