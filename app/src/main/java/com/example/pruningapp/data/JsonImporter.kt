package com.example.pruningapp.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class JsonImporter(
    private val context: Context,
    private val db: AppDatabase
) {
    private val gson = Gson()
    private val fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val windowFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun import() {
        val json = context.assets.open("plants.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<PlantJson>>() {}.type
        val plants: List<PlantJson> = gson.fromJson(json, type)

        plants.forEach { plantJson ->
            val plant = Plant(
                name = plantJson.name,
                type = plantJson.type,
                instructions = gson.toJson(plantJson.instructions),
                harvestStart = plantJson.harvest?.start,
                harvestEnd = plantJson.harvest?.end,
                harvestAppearance = plantJson.harvest?.appearance
            )
            val plantId = db.plantDao().insertPlant(plant)

            plantJson.pruning.forEach { (pruningType, window) ->
                val rule = PruningRule(
                    plantId = plantId,
                    startMonthDay = window.start,
                    endMonthDay = window.end,
                    type = pruningType
                )
                db.plantDao().insertPruningRule(rule)
                generateTask(plantId, window.start, window.end, pruningType)
            }
        }
    }

    // Generuje JEDNO zadanie na okno cięcia na rok — nie per dzień.
    // Okno to zakres, w którym można ciąć, nie wymóg codziennego cięcia.
    private suspend fun generateTask(
        plantId: Long,
        start: String,   // MM-dd, np. "04-01"
        end: String,     // MM-dd, np. "09-01"
        type: String
    ) {
        val today = LocalDate.now()

        for (yearOffset in 0..1) {
            val year = today.year + yearOffset
            val startDate = LocalDate.parse("$year-$start", windowFormatter)
            val endDate = LocalDate.parse("$year-$end", windowFormatter)

            // Pomiń okna, które całkowicie minęły
            if (endDate.isBefore(today)) continue

            val count = db.taskDao().countTaskForPlantAndDate(
                plantId, startDate.format(fullFormatter), type
            )
            if (count == 0) {
                db.taskDao().insertTask(
                    Task(
                        plantId = plantId,
                        date = startDate.format(fullFormatter),
                        endDate = endDate.format(fullFormatter),
                        type = type,
                        status = "pending"
                    )
                )
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

    data class PruningWindow(
        val start: String,
        val end: String
    )

    data class HarvestInfo(
        val start: String,
        val end: String,
        val appearance: String
    )
}
