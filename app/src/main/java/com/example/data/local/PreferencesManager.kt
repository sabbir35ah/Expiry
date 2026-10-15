package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "expiry_user_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        private val KEY_PIN_CODE = stringPreferencesKey("pin_code")
        private val KEY_REDACT_SENSITIVE = booleanPreferencesKey("redact_sensitive_numbers")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_PREMIUM_ACTIVE = booleanPreferencesKey("premium_tier_active")
        private val KEY_SEEDED_INITIAL = booleanPreferencesKey("seeded_initial_documents")
    }

    val isPinEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_PIN_ENABLED] ?: false
    }

    val pinCode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PIN_CODE] ?: "1234"
    }

    val redactSensitive: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_REDACT_SENSITIVE] ?: true
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val isPremiumActive: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_PREMIUM_ACTIVE] ?: false
    }

    val isInitialSeeded: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SEEDED_INITIAL] ?: false
    }

    suspend fun setPinLock(enabled: Boolean, pin: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_PIN_ENABLED] = enabled
            prefs[KEY_PIN_CODE] = pin
        }
    }

    suspend fun setRedactSensitive(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REDACT_SENSITIVE] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setPremiumActive(active: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PREMIUM_ACTIVE] = active
        }
    }

    suspend fun setInitialSeeded(seeded: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SEEDED_INITIAL] = seeded
        }
    }
}
