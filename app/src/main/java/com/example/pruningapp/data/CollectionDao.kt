package com.example.pruningapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Transaction
    @Query("SELECT * FROM collections ORDER BY name ASC")
    fun getAllCollectionsWithPlants(): Flow<List<CollectionWithPlants>>

    @Transaction
    @Query("SELECT * FROM collections WHERE id = :id")
    fun getCollectionWithPlantsFlow(id: Long): Flow<CollectionWithPlants?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: Collection): Long

    @Update
    suspend fun updateCollection(collection: Collection)

    @Delete
    suspend fun deleteCollection(collection: Collection)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPlantToCollection(crossRef: PlantCollectionCrossRef)

    @Delete
    suspend fun removePlantFromCollection(crossRef: PlantCollectionCrossRef)

    @Query("SELECT plantId FROM plant_collection_cross_ref WHERE collectionId = :collectionId")
    fun getPlantIdsForCollection(collectionId: Long): Flow<List<Long>>

    @Query("DELETE FROM plant_collection_cross_ref WHERE collectionId = :collectionId")
    suspend fun removeAllPlantsFromCollection(collectionId: Long)
}
