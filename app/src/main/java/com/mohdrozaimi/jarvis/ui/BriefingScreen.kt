package com.mohdrozaimi.jarvis.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohdrozaimi.jarvis.briefing.BriefingViewModel
import com.mohdrozaimi.jarvis.briefing.Card
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PLACEHOLDER = listOf(
    Card("Cuaca", "—", "Tekan ▶ Taklimat"),
    Card("IPU (anggaran)", "—", ""),
    Card("Mesyuarat", "—", ""),
    Card("Kesihatan", "—", ""),
    Card("Pemakanan", "", ""),
    Card("Berita", "", ""),
)

@Composable
fun BriefingScreen(vm: BriefingViewModel, modifier: Modifier = Modifier) {
    val state by vm.state.collectAsStateWithLifecycle()

    val speech = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let(vm::handleVoice)
    }
    val listen = {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ms-MY")
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "Cakap \"good morning\" atau \"jarak dari Ipoh ke KL\"")
        try {
            speech.launch(i)
        } catch (e: ActivityNotFoundException) {
            vm.handleVoice("")
        }
    }

    Column(
        modifier
            .background(Brush.verticalGradient(listOf(Fajar.PanelHi, Fajar.Bg, Fajar.Bg)))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Kepala: jenama + tarikh + butang
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("J · A · R · V · I · S", color = Fajar.Copper, fontSize = 12.sp, letterSpacing = 3.sp)
                Text(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMM", Locale("ms", "MY"))),
                    color = Fajar.Cream,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OutlinedButton(
                onClick = listen,
                border = BorderStroke(1.dp, Fajar.Copper),
            ) { Text("🎙", fontSize = 18.sp) }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { vm.runBriefing(speak = true) },
                colors = ButtonDefaults.buttonColors(containerColor = Fajar.Copper, contentColor = Fajar.Bg),
            ) { Text("▶ Taklimat", fontWeight = FontWeight.Bold) }
        }

        Spacer(Modifier.height(8.dp))
        if (state.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = Fajar.Copper, trackColor = Fajar.Panel)
        } else {
            Spacer(Modifier.height(4.dp))
        }
        state.message?.let {
            Text(it, color = Fajar.Ember, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp), maxLines = 2)
        }
        Spacer(Modifier.height(10.dp))

        // Grid kad tanpa scroll
        val cards = buildList {
            addAll(state.briefing?.cards ?: PLACEHOLDER)
            state.routeText?.let { add(Card("Jarak", "", it)) }
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val cols = if (maxWidth > 600.dp) 3 else 2
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                cards.chunked(cols).forEach { row ->
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { InfoCard(it, Modifier.weight(1f).fillMaxHeight()) }
                        repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(card: Card, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Fajar.Panel,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Fajar.Line),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(card.title.uppercase(), color = Fajar.Copper, fontSize = 11.sp, letterSpacing = 2.sp, maxLines = 1)
            if (card.value.isNotEmpty()) {
                Text(
                    card.value,
                    color = Fajar.Cream,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                card.detail,
                color = Fajar.Muted,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                maxLines = 8,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
