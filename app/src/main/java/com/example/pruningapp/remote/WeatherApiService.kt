package com.example.pruningapp.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
        @Query("lang") lang: String
    ): WeatherResponse

    @GET("https://api.openweathermap.org/geo/1.0/direct")
    suspend fun findCities(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("appid") apiKey: String
    ): List<GeocodingResponse>

    @GET("https://api.openweathermap.org/geo/1.0/zip")
    suspend fun findByZip(
        @Query("zip") zipAndCountry: String,
        @Query("appid") apiKey: String
    ): GeocodingResponse

    companion object {
        val instance: WeatherApiService by lazy {
            Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .client(sharedOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApiService::class.java)
        }
    }
}
