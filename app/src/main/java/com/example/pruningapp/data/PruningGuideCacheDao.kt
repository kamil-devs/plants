package com.example.pruningapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PruningGuideCacheDao {

    @Query("SELECT * FROM pruning_guide_cache WHERE perenualId = :id LIMIT 1")
    suspend fun getById(id: Int): PruningGuideCache?

    @Query("UPDATE pruning_guide_cache SET imageUrl = :url WHERE perenualId = :id")
    suspend fun updateImageUrl(id: Int, url: String)

    @Query("SELECT * FROM pruning_guide_cache")
    fun getAllFlow(): kotlinx.coroutines.flow.Flow<List<PruningGuideCache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: PruningGuideCache)
}
