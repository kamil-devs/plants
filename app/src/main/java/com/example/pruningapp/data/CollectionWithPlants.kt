package com.example.pruningapp.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CollectionWithPlants(
    @Embedded val collection: Collection,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlantCollectionCrossRef::class,
            parentColumn = "collectionId",
            entityColumn = "plantId"
        )
    )
    val plants: List<Plant>
)
