package com.example.pruningapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantId: Long,
    val date: String,       // yyyy-MM-dd — start okna cięcia
    val endDate: String,    // yyyy-MM-dd — koniec okna cięcia
    val type: String,
    val status: String = "pending"
)
