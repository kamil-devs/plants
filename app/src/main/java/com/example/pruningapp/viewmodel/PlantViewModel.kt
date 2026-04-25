package com.example.pruningapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruningapp.App
import com.example.pruningapp.data.Plant
import com.example.pruningapp.data.PruningRule
import com.example.pruningapp.repository.PlantRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val plantRepository = PlantRepository((application as App).database)

    val allPlants: StateFlow<List<Plant>> = plantRepository.getAllPlants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getPlantById(id: Long): Plant? = plantRepository.getPlantById(id)

    suspend fun getPruningRules(plantId: Long): List<PruningRule> =
        plantRepository.getPruningRules(plantId)

    fun toggleOwned(plant: Plant) {
        viewModelScope.launch {
            plantRepository.setOwned(plant.id, !plant.owned)
        }
    }

    fun addPlant(name: String, type: String) {
        viewModelScope.launch {
            plantRepository.insertPlant(Plant(name = name, type = type, instructions = "[]"))
        }
    }

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            plantRepository.deletePlant(plant)
        }
    }
}
