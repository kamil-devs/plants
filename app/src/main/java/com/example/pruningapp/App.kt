package com.example.pruningapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.*
import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.JsonImporter
import com.example.pruningapp.worker.NotificationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class App : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleNotificationWorker()

        CoroutineScope(Dispatchers.IO).launch {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            if (!prefs.getBoolean("json_imported", false)) {
                JsonImporter(this@App, database).import()
                prefs.edit().putBoolean("json_imported", true).apply()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pruning Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily pruning reminders for your plants"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun scheduleNotificationWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "pruning_notification",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        const val CHANNEL_ID = "pruning_channel"
    }
}
