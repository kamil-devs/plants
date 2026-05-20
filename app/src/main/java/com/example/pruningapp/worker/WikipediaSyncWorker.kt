package com.example.pruningapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters

// Wikipedia image sync removed — all plants now use local drawable resources.
// Worker retained so existing enqueue calls in App.kt compile without changes.
class WikipediaSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = Result.success()

    companion object {
        private const val WORK_NAME = "wikipedia_image_sync"

        fun enqueue(context: Context) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    OneTimeWorkRequestBuilder<WikipediaSyncWorker>().build()
                )
        }
    }
}
