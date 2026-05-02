package com.example.pruningapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.notifDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_prefs")

data class NotifSettings(
    val activeToday: Boolean = true,
    val tomorrow: Boolean = true,
    val overdue: Boolean = true,
    val endingSoon: Boolean = true,
    val weekly: Boolean = true,
    val smart: Boolean = true
)

class NotificationPreferences(private val context: Context) {

    companion object {
        val KEY_ACTIVE_TODAY = booleanPreferencesKey("notify_active_today")
        val KEY_TOMORROW = booleanPreferencesKey("notify_tomorrow")
        val KEY_OVERDUE = booleanPreferencesKey("notify_overdue")
        val KEY_ENDING_SOON = booleanPreferencesKey("notify_ending_soon")
        val KEY_WEEKLY = booleanPreferencesKey("notify_weekly")
        val KEY_SMART = booleanPreferencesKey("notify_smart")
    }

    val settings: Flow<NotifSettings> = context.notifDataStore.data.map { prefs ->
        NotifSettings(
            activeToday = prefs[KEY_ACTIVE_TODAY] ?: true,
            tomorrow = prefs[KEY_TOMORROW] ?: true,
            overdue = prefs[KEY_OVERDUE] ?: true,
            endingSoon = prefs[KEY_ENDING_SOON] ?: true,
            weekly = prefs[KEY_WEEKLY] ?: true,
            smart = prefs[KEY_SMART] ?: true
        )
    }

    suspend fun setActiveToday(enabled: Boolean) =
        context.notifDataStore.edit { it[KEY_ACTIVE_TODAY] = enabled }

    suspend fun setTomorrow(enabled: Boolean) =
        context.notifDataStore.edit { it[KEY_TOMORROW] = enabled }

    suspend fun setOverdue(enabled: Boolean) =
        context.notifDataStore.edit { it[KEY_OVERDUE] = enabled }

    suspend fun setEndingSoon(enabled: Boolean) =
        context.notifDataStore.edit { it[KEY_ENDING_SOON] = enabled }

    suspend fun setWeekly(enabled: Boolean) =
        context.notifDataStore.edit { it[KEY_WEEKLY] = enabled }

    suspend fun setSmart(enabled: Boolean) =
        context.notifDataStore.edit { it[KEY_SMART] = enabled }
}
