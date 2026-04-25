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
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now().format(fmt)
        val tomorrow = LocalDate.now().plusDays(1).format(fmt)

        // Okna aktywne dziś (date <= today <= endDate)
        db.taskDao().getActiveTasksForToday(today).forEach { task ->
            val plant = db.plantDao().getPlantById(task.plantId) ?: return@forEach
            val isStart = task.date == today
            sendNotification(
                title = if (isStart) "Dziś zaczyna się okno cięcia — ${plant.name}"
                        else "Trwa okno cięcia — ${plant.name}",
                body = "Możesz przycinać: ${task.date.substring(5)} – ${task.endDate.substring(5)}",
                notificationId = (task.id % Int.MAX_VALUE).toInt()
            )
        }

        // Okna zaczynające się jutro — przypomnienie z wyprzedzeniem
        db.taskDao().getTasksStartingOn(tomorrow).forEach { task ->
            val plant = db.plantDao().getPlantById(task.plantId) ?: return@forEach
            sendNotification(
                title = "Jutro zaczyna się okno cięcia — ${plant.name}",
                body = "Okno: ${task.date.substring(5)} – ${task.endDate.substring(5)}",
                notificationId = (task.id + 500_000).toInt()
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
