package com.example.pruningapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PruningGuideCacheDao {

    @Query("SELECT * FROM pruning_guide_cache WHERE perenualId = :id LIMIT 1")
    suspend fun getById(id: Int): PruningGuideCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: PruningGuideCache)
}
