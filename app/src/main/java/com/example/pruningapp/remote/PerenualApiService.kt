package com.example.pruningapp.remote

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PerenualApiService {

    @GET("species/details/{id}")
    suspend fun getSpeciesDetails(
        @Path("id") id: Int,
        @Query("key") apiKey: String
    ): SpeciesDetails

    companion object {
        val instance: PerenualApiService by lazy {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val gson = GsonBuilder()
                .registerTypeAdapter(PruningCount::class.java, PruningCountDeserializer())
                .create()

            Retrofit.Builder()
                .baseUrl("https://perenual.com/api/v2/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(PerenualApiService::class.java)
        }
    }
}
