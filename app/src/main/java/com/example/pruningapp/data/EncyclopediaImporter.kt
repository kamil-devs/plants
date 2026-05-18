package com.example.pruningapp.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Jednorazowy importer katalogu gatunków z encyclopedia_species.json do Room.
// Wywoływany w App.onCreate gdy tabela encyclopedia_species jest pusta —
// zarówno przy świeżej instalacji, jak i po migracji do wersji 13.
internal class EncyclopediaImporter(
    private val context: Context,
    private val db: AppDatabase
) {
    private data class SpeciesJson(
        val perenualId: Int,
        val polishName: String,
        val latinName: String,
        val category: String
    )

    suspend fun import() {
        val json = context.assets.open("encyclopedia_species.json")
            .bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<SpeciesJson>>() {}.type
        val entries: List<SpeciesJson> = Gson().fromJson(json, type)

        val entities = entries.map {
            EncyclopediaSpecies(
                perenualId = it.perenualId,
                polishName = it.polishName,
                latinName = it.latinName,
                category = it.category
            )
        }
        db.encyclopediaSpeciesDao().insertAll(entities)
    }
}
