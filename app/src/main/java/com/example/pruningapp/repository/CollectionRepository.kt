package com.example.pruningapp.repository

import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.Collection
import com.example.pruningapp.data.CollectionWithPlants
import com.example.pruningapp.data.PlantCollectionCrossRef
import kotlinx.coroutines.flow.Flow

class CollectionRepository(private val db: AppDatabase) {

    fun getAllCollectionsWithPlants(): Flow<List<CollectionWithPlants>> =
        db.collectionDao().getAllCollectionsWithPlants()

    fun getCollectionWithPlantsFlow(id: Long): Flow<CollectionWithPlants?> =
        db.collectionDao().getCollectionWithPlantsFlow(id)

    fun getPlantIdsForCollection(collectionId: Long): Flow<List<Long>> =
        db.collectionDao().getPlantIdsForCollection(collectionId)

    suspend fun insertCollection(collection: Collection): Long =
        db.collectionDao().insertCollection(collection)

    suspend fun updateCollection(collection: Collection) =
        db.collectionDao().updateCollection(collection)

    suspend fun deleteCollection(collection: Collection) {
        db.collectionDao().removeAllPlantsFromCollection(collection.id)
        db.collectionDao().deleteCollection(collection)
    }

    suspend fun addPlantToCollection(plantId: Long, collectionId: Long) =
        db.collectionDao().addPlantToCollection(PlantCollectionCrossRef(plantId, collectionId))

    suspend fun removePlantFromCollection(plantId: Long, collectionId: Long) =
        db.collectionDao().removePlantFromCollection(PlantCollectionCrossRef(plantId, collectionId))
}
