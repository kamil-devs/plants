package com.example.pruningapp.remote

import com.example.pruningapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

// Shared OkHttpClient used by all Retrofit instances.
// Logging is only active in debug builds to avoid leaking request bodies in production.
val sharedOkHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                )
            }
        }
        .build()
}
