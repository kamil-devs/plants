package com.example.pruningapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EncyclopediaSpeciesDao {

    @Query("SELECT * FROM encyclopedia_species ORDER BY polishName ASC")
    fun getAllFlow(): Flow<List<EncyclopediaSpecies>>

    @Query("SELECT * FROM encyclopedia_species WHERE perenualId = :id LIMIT 1")
    suspend fun getById(id: Int): EncyclopediaSpecies?

    @Query("SELECT * FROM encyclopedia_species WHERE polishName = :name COLLATE NOCASE LIMIT 1")
    suspend fun getByPolishName(name: String): EncyclopediaSpecies?

    @Query("SELECT * FROM encyclopedia_species ORDER BY polishName ASC")
    suspend fun getAll(): List<EncyclopediaSpecies>

    @Query("SELECT COUNT(*) FROM encyclopedia_species")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(species: List<EncyclopediaSpecies>)
}
