package com.example.pruningapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.pruningapp.data.SyncPreferences

// Perenual API sync removed — app is fully offline-first.
// Worker retained so existing enqueue calls in App.kt compile without changes.
class GlobalSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        SyncPreferences(applicationContext).recordResult(total = 0, failCount = 0, lastError = null)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "global_perenual_sync"

        fun enqueue(context: Context) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<GlobalSyncWorker>().build()
                )
        }
    }
}
