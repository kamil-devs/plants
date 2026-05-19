package com.example.pruningapp.remote

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
            // Wikipedia requires a User-Agent that identifies the app; build on top of sharedOkHttpClient
            val wikiClient = sharedOkHttpClient.newBuilder()
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .header("User-Agent", "PruningApp/1.0 (Android; plant pruning calendar)")
                            .build()
                    )
                }
                .build()

            Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/")
                .client(wikiClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WikipediaApiService::class.java)
        }
    }
}
