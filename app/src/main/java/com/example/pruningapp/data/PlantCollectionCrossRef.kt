package com.example.pruningapp.data

import androidx.room.Entity

@Entity(
    tableName = "plant_collection_cross_ref",
    primaryKeys = ["plantId", "collectionId"]
)
data class PlantCollectionCrossRef(
    val plantId: Long,
    val collectionId: Long
)
