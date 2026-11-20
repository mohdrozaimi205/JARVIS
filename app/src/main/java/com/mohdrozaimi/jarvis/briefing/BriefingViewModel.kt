package com.mohdrozaimi.jarvis.briefing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohdrozaimi.jarvis.ai.GeminiClient
import com.mohdrozaimi.jarvis.ai.buildNutritionPrompt
import com.mohdrozaimi.jarvis.air.AirSource
import com.mohdrozaimi.jarvis.calendar.CalendarSource
import com.mohdrozaimi.jarvis.data.AppSettings
import com.mohdrozaimi.jarvis.data.LocationProvider
import com.mohdrozaimi.jarvis.data.SettingsRepo
import com.mohdrozaimi.jarvis.health.HealthSnapshot
import com.mohdrozaimi.jarvis.health.HealthSource
import com.mohdrozaimi.jarvis.news.NewsSource
import com.mohdrozaimi.jarvis.route.RouteSource
import com.mohdrozaimi.jarvis.route.parseDistanceCommand
import com.mohdrozaimi.jarvis.voice.Speaker
import com.mohdrozaimi.jarvis.weather.WeatherSource
import com.mohdrozaimi.jarvis.weather.matchLocation
import android.location.Location
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class UiState(
    val loading: Boolean = false,
    val briefing: Briefing? = null,
    val routeText: String? = null,
    val message: String? = null,
)

class BriefingViewModel(app: Application) : AndroidViewModel(app) {

    val settingsRepo = SettingsRepo(app)
    val speaker = Speaker(app)

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    fun runBriefing(speak: Boolean) {
        if (_state.value.loading) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            val s = settingsRepo.flow.first()
            speaker.preferredVoice = s.voiceName.ifBlank { null }
            val b = compose(gather(s))
            _state.update { it.copy(loading = false, briefing = b) }
            if (speak) speaker.speak(b)
        }
    }

    /** Teks dari mikrofon: "good morning" → taklimat; "jarak dari A ke B" → jarak. */
    fun handleVoice(text: String) {
        val t = text.lowercase()
        if ("good morning" in t || "selamat pagi" in t || "taklimat" in t) {
            runBriefing(speak = true)
            return
        }
        val q = parseDistanceCommand(text)
        if (q == null) {
            _state.update { it.copy(message = "Maaf, saya tak faham: \"$text\"") }
            speaker.speakMs("Maaf, saya tak faham arahan itu.")
            return
        }
        viewModelScope.launch {
            val s = settingsRepo.flow.first()
            speaker.preferredVoice = s.voiceName.ifBlank { null }
            if (s.orsKey.isBlank()) {
                val msg = "Masukkan kunci OpenRouteService dalam Tetapan dahulu."
                _state.update { it.copy(message = msg) }
                speaker.speakMs(msg)
                return@launch
            }
            _state.update { it.copy(message = "Mengira jarak ${q.from} ke ${q.to}…") }
            val r = safe { RouteSource(s.orsKey).route(q) }
            if (r == null) {
                val msg = "Maaf, laluan ${q.from} ke ${q.to} tidak dijumpai."
                _state.update { it.copy(message = msg) }
                speaker.speakMs(msg)
            } else {
                val km = r.km.toString().replace('.', ',')
                _state.update {
                    it.copy(message = null, routeText = "${q.from} → ${q.to}\n$km km · ~${r.minutes} minit")
                }
                speaker.speakMs(
                    "Jarak dari ${q.from} ke ${q.to} ialah $km kilometer, lebih kurang ${r.minutes} minit memandu.",
                )
            }
        }
    }

    private suspend fun gather(s: AppSettings): BriefingInput {
        val ctx = getApplication<Application>()
        val loc = safe { LocationProvider.current(ctx) }
        return coroutineScope {
            val weather = async { safe { WeatherSource().today(resolvePlace(s, loc)) } }
            val air = async { safe { loc?.let { AirSource().now(it.latitude, it.longitude) } } }
            val meetings = async { safe { CalendarSource(ctx).today() } }
            val news = async { safe { NewsSource().top() } }
            val health = safe { HealthSource(ctx).snapshot() }
            val nutrition = if (s.geminiKey.isNotBlank()) {
                safe {
                    GeminiClient(s.geminiKey).ask(
                        buildNutritionPrompt(health ?: HealthSnapshot(null, null, null)),
                    )
                }
            } else null
            BriefingInput(
                date = LocalDate.now(),
                weather = weather.await(),
                air = air.await(),
                meetings = meetings.await(),
                health = health,
                nutrition = nutrition,
                news = news.await(),
            )
        }
    }

    private suspend fun resolvePlace(s: AppSettings, loc: Location?): String {
        if (s.metPlace.isNotBlank()) return s.metPlace
        val district = LocationProvider.district(getApplication(), loc)
        val names = safe { WeatherSource().districtNames() } ?: emptyList()
        return matchLocation(district, names)
    }

    override fun onCleared() {
        speaker.shutdown()
    }
}

/** Jalankan satu sumber; sebarang ralat → null supaya taklimat tetap keluar. */
suspend fun <T> safe(block: suspend () -> T): T? = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    null
}
