package com.example.pruningapp.data

object PlantDatabase {

    val plants: List<PerenualPlant> = listOf(
        // Owocowe
        PerenualPlant(4892,  "Jabłoń",                "Malus domestica",           "Owocowe"),
        PerenualPlant(7041,  "Grusza",                "Pyrus communis",            "Owocowe"),
        PerenualPlant(7001,  "Śliwa",                 "Prunus domestica",          "Owocowe"),
        PerenualPlant(6998,  "Wiśnia",                "Prunus cerasus",            "Owocowe"),
        PerenualPlant(6995,  "Czereśnia",             "Prunus avium",              "Owocowe"),
        PerenualPlant(7003,  "Brzoskwinia",           "Prunus persica",            "Owocowe"),
        PerenualPlant(6994,  "Morela",                "Prunus armeniaca",          "Owocowe"),
        PerenualPlant(7214,  "Porzeczka czarna",      "Ribes nigrum",              "Owocowe"),
        PerenualPlant(7216,  "Porzeczka czerwona",    "Ribes rubrum",              "Owocowe"),
        PerenualPlant(3694,  "Agrest",                "Ribes uva-crispa",          "Owocowe"),
        PerenualPlant(7192,  "Malina letnia",         "Rubus idaeus",              "Owocowe"),
        PerenualPlant(7192,  "Malina powtarzająca",   "Rubus idaeus",              "Owocowe"),
        PerenualPlant(7201,  "Jeżyna",                "Rubus fruticosus",          "Owocowe"),
        PerenualPlant(8780,  "Borówka wysoka",        "Vaccinium corymbosum",      "Owocowe"),
        PerenualPlant(4710,  "Orzech włoski",         "Juglans regia",             "Owocowe"),
        PerenualPlant(8712,  "Winorośl",              "Vitis vinifera",            "Owocowe"),
        PerenualPlant(2018,  "Leszczyna",             "Corylus avellana",          "Owocowe"),
        PerenualPlant(1812,  "Pigwowiec",             "Chaenomeles speciosa",      "Owocowe"),
        PerenualPlant(2074,  "Dereń jadalny",         "Cornus mas",                "Owocowe"),
        PerenualPlant(955,   "Aronia",                "Aronia melanocarpa",        "Owocowe"),
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
        PerenualPlant(1251,  "Budleja Dawida",        "Buddleja davidii",          "Ozdobne"),
        PerenualPlant(3232,  "Forsycja",              "Forsythia",                 "Ozdobne"),
        PerenualPlant(5937,  "Jaśminowiec",           "Philadelphus coronarius",   "Ozdobne"),
        PerenualPlant(8013,  "Tawuła japońska",       "Spiraea japonica",          "Ozdobne"),
        PerenualPlant(1025,  "Tawuła wczesna",        "Spiraea arguta",            "Ozdobne"),
        PerenualPlant(1150,  "Berberys",              "Berberis vulgaris",         "Ozdobne"),
        PerenualPlant(4815,  "Ligustr",               "Ligustrum vulgare",         "Ozdobne"),
        PerenualPlant(8300,  "Żywotnik (tuja)",       "Thuja occidentalis",        "Ozdobne"),
        PerenualPlant(8210,  "Cis",                   "Taxus baccata",             "Ozdobne"),
        PerenualPlant(8821,  "Krzewuszka",            "Weigela florida",           "Ozdobne"),
        PerenualPlant(6521,  "Pięciornik krzewiasty",  "Dasiphora fruticosa",       "Ozdobne"),
        PerenualPlant(2125,  "Perukowiec",            "Cotinus coggygria",         "Ozdobne"),
        PerenualPlant(2822,  "Trzmielina",            "Euonymus europaeus",        "Ozdobne"),
        PerenualPlant(4615,  "Jałowiec",              "Juniperus communis",        "Ozdobne"),
        PerenualPlant(8235,  "Bez lilak",             "Syringa vulgaris",          "Ozdobne"),
        PerenualPlant(3612,  "Oczar",                 "Hamamelis virginiana",      "Ozdobne"),

        // Ozdobne drzewa
        PerenualPlant(321,   "Klon kulisty",          "Acer platanoides",          "Ozdobne drzewa"),
        PerenualPlant(7521,  "Wierzba Hakuro-Nishiki","Salix integra",             "Ozdobne drzewa"),
        PerenualPlant(7411,  "Robinia akacjowa",      "Robinia pseudoacacia",      "Ozdobne drzewa"),
        PerenualPlant(5125,  "Magnolia",              "Magnolia",                  "Ozdobne drzewa"),

        // Doniczkowe
        PerenualPlant(5243,  "Monstera",              "Monstera deliciosa",        "Doniczkowe"),
        PerenualPlant(2094,  "Bluszcz",               "Hedera helix",              "Doniczkowe"),
        PerenualPlant(151,   "Aloes",                 "Aloe vera",                 "Doniczkowe"),
    )

    fun findById(id: Int): PerenualPlant? = plants.find { it.perenualId == id }
}
