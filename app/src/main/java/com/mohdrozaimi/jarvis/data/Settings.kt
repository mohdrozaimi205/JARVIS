package com.mohdrozaimi.jarvis.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "jarvis_settings")

data class AppSettings(
    val geminiKey: String = "",
    val orsKey: String = "",
    val alarmEnabled: Boolean = false,
    val alarmHour: Int = 6,
    val alarmMinute: Int = 30,
    /** Kosong = automatik ikut lokasi GPS. */
    val metPlace: String = "",
    /** Kosong = suara BM lalai enjin TTS. */
    val voiceName: String = "",
)

class SettingsRepo(private val ctx: Context) {
    private object K {
        val GEMINI = stringPreferencesKey("gemini_key")
        val ORS = stringPreferencesKey("ors_key")
        val ALARM_ON = booleanPreferencesKey("alarm_on")
        val ALARM_H = intPreferencesKey("alarm_h")
        val ALARM_M = intPreferencesKey("alarm_m")
        val MET = stringPreferencesKey("met_place")
        val VOICE = stringPreferencesKey("voice")
    }

    val flow: Flow<AppSettings> = ctx.applicationContext.dataStore.data.map { p ->
        AppSettings(
            geminiKey = p[K.GEMINI] ?: "",
            orsKey = p[K.ORS] ?: "",
            alarmEnabled = p[K.ALARM_ON] ?: false,
            alarmHour = p[K.ALARM_H] ?: 6,
            alarmMinute = p[K.ALARM_M] ?: 30,
            metPlace = p[K.MET] ?: "",
            voiceName = p[K.VOICE] ?: "",
        )
    }

    suspend fun save(s: AppSettings) {
        ctx.applicationContext.dataStore.edit { p ->
            p[K.GEMINI] = s.geminiKey.trim()
            p[K.ORS] = s.orsKey.trim()
            p[K.ALARM_ON] = s.alarmEnabled
            p[K.ALARM_H] = s.alarmHour
            p[K.ALARM_M] = s.alarmMinute
            p[K.MET] = s.metPlace.trim()
            p[K.VOICE] = s.voiceName
        }
    }
}
