package com.example.pruningapp.remote

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("weather") val weather: List<WeatherCondition>,
    @SerializedName("main") val main: MainWeather,
    @SerializedName("name") val name: String
)

data class WeatherCondition(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String
)

data class MainWeather(
    @SerializedName("temp") val temp: Float
)
