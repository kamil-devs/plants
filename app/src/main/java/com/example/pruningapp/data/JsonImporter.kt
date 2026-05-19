package com.example.pruningapp.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// perenualId pochodzi teraz z encyclopediaSpeciesDao (SSOT) zamiast statycznego PlantDatabase.
// Wymaga: encyclopedia_species musi być zasilona przed wywołaniem import().
class JsonImporter(
    private val context: Context,
    private val db: AppDatabase
) {
    private val gson = Gson()

    suspend fun import() {
        val json = context.assets.open("plants.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<PlantJson>>() {}.type
        val plants: List<PlantJson> = gson.fromJson(json, type)

        plants.forEach { plantJson ->
            val perenualId = db.encyclopediaSpeciesDao().getByPolishName(plantJson.name)?.perenualId
            val plant = Plant(
                name = plantJson.name,
                type = plantJson.type,
                instructions = gson.toJson(plantJson.instructions),
                harvestStart = plantJson.harvest?.start,
                harvestEnd = plantJson.harvest?.end,
                harvestAppearance = plantJson.harvest?.appearance,
                perenualId = perenualId
            )
            val plantId = db.plantDao().insertPlant(plant)

            plantJson.pruning.forEach { (pruningType, window) ->
                db.plantDao().insertPruningRule(
                    PruningRule(
                        plantId = plantId,
                        startMonthDay = window.start,
                        endMonthDay = window.end,
                        type = pruningType
                    )
                )
                db.generateTasksForRule(plantId, window.start, window.end, pruningType)
            }
        }
    }

    data class PlantJson(
        val name: String,
        val type: String,
        val pruning: Map<String, PruningWindow>,
        val instructions: List<String>,
        val harvest: HarvestInfo? = null
    )

    data class PruningWindow(val start: String, val end: String)

    data class HarvestInfo(val start: String, val end: String, val appearance: String)
}
