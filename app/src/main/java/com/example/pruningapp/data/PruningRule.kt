package com.example.pruningapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pruning_rules")
data class PruningRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantId: Long,
    val startMonthDay: String,
    val endMonthDay: String,
    val type: String
)
