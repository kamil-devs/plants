package com.example.pruningapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruningapp.App
import com.example.pruningapp.data.Plant
import com.example.pruningapp.data.PruningRule
import com.example.pruningapp.repository.PlantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val plantRepository = PlantRepository((application as App).database)

    val allPlants: StateFlow<List<Plant>> = plantRepository.getAllPlants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getPlantById(id: Long): Plant? = plantRepository.getPlantById(id)

    fun getPlantByIdFlow(id: Long): Flow<Plant?> = plantRepository.getPlantByIdFlow(id)

    suspend fun getPruningRules(plantId: Long): List<PruningRule> =
        plantRepository.getPruningRules(plantId)

    fun getPruningRulesFlow(plantId: Long): Flow<List<PruningRule>> =
        plantRepository.getPruningRulesFlow(plantId)

    fun toggleOwned(plant: Plant) {
        viewModelScope.launch {
            plantRepository.setOwned(plant.id, !plant.owned)
        }
    }

    fun togglePinned(plant: Plant) {
        viewModelScope.launch {
            plantRepository.setPinned(plant.id, !plant.pinned)
        }
    }

    fun addPlant(name: String, type: String) {
        viewModelScope.launch {
            plantRepository.insertPlant(Plant(name = name, type = type, instructions = "[]", isUserAdded = true, owned = true))
        }
    }

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            plantRepository.deletePlant(plant)
        }
    }

    fun saveEditedPlantById(
        plantId: Long,
        newName: String,
        newType: String,
        instructionsJson: String,
        rules: List<Triple<String, String, String>>
    ) {
        viewModelScope.launch {
            val plant = plantRepository.getPlantById(plantId) ?: return@launch
            val updated = plant.copy(
                name = newName.trim(),
                type = newType.trim().ifBlank { "ogólna" },
                instructions = instructionsJson,
                owned = if (rules.isNotEmpty()) true else plant.owned
            )
            plantRepository.updatePlant(updated)
            plantRepository.replacePruningRulesAndTasks(plantId, rules)
        }
    }
}
