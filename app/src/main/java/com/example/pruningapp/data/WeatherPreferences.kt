package com.example.pruningapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.weatherDataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_prefs")

data class WeatherData(
    val temp: Float,
    val conditionId: Int,
    val description: String,
    val city: String,
    val cachedAt: Long
) {
    val isFresh: Boolean
        get() = System.currentTimeMillis() - cachedAt < 3 * 60 * 60 * 1000L

    val hasWarning: Boolean
        get() = temp < 0f || conditionId in 200..531

    val warningText: String
        get() = when {
            temp < 0f && conditionId in 600..622 ->
                "Mróz i opady śniegu (${temp.toInt()}°C). Nie zalecamy wykonywania zabiegów ogrodniczych."
            temp < 0f ->
                "Temperatura poniżej 0°C (${temp.toInt()}°C). Mróz może uszkodzić przycięte rośliny. Sugerujemy przełożenie zadania na inny dzień."
            conditionId in 200..231 ->
                "Prognozowana burza z piorunami. Sugerujemy przełożenie prac ogrodniczych na inny dzień."
            conditionId in 300..531 ->
                "Prognozowane opady deszczu. Sugerujemy przełożenie przycinania i podlewania na inny dzień."
            else -> ""
        }

    val warningTextShort: String
        get() = when {
            temp < 0f              -> "Pogoda nie sprzyja: mróz (${temp.toInt()}°C). Rozważ przełożenie zadania."
            conditionId in 200..231 -> "Pogoda nie sprzyja: burza. Rozważ przełożenie zadania."
            conditionId in 300..531 -> "Pogoda nie sprzyja: deszcz. Rozważ przełożenie zadania."
            else                   -> ""
        }
}

class WeatherPreferences(private val context: Context) {

    companion object {
        val KEY_TEMP         = floatPreferencesKey("weather_temp")
        val KEY_CONDITION_ID = intPreferencesKey("weather_condition_id")
        val KEY_DESCRIPTION  = stringPreferencesKey("weather_description")
        val KEY_CACHED_AT    = longPreferencesKey("weather_cached_at")
        val KEY_CITY         = stringPreferencesKey("weather_city")
    }

    suspend fun getCity(): String =
        context.weatherDataStore.data.first()[KEY_CITY] ?: "Lublin"

    suspend fun setCity(city: String) =
        context.weatherDataStore.edit { it[KEY_CITY] = city }

    suspend fun getCachedWeather(): WeatherData? {
        val prefs = context.weatherDataStore.data.first()
        val cachedAt = prefs[KEY_CACHED_AT] ?: return null
        return WeatherData(
            temp        = prefs[KEY_TEMP] ?: 15f,
            conditionId = prefs[KEY_CONDITION_ID] ?: 800,
            description = prefs[KEY_DESCRIPTION] ?: "",
            city        = prefs[KEY_CITY] ?: "Lublin",
            cachedAt    = cachedAt
        )
    }

    suspend fun cacheWeather(data: WeatherData) {
        context.weatherDataStore.edit { prefs ->
            prefs[KEY_TEMP]         = data.temp
            prefs[KEY_CONDITION_ID] = data.conditionId
            prefs[KEY_DESCRIPTION]  = data.description
            prefs[KEY_CACHED_AT]    = data.cachedAt
        }
    }
}
