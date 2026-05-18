package com.example.pruningapp.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.pruningapp.data.AppDatabase
import com.example.pruningapp.repository.PlantRepository
import retrofit2.HttpException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class PerenualSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val plantId = inputData.getLong(KEY_PLANT_ID, -1L)
        if (plantId == -1L) return Result.failure()

        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            PlantRepository(db).syncPlantFromApi(plantId)
            Result.success()
        } catch (e: HttpException) {
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        } catch (e: UnknownHostException) {
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val KEY_PLANT_ID = "plant_id"
        private const val MAX_RETRIES = 5

        fun buildRequest(plantId: Long): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<PerenualSyncWorker>()
                .setInputData(workDataOf(KEY_PLANT_ID to plantId))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()
    }
}
