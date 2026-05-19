package com.example.pruningapp.data

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "plant_collection_cross_ref",
    primaryKeys = ["plantId", "collectionId"],
    indices = [Index(value = ["collectionId"])]
)
data class PlantCollectionCrossRef(
    val plantId: Long,
    val collectionId: Long
)
