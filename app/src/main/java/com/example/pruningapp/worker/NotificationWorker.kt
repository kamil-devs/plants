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
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now().format(formatter)
        val tomorrow = LocalDate.now().plusDays(1).format(formatter)

        val todayTasks = db.taskDao().getPendingTasksForDate(today)
        val tomorrowTasks = db.taskDao().getPendingTasksForDate(tomorrow)

        (todayTasks + tomorrowTasks).forEach { task ->
            val plant = db.plantDao().getPlantById(task.plantId) ?: return@forEach
            val rules = db.plantDao().getPruningRulesForPlant(task.plantId)
            val rule = rules.firstOrNull { it.type == task.type } ?: return@forEach
            val label = if (task.date == today) "Dziś" else "Jutro"

            sendNotification(
                plantName = plant.name,
                start = rule.startMonthDay,
                end = rule.endMonthDay,
                label = label,
                notificationId = (task.id % Int.MAX_VALUE).toInt()
            )
        }

        return Result.success()
    }

    private fun sendNotification(
        plantName: String,
        start: String,
        end: String,
        label: String,
        notificationId: Int
    ) {
        val notification = NotificationCompat.Builder(applicationContext, App.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_plant_notification)
            .setContentTitle("Czas przyciąć $plantName")
            .setContentText("$label • Okno cięcia: $start–$end")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}
