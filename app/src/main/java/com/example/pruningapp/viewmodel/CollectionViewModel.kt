package com.example.pruningapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruningapp.App
import com.example.pruningapp.data.Collection
import com.example.pruningapp.data.CollectionWithPlants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as App).collectionRepository

    val allCollectionsWithPlants: StateFlow<List<CollectionWithPlants>> =
        repo.getAllCollectionsWithPlants()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getCollectionWithPlantsFlow(id: Long): Flow<CollectionWithPlants?> =
        repo.getCollectionWithPlantsFlow(id)

    fun getPlantIdsForCollection(collectionId: Long): Flow<List<Long>> =
        repo.getPlantIdsForCollection(collectionId)

    fun saveCollection(id: Long?, name: String, description: String, type: String) {
        viewModelScope.launch {
            if (id == null) {
                repo.insertCollection(Collection(name = name, description = description, type = type))
            } else {
                repo.updateCollection(Collection(id = id, name = name, description = description, type = type))
            }
        }
    }

    fun deleteCollection(collection: Collection) {
        viewModelScope.launch { repo.deleteCollection(collection) }
    }

    fun togglePlantInCollection(plantId: Long, collectionId: Long, isInCollection: Boolean) {
        viewModelScope.launch {
            if (isInCollection) {
                repo.removePlantFromCollection(plantId, collectionId)
            } else {
                repo.addPlantToCollection(plantId, collectionId)
            }
        }
    }
}
