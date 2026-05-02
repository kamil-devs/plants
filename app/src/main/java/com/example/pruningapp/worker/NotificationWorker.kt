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
        val today = LocalDate.now()
        val todayStr = today.format(fmt)
        val tomorrowStr = today.plusDays(1).format(fmt)
        val dayAfterStr = today.plusDays(2).format(fmt)
        val weekStart = today.minusDays((today.dayOfWeek.value - 1).toLong()).format(fmt)
        val weekEnd = today.minusDays((today.dayOfWeek.value - 1).toLong()).plusDays(6).format(fmt)

        sendActiveTodayNotifications(db, todayStr)
        sendTomorrowNotifications(db, tomorrowStr)
        sendOverdueNotification(db, todayStr)
        sendEndingSoonNotifications(db, tomorrowStr, dayAfterStr)
        if (today.dayOfWeek.value == 1) sendWeeklyWindowsNotification(db, weekStart, weekEnd)
        sendSmartNotification(db, today, todayStr, weekStart, weekEnd, fmt)

        return Result.success()
    }

    private suspend fun sendActiveTodayNotifications(db: AppDatabase, todayStr: String) {
        db.taskDao().getActiveTasksForToday(todayStr).forEach { task ->
            val plant = db.plantDao().getPlantById(task.plantId) ?: return@forEach
            val isStart = task.date == todayStr
            sendNotification(
                channelId = App.CHANNEL_ID,
                title = if (isStart) "Dzis zaczyna sie okno ciecia - ${plant.name}"
                        else "Trwa okno ciecia - ${plant.name}",
                body = "Mozesz przycinac: ${task.date.substring(5)} - ${task.endDate.substring(5)}",
                notificationId = task.id.toInt()
            )
        }
    }

    private suspend fun sendTomorrowNotifications(db: AppDatabase, tomorrowStr: String) {
        db.taskDao().getTasksStartingOn(tomorrowStr).forEach { task ->
            val plant = db.plantDao().getPlantById(task.plantId) ?: return@forEach
            sendNotification(
                channelId = App.CHANNEL_ID,
                title = "Jutro zaczyna sie okno ciecia - ${plant.name}",
                body = "Okno: ${task.date.substring(5)} - ${task.endDate.substring(5)}",
                notificationId = task.id.toInt() + 500_000
            )
        }
    }

    private suspend fun sendOverdueNotification(db: AppDatabase, todayStr: String) {
        val overdue = db.taskDao().getOverdueTasks(todayStr)
        if (overdue.isEmpty()) return
        val names = overdue
            .mapNotNull { db.plantDao().getPlantById(it.plantId)?.name }
            .take(3)
            .joinToString(", ")
        val suffix = if (overdue.size > 3) " i inne" else ""
        sendNotification(
            channelId = App.CHANNEL_ID,
            title = "Zaległe ciecia: ${overdue.size}",
            body = if (names.isNotEmpty()) "$names$suffix" else "Sprawdz zakładke Glowna",
            notificationId = 1_000_000
        )
    }

    private suspend fun sendEndingSoonNotifications(
        db: AppDatabase,
        tomorrowStr: String,
        dayAfterStr: String
    ) {
        db.taskDao().getTasksEndingSoon(tomorrowStr, dayAfterStr).forEach { task ->
            val plant = db.plantDao().getPlantById(task.plantId) ?: return@forEach
            sendNotification(
                channelId = App.CHANNEL_ID,
                title = "Konczy sie okno ciecia - ${plant.name}",
                body = "Ostatni dzien: ${task.endDate.substring(5)}. Nie przegap!",
                notificationId = task.id.toInt() + 1_500_000
            )
        }
    }

    private suspend fun sendWeeklyWindowsNotification(
        db: AppDatabase,
        weekStart: String,
        weekEnd: String
    ) {
        val tasks = db.taskDao().getTasksStartingInRange(weekStart, weekEnd)
        val ownedTasks = tasks.filter {
            db.plantDao().getPlantById(it.plantId)?.owned == true
        }
        if (ownedTasks.isEmpty()) return
        val names = ownedTasks
            .mapNotNull { db.plantDao().getPlantById(it.plantId)?.name }
            .distinct()
            .take(3)
            .joinToString(", ")
        val suffix = if (ownedTasks.size > 3) " i inne" else ""
        sendNotification(
            channelId = App.CHANNEL_ID,
            title = "Ten tydzien: ${ownedTasks.size} nowych okien ciecia",
            body = "$names$suffix",
            notificationId = 2_000_000
        )
    }

    private suspend fun sendSmartNotification(
        db: AppDatabase,
        today: LocalDate,
        todayStr: String,
        weekStart: String,
        weekEnd: String,
        fmt: DateTimeFormatter
    ) {
        val month = today.monthValue
        val seasonMsg = when (month) {
            3 -> "Wiosna sie zaczyna - dobry czas na pierwsze ciecia"
            4 -> "Kwiecien sprzyja cieciu roslin ozdobnych"
            5 -> "Maj - idealna pora na ciecie i formowanie"
            8 -> "Sierpien: czas na ciecia letnie i przygotowania"
            9 -> "Jesien - przytnij rosliny przed zima"
            10 -> "Ostatni moment na ciecia jesienne"
            else -> return
        }

        // Szukaj posiadanej rosliny z aktywnym oknem
        val activeTasks = db.taskDao().getActiveTasksForToday(todayStr)
        val activePlant = activeTasks
            .mapNotNull { db.plantDao().getPlantById(it.plantId) }
            .firstOrNull { it.owned }

        if (activePlant != null) {
            sendNotification(
                channelId = App.CHANNEL_SMART_ID,
                title = "Wskazowka: ${activePlant.name}",
                body = "$seasonMsg. Okno ciecia aktywne!",
                notificationId = 2_500_000
            )
            return
        }

        // Fallback: szukaj rosliny z oknem w tym tygodniu
        val weekTasks = db.taskDao().getTasksStartingInRange(weekStart, weekEnd)
        val weekPlant = weekTasks
            .mapNotNull { db.plantDao().getPlantById(it.plantId) }
            .firstOrNull { it.owned }

        if (weekPlant != null) {
            sendNotification(
                channelId = App.CHANNEL_SMART_ID,
                title = "Wskazowka ogrodnicza",
                body = "$seasonMsg. Wkrotce ciecie: ${weekPlant.name}",
                notificationId = 2_500_001
            )
        }
    }

    private fun sendNotification(
        channelId: String,
        title: String,
        body: String,
        notificationId: Int
    ) {
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_plant_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(
                if (channelId == App.CHANNEL_SMART_ID) NotificationCompat.PRIORITY_LOW
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .build()

        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}
