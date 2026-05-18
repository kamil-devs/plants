package com.example.pruningapp.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WikipediaApiService {

    @GET("w/api.php")
    suspend fun getPageImages(
        @Query("action") action: String = "query",
        @Query("titles") titles: String,
        @Query("prop") prop: String = "pageimages",
        @Query("format") format: String = "json",
        @Query("pithumbsize") thumbSize: Int = 600,
        @Query("redirects") redirects: Int = 1
    ): WikipediaResponse

    companion object {
        val instance: WikipediaApiService by lazy {
            Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WikipediaApiService::class.java)
        }
    }
}
