package com.example.pruningapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_prefs")

data class SyncStatusInfo(
    val lastRunAtMillis: Long = 0L,
    val failCount: Int = 0,
    val totalCount: Int = 0,
    val lastError: String? = null
) {
    val hadErrors: Boolean get() = failCount > 0 && !lastError.isNullOrBlank()
}

class SyncPreferences(private val context: Context) {

    val status: Flow<SyncStatusInfo> = context.syncDataStore.data.map { prefs ->
        SyncStatusInfo(
            lastRunAtMillis = prefs[KEY_LAST_RUN] ?: 0L,
            failCount = prefs[KEY_FAIL_COUNT] ?: 0,
            totalCount = prefs[KEY_TOTAL_COUNT] ?: 0,
            lastError = prefs[KEY_LAST_ERROR]
        )
    }

    suspend fun recordResult(total: Int, failCount: Int, lastError: String?) {
        context.syncDataStore.edit { prefs ->
            prefs[KEY_LAST_RUN] = System.currentTimeMillis()
            prefs[KEY_TOTAL_COUNT] = total
            prefs[KEY_FAIL_COUNT] = failCount
            prefs[KEY_LAST_ERROR] = when {
                failCount > 0 -> lastError ?: "Nieznany blad synchronizacji"
                else -> ""
            }
        }
    }

    suspend fun clearError() {
        context.syncDataStore.edit { prefs ->
            prefs[KEY_FAIL_COUNT] = 0
            prefs[KEY_LAST_ERROR] = ""
        }
    }

    companion object {
        private val KEY_LAST_RUN = longPreferencesKey("sync_last_run")
        private val KEY_FAIL_COUNT = intPreferencesKey("sync_fail_count")
        private val KEY_TOTAL_COUNT = intPreferencesKey("sync_total_count")
        private val KEY_LAST_ERROR = stringPreferencesKey("sync_last_error")
    }
}
