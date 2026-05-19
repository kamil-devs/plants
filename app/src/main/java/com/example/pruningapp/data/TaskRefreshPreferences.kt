package com.example.pruningapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.taskRefreshDataStore: DataStore<Preferences> by preferencesDataStore(name = "task_refresh_prefs")

class TaskRefreshPreferences(private val context: Context) {

    suspend fun getLastRefreshYear(): Int? =
        context.taskRefreshDataStore.data.first()[KEY_YEAR]

    suspend fun setLastRefreshYear(year: Int) {
        context.taskRefreshDataStore.edit { it[KEY_YEAR] = year }
    }

    companion object {
        private val KEY_YEAR = intPreferencesKey("last_task_refresh_year")
    }
}
