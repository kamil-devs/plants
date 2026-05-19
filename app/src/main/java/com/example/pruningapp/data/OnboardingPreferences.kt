package com.example.pruningapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_prefs")

class OnboardingPreferences(private val context: Context) {

    val hasSeenOnboarding: Flow<Boolean> = context.onboardingDataStore.data.map { prefs ->
        prefs[KEY_SEEN] ?: false
    }

    suspend fun setOnboardingSeen() {
        context.onboardingDataStore.edit { it[KEY_SEEN] = true }
    }

    companion object {
        private val KEY_SEEN = booleanPreferencesKey("onboarding_seen")
    }
}
