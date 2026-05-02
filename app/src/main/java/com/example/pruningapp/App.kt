package com.example.pruningapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.JsonImporter
import com.example.pruningapp.worker.NotificationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class App : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleNotificationWorker()

        CoroutineScope(Dispatchers.IO).launch {
            if (database.plantDao().getPlantCount() == 0) {
                JsonImporter(this@App, database).import()
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        val minutes = if (now.isBefore(target)) {
            java.time.Duration.between(now, target).toMinutes()
        } else {
            java.time.Duration.between(now, target).toMinutes() + 24 * 60
        }
        return minutes.coerceAtLeast(1)
    }

    companion object {
        const val CHANNEL_ID = "pruning_channel"
        const val CHANNEL_SMART_ID = "smart_channel"
        private const val WORK_NAME = "pruning_notification_daily"
    }
}
