package com.example.pruningapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.data.TaskRefreshPreferences
import com.example.pruningapp.data.generateTasksForRule
import java.time.LocalDate
import java.util.concurrent.TimeUnit

// Ensures pruning-window tasks exist for the current and next calendar year.
// Runs daily but only regenerates when the calendar year changes or on first run.
class TaskRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = TaskRefreshPreferences(applicationContext)
        val currentYear = LocalDate.now().year
        val lastYear = prefs.getLastRefreshYear()

        if (lastYear == currentYear) {
            return Result.success()
        }

        val db = AppDatabase.getDatabase(applicationContext)
        val rules = db.plantDao().getAllPruningRules()
        var generated = 0

        for (rule in rules) {
            try {
                db.generateTasksForRule(
                    plantId = rule.plantId,
                    startMonthDay = rule.startMonthDay,
                    endMonthDay = rule.endMonthDay,
                    type = rule.type
                )
                generated++
            } catch (e: Exception) {
                Log.e(TAG, "Task refresh failed for plantId=${rule.plantId}: ${e.message}")
            }
        }

        prefs.setLastRefreshYear(currentYear)
        Log.i(TAG, "Task refresh for year $currentYear: processed ${rules.size} rules ($generated ok)")
        return Result.success()
    }

    companion object {
        private const val TAG = "TaskRefreshWorker"
        private const val WORK_NAME = "task_refresh_daily"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<TaskRefreshWorker>(1, TimeUnit.DAYS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun runOnceNow(context: Context) {
            val request = androidx.work.OneTimeWorkRequestBuilder<TaskRefreshWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
