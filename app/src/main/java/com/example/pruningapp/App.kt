package com.example.pruningapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.JsonImporter
import com.example.pruningapp.worker.GlobalSyncWorker
import com.example.pruningapp.worker.WikipediaSyncWorker
import com.example.pruningapp.worker.NotificationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.ImageLoaderFactory
import okhttp3.OkHttpClient

class App : Application(), ImageLoaderFactory {

    val database by lazy { AppDatabase.getDatabase(this) }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // ok. 50MB-100MB zależnie od urządzenia
                    .build()
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("User-Agent", "PruningApp/1.0 (Android; plant pruning calendar)")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            }
            .respectCacheHeaders(false) // Ignorujemy nagłówki Cache-Control serwera, by wymusić offline-first
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleNotificationWorker()

        CoroutineScope(Dispatchers.IO).launch {
            if (database.plantDao().getPlantCount() == 0) {
                JsonImporter(this@App, database).import()
            }
        }
        GlobalSyncWorker.enqueue(this)
        WikipediaSyncWorker.enqueue(this)
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Przypomnienia o cieciu",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Codzienne przypomnienia: aktywne okna, zaległe i kończące sie ciecia"
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SMART_ID,
                "Inteligentne wskazowki",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sezonowe porady ogrodnicze dostosowane do posiadanych roslin"
            }
        )
    }

    private fun scheduleNotificationWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(minutesUntil8AM(), TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun minutesUntil8AM(): Long {
        val now = LocalTime.now()
        val target = LocalTime.of(8, 0)
        val duration = java.time.Duration.between(now, target)
        val minutes = if (now.isBefore(target)) {
            duration.toMinutes()
        } else {
            duration.toMinutes() + 24 * 60
        }
        return minutes.coerceAtLeast(1)
    }

    companion object {
        const val CHANNEL_ID = "pruning_channel"
        const val CHANNEL_SMART_ID = "smart_channel"
        private const val WORK_NAME = "pruning_notification_daily"
    }
}
