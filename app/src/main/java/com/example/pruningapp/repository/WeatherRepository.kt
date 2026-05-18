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
            val city = prefs.getCity()
            val response = api.getCurrentWeather(
                city   = city,
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
}
