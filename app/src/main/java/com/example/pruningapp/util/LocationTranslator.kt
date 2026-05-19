package com.example.pruningapp.util

object LocationTranslator {
    private val voivodeships = mapOf(
        "Greater Poland Voivodeship" to "Wielkopolskie",
        "Greater Poland" to "Wielkopolskie",
        "Lesser Poland Voivodeship" to "Małopolskie",
        "Lesser Poland" to "Małopolskie",
        "Masovian Voivodeship" to "Mazowieckie",
        "Masovian" to "Mazowieckie",
        "Lower Silesian Voivodeship" to "Dolnośląskie",
        "Lower Silesian" to "Dolnośląskie",
        "Łódź Voivodeship" to "Łódzkie",
        "Lodz Voivodeship" to "Łódzkie",
        "Łódź" to "Łódzkie",
        "Pomeranian Voivodeship" to "Pomorskie",
        "Pomeranian" to "Pomorskie",
        "Silesian Voivodeship" to "Śląskie",
        "Silesian" to "Śląskie",
        "Lublin Voivodeship" to "Lubelskie",
        "Lublin" to "Lubelskie",
        "Subcarpathian Voivodeship" to "Podkarpackie",
        "Subcarpathian" to "Podkarpackie",
        "Podlaskie Voivodeship" to "Podlaskie",
        "Podlaskie" to "Podlaskie",
        "West Pomeranian Voivodeship" to "Zachodniopomorskie",
        "West Pomeranian" to "Zachodniopomorskie",
        "Holy Cross Voivodeship" to "Świętokrzyskie",
        "Świętokrzyskie" to "Świętokrzyskie",
        "Swietokrzyskie" to "Świętokrzyskie",
        "Warmian-Masurian Voivodeship" to "Warmińsko-Mazurskie",
        "Warmian-Masurian" to "Warmińsko-Mazurskie",
        "Kuyavian-Pomeranian Voivodeship" to "Kujawsko-Pomorskie",
        "Kuyavian-Pomeranian" to "Kujawsko-Pomorskie",
        "Lubusz Voivodeship" to "Lubuskie",
        "Lubusz" to "Lubuskie",
        "Opole Voivodeship" to "Opolskie",
        "Opole" to "Opolskie"
    )

    fun translateVoivodeship(englishName: String?): String? {
        if (englishName == null) return null
        return voivodeships[englishName] ?: englishName
    }
}
