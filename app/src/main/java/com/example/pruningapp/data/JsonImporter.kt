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

    suspend fun import() {
        val json = context.assets.open("plants.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<PlantJson>>() {}.type
        val plants: List<PlantJson> = gson.fromJson(json, type)

        plants.forEach { plantJson ->
            val plant = Plant(
                name = plantJson.name,
                type = plantJson.type,
                instructions = gson.toJson(plantJson.instructions)
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
                generateTasksForWindow(plantId, window.start, window.end, pruningType)
            }
        }
    }

    private suspend fun generateTasksForWindow(
        plantId: Long,
        start: String,
        end: String,
        type: String
    ) {
        val today = LocalDate.now()
        val windowFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        for (yearOffset in 0..1) {
            val year = today.year + yearOffset
            val startDate = LocalDate.parse("$year-$start", windowFormatter)
            val endDate = LocalDate.parse("$year-$end", windowFormatter)

            if (endDate.isBefore(startDate)) continue

            var current = startDate
            while (!current.isAfter(endDate)) {
                if (!current.isBefore(today)) {
                    val dateStr = current.format(fullFormatter)
                    val count = db.taskDao().countTaskForPlantAndDate(plantId, dateStr, type)
                    if (count == 0) {
                        db.taskDao().insertTask(
                            Task(
                                plantId = plantId,
                                date = dateStr,
                                type = type,
                                status = "pending"
                            )
                        )
                    }
                }
                current = current.plusDays(1)
            }
        }
    }

    data class PlantJson(
        val name: String,
        val type: String,
        val pruning: Map<String, PruningWindow>,
        val instructions: List<String>
    )

    data class PruningWindow(
        val start: String,
        val end: String
    )
}
