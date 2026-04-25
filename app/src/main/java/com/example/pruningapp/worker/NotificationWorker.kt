package com.example.pruningapp.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pruningapp.App
import com.example.pruningapp.R
import com.example.pruningapp.data.AppDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        // Powiadom o wszystkich oknach cięcia aktywnych dziś (date <= today <= endDate)
        db.taskDao().getActiveTasksForToday(today).forEach { task ->
            val plant = db.plantDao().getPlantById(task.plantId) ?: return@forEach

            val isWindowStart = task.date == today
            val title = if (isWindowStart)
                "Otwiera się okno cięcia — ${plant.name}"
            else
                "Trwa okno cięcia — ${plant.name}"

            sendNotification(
                title = title,
                body = "Możesz przyciąć między ${task.date.substring(5)} a ${task.endDate.substring(5)}",
                notificationId = (task.id % Int.MAX_VALUE).toInt()
            )
        }

        return Result.success()
    }

    private fun sendNotification(title: String, body: String, notificationId: Int) {
        val notification = NotificationCompat.Builder(applicationContext, App.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_plant_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}
