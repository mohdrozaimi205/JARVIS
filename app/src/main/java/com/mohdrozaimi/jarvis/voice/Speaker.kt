package com.mohdrozaimi.jarvis.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.mohdrozaimi.jarvis.briefing.Briefing
import java.util.Locale

/**
 * Suara JARVIS: sapaan dalam English British, kemudian isi dalam BM.
 * Satu enjin TTS; suara BM boleh dipilih dalam Tetapan.
 */
class Speaker(ctx: Context) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(ctx.applicationContext, this)
    private var ready = false
    private var pending: (() -> Unit)? = null
    private var queuedBody: List<String> = emptyList()
    private val msLocale = Locale("ms", "MY")

    var preferredVoice: String? = null

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (!ready) return
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                if (utteranceId == GREET) speakBody()
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                if (utteranceId == GREET) speakBody()
            }
        })
        pending?.invoke()
        pending = null
    }

    fun speak(b: Briefing) = whenReady {
        queuedBody = b.bodyMs
        tts.stop()
        tts.language = Locale.UK
        tts.speak(b.greetingEn, TextToSpeech.QUEUE_FLUSH, null, GREET)
    }

    fun speakMs(text: String) = whenReady {
        tts.stop()
        useMalay()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ms")
    }

    fun stop() {
        if (ready) tts.stop()
    }

    /** Nama suara Bahasa Melayu yang ada dalam telefon. */
    fun malayVoices(): List<String> = runCatching {
        tts.voices?.filter { it.locale.language == "ms" }?.map { it.name }?.sorted()
    }.getOrNull() ?: emptyList()

    fun shutdown() = tts.shutdown()

    private fun speakBody() {
        useMalay()
        queuedBody.forEachIndexed { i, line ->
            tts.speak(line, TextToSpeech.QUEUE_ADD, null, "body$i")
        }
    }

    private fun useMalay() {
        val voice = preferredVoice?.let { name -> tts.voices?.firstOrNull { it.name == name } }
        if (voice != null) tts.voice = voice else tts.language = msLocale
    }

    private fun whenReady(block: () -> Unit) {
        if (ready) block() else pending = block
    }

    private companion object {
        const val GREET = "greet"
    }
}
