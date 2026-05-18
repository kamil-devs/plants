package com.example.pruningapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pruning_guide_cache")
data class PruningGuideCache(
    @PrimaryKey val perenualId: Int,
    val commonName: String,
    val pruningMonthsJson: String,
    val frequency: String?,
    val maintenanceLevel: String?,
    val description: String?,
    val imageUrl: String?,
    val fetchedAt: Long
)
