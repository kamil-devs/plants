package com.example.pruningapp.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface WikipediaApiService {

    @GET
    suspend fun getPageImages(
        @Url url: String,
        @Query("action") action: String = "query",
        @Query("titles") titles: String,
        @Query("prop") prop: String = "pageimages",
        @Query("format") format: String = "json",
        @Query("pithumbsize") thumbSize: Int = 500,
        @Query("redirects") redirects: Int = 1
    ): WikipediaResponse

    companion object {
        val instance: WikipediaApiService by lazy {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .header("User-Agent", "PruningApp/1.0 (https://github.com/example/plants; contact@example.com) Android")
                            .build()
                    )
                }
                .build()

            Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WikipediaApiService::class.java)
        }
    }
}
