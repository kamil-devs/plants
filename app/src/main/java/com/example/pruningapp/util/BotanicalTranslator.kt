package com.example.pruningapp.util

object BotanicalTranslator {

    fun translateSunlight(value: String): String = when (value.lowercase().trim()) {
        "full sun"                -> "Pełne słońce"
        "part shade"              -> "Półcień"
        "part sun/part shade"     -> "Słoneczno-półcień"
        "full shade"              -> "Pełny cień"
        "sun-exposed"             -> "Nasłonecznione"
        "filtered indirect light" -> "Filtrowane światło"
        else                      -> value
    }

    fun translateSunlightList(value: String): String =
        value.split(", ").joinToString(", ") { translateSunlight(it) }

    fun translateWatering(value: String): String = when (value.lowercase().trim()) {
        "minimum", "minimum watering" -> "Rzadkie"
        "average"                     -> "Umiarkowane"
        "frequent"                    -> "Częste"
        "maximum"                     -> "Intensywne"
        else                          -> value
    }

    fun translateMaintenance(value: String): String = when (value.lowercase().trim()) {
        "low"                 -> "Niski"
        "medium", "moderate"  -> "Średni"
        "high"                -> "Wysoki"
        else                  -> value
    }
}
