package com.example.pruningapp.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Encja domenowa katalogu gatunków (źródło: encyclopedia_species.json).
// Indeksy na kolumnach wyszukiwania eliminują pełne skany tabeli przy mapowaniu
// polskiej nazwy na łacińską podczas synchronizacji z Wikipedią.
@Entity(
    tableName = "encyclopedia_species",
    indices = [
        Index(value = ["polishName"]),
        Index(value = ["latinName"])
    ]
)
data class EncyclopediaSpecies(
    @PrimaryKey val perenualId: Int,
    val polishName: String,
    val latinName: String,
    val category: String
)
