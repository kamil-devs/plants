package com.example.pruningapp.data

object PlantDatabase {

    val plants: List<PerenualPlant> = listOf(
        // Owocowe
        PerenualPlant(4892,  "Jabłoń",                "Malus domestica",           "Owocowe"),
        PerenualPlant(0,     "Grusza",                "Pyrus communis",            "Owocowe"),
        PerenualPlant(0,     "Śliwa",                 "Prunus domestica",          "Owocowe"),
        PerenualPlant(0,     "Wiśnia",                "Prunus cerasus",            "Owocowe"),
        PerenualPlant(0,     "Czereśnia",             "Prunus avium",              "Owocowe"),
        PerenualPlant(0,     "Brzoskwinia",           "Prunus persica",            "Owocowe"),
        PerenualPlant(0,     "Morela",                "Prunus armeniaca",          "Owocowe"),
        PerenualPlant(0,     "Porzeczka czarna",      "Ribes nigrum",              "Owocowe"),
        PerenualPlant(0,     "Porzeczka czerwona",    "Ribes rubrum",              "Owocowe"),
        PerenualPlant(3694,  "Agrest",                "Ribes uva-crispa",          "Owocowe"),
        PerenualPlant(7192,  "Malina letnia",         "Rubus idaeus",              "Owocowe"),
        PerenualPlant(7192,  "Malina powtarzająca",   "Rubus idaeus",              "Owocowe"),
        PerenualPlant(0,     "Jeżyna",                "Rubus fruticosus",          "Owocowe"),
        PerenualPlant(8780,  "Borówka wysoka",        "Vaccinium corymbosum",      "Owocowe"),
        PerenualPlant(0,     "Orzech włoski",         "Juglans regia",             "Owocowe"),
        PerenualPlant(0,     "Winorośl",              "Vitis vinifera",            "Owocowe"),
        PerenualPlant(0,     "Leszczyna",             "Corylus avellana",          "Owocowe"),
        PerenualPlant(0,     "Pigwowiec",             "Chaenomeles",               "Owocowe"),
        PerenualPlant(0,     "Dereń jadalny",         "Cornus mas",                "Owocowe"),
        PerenualPlant(0,     "Aronia",                "Aronia",                    "Owocowe"),
        PerenualPlant(2855,  "Truskawka",             "Fragaria × ananassa",       "Owocowe"),

        // Warzywa i zioła
        PerenualPlant(8131,  "Pomidor",               "Solanum lycopersicum",      "Warzywa"),
        PerenualPlant(5638,  "Bazylia",               "Ocimum basilicum",          "Zioła"),
        PerenualPlant(3919,  "Lawenda",               "Lavandula angustifolia",    "Zioła"),
        PerenualPlant(8375,  "Tymianek",              "Thymus vulgaris",           "Zioła"),

        // Ozdobne
        PerenualPlant(7342,  "Róża",                  "Rosa",                      "Ozdobne"),
        PerenualPlant(7342,  "Róża rabatowa",         "Rosa",                      "Ozdobne"),
        PerenualPlant(7342,  "Róża pnąca",            "Rosa",                      "Ozdobne"),
        PerenualPlant(2955,  "Hortensja",             "Hydrangea macrophylla",     "Ozdobne"),
        PerenualPlant(2955,  "Hortensja bukietowa",    "Hydrangea paniculata",      "Ozdobne"),
        PerenualPlant(2955,  "Hortensja ogrodowa",     "Hydrangea macrophylla",     "Ozdobne"),
        PerenualPlant(6341,  "Pelargonia",            "Pelargonium × hortorum",    "Ozdobne"),
        PerenualPlant(2837,  "Słonecznik",            "Helianthus annuus",         "Ozdobne"),
        PerenualPlant(0,     "Budleja Dawida",        "Buddleja davidii",          "Ozdobne"),
        PerenualPlant(0,     "Forsycja",              "Forsythia",                 "Ozdobne"),
        PerenualPlant(0,     "Jaśminowiec",           "Philadelphus",              "Ozdobne"),
        PerenualPlant(0,     "Tawuła japońska",       "Spiraea japonica",          "Ozdobne"),
        PerenualPlant(0,     "Tawuła wczesna",        "Spiraea × arguta",          "Ozdobne"),
        PerenualPlant(0,     "Berberys",              "Berberis",                  "Ozdobne"),
        PerenualPlant(0,     "Ligustr",               "Ligustrum",                 "Ozdobne"),
        PerenualPlant(0,     "Żywotnik (tuja)",       "Thuja",                     "Ozdobne"),
        PerenualPlant(0,     "Cis",                   "Taxus baccata",             "Ozdobne"),
        PerenualPlant(0,     "Krzewuszka",            "Weigela",                   "Ozdobne"),
        PerenualPlant(0,     "Pięciornik krzewiasty",  "Dasiphora fruticosa",       "Ozdobne"),
        PerenualPlant(0,     "Perukowiec",            "Cotinus coggygria",         "Ozdobne"),
        PerenualPlant(0,     "Trzmielina",            "Euonymus",                  "Ozdobne"),
        PerenualPlant(0,     "Jałowiec",              "Juniperus",                 "Ozdobne"),
        PerenualPlant(0,     "Bez lilak",             "Syringa vulgaris",          "Ozdobne"),
        PerenualPlant(0,     "Oczar",                 "Hamamelis",                 "Ozdobne"),

        // Ozdobne drzewa
        PerenualPlant(0,     "Klon kulisty",          "Acer platanoides",          "Ozdobne drzewa"),
        PerenualPlant(0,     "Wierzba Hakuro-Nishiki","Salix integra",             "Ozdobne drzewa"),
        PerenualPlant(0,     "Robinia akacjowa",      "Robinia pseudoacacia",      "Ozdobne drzewa"),
        PerenualPlant(0,     "Magnolia",              "Magnolia",                  "Ozdobne drzewa"),

        // Doniczkowe
        PerenualPlant(5243,  "Monstera",              "Monstera deliciosa",        "Doniczkowe"),
        PerenualPlant(2094,  "Bluszcz",               "Hedera helix",              "Doniczkowe"),
        PerenualPlant(151,   "Aloes",                 "Aloe vera",                 "Doniczkowe"),
    )

    fun findById(id: Int): PerenualPlant? = plants.find { it.perenualId == id }
}
