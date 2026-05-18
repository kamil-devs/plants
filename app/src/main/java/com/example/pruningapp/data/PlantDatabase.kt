package com.example.pruningapp.data

object PlantDatabase {

    val plants: List<PerenualPlant> = listOf(
        // Owocowe
        PerenualPlant(3694,  "Agrest",         "Ribes uva-crispa",          "Owocowe"),
        PerenualPlant(7192,  "Malina",          "Rubus idaeus",              "Owocowe"),
        PerenualPlant(2855,  "Truskawka",       "Fragaria × ananassa",       "Owocowe"),
        PerenualPlant(8780,  "Borówka wysoka",  "Vaccinium corymbosum",      "Owocowe"),
        PerenualPlant(4892,  "Jabłoń",          "Malus domestica",           "Owocowe"),
        // Warzywa i zioła
        PerenualPlant(8131,  "Pomidor",         "Solanum lycopersicum",      "Warzywa"),
        PerenualPlant(5638,  "Bazylia",         "Ocimum basilicum",          "Zioła"),
        PerenualPlant(3919,  "Lawenda",         "Lavandula angustifolia",    "Zioła"),
        PerenualPlant(8375,  "Tymianek",        "Thymus vulgaris",           "Zioła"),
        // Ozdobne
        PerenualPlant(7342,  "Róża",            "Rosa",                      "Ozdobne"),
        PerenualPlant(2955,  "Hortensja",       "Hydrangea macrophylla",     "Ozdobne"),
        PerenualPlant(6341,  "Pelargonia",      "Pelargonium × hortorum",    "Ozdobne"),
        PerenualPlant(2837,  "Słonecznik",      "Helianthus annuus",         "Ozdobne"),
        // Doniczkowe
        PerenualPlant(5243,  "Monstera",        "Monstera deliciosa",        "Doniczkowe"),
        PerenualPlant(2094,  "Bluszcz",         "Hedera helix",              "Doniczkowe"),
        PerenualPlant(151,   "Aloes",           "Aloe vera",                 "Doniczkowe"),
    )

    fun findById(id: Int): PerenualPlant? = plants.find { it.perenualId == id }
}
