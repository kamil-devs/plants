package com.example.pruningapp.repository

import android.content.Context
import com.example.pruningapp.BuildConfig
import com.example.pruningapp.data.WeatherData
import com.example.pruningapp.data.WeatherPreferences
import com.example.pruningapp.remote.WeatherApiService

class WeatherRepository(private val context: Context) {

    private val prefs = WeatherPreferences(context)
    private val api   = WeatherApiService.instance

    /**
     * Returns fresh or cached weather. Returns null only when there's no
     * cache at all AND the network call fails.
     */
    suspend fun getWeather(): WeatherData? {
        val cached = try { prefs.getCachedWeather() } catch (_: Exception) { null }
        if (cached != null && cached.isFresh) return cached

        if (BuildConfig.WEATHER_API_KEY.isBlank()) return cached

        return try {
            val location = prefs.getFullLocation()
            val response = api.getCurrentWeather(
                city   = location,
                apiKey = BuildConfig.WEATHER_API_KEY,
                units  = "metric",
                lang   = "pl"
            )
            val condition = response.weather.firstOrNull()
            val data = WeatherData(
                temp        = response.main.temp,
                conditionId = condition?.id ?: 800,
                description = condition?.description ?: "",
                city        = response.name,
                cachedAt    = System.currentTimeMillis()
            )
            prefs.cacheWeather(data)
            data
        } catch (_: Exception) {
            cached  // Stale cache beats nothing
        }
    }

    suspend fun searchCities(query: String): List<com.example.pruningapp.remote.GeocodingResponse> {
        if (BuildConfig.WEATHER_API_KEY.isBlank()) return emptyList()
        val trimmed = query.trim()
        val polandCode = "PL"
        
        // Extended Smart Search Logic
        // 1. Clean query and handle common Polish regional names
        val cleanedQuery = trimmed
            .replace(Regex("województwo", RegexOption.IGNORE_CASE), "")
            .replace(Regex("woj\\.", RegexOption.IGNORE_CASE), "")
            .replace(Regex("powiat", RegexOption.IGNORE_CASE), "")
            .replace(Regex("podkarpacie", RegexOption.IGNORE_CASE), "podkarpackie")
            .replace(Regex("małopolska", RegexOption.IGNORE_CASE), "małopolskie")
            .replace(Regex("wielkopolska", RegexOption.IGNORE_CASE), "wielkopolskie")
            .replace(Regex("śląsk", RegexOption.IGNORE_CASE), "śląskie")
            .trim()

        return try {
            // Case 1: Pure Zip Code (e.g. 39-305)
            if (cleanedQuery.matches(Regex("^[0-9]{2}-[0-9]{3}$")) || cleanedQuery.matches(Regex("^[0-9]{5}$"))) {
                val response = api.findByZip("$cleanedQuery,$polandCode", BuildConfig.WEATHER_API_KEY)
                listOf(response)
            } else {
                // Case 2: Mixed search (e.g. Borowa 39-305, Borowa Mielec)
                val zipInQuery = Regex("[0-9]{2}-[0-9]{3}").find(cleanedQuery)?.value
                if (zipInQuery != null) {
                    try {
                        val response = api.findByZip("$zipInQuery,$polandCode", BuildConfig.WEATHER_API_KEY)
                        return listOf(response)
                    } catch (_: Exception) {}
                }

                // If it's a multi-word query (e.g. "Borowa Podkarpackie"), try to comma-separate for OWM
                val geocodeQuery = if (cleanedQuery.contains(" ") && !cleanedQuery.contains(",")) {
                    cleanedQuery.replace(" ", ",") + ",$polandCode"
                } else {
                    "$cleanedQuery,$polandCode"
                }
                
                api.findCities(geocodeQuery, 20, BuildConfig.WEATHER_API_KEY)
            }
        } catch (e: Exception) {
            try {
                api.findCities("$cleanedQuery,PL", 20, BuildConfig.WEATHER_API_KEY)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun setLocation(city: String, country: String?, state: String?) {
        prefs.setLocation(city, country, state)
    }
}
