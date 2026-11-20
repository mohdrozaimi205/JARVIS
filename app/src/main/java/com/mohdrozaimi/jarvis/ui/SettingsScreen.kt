package com.mohdrozaimi.jarvis.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.mohdrozaimi.jarvis.alarm.MorningAlarm
import com.mohdrozaimi.jarvis.briefing.BriefingViewModel
import com.mohdrozaimi.jarvis.data.AppSettings
import com.mohdrozaimi.jarvis.health.HealthSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: BriefingViewModel, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var loaded by remember { mutableStateOf(false) }
    var s by remember { mutableStateOf(AppSettings()) }
    var timeText by remember { mutableStateOf("06:30") }
    LaunchedEffect(Unit) {
        s = vm.settingsRepo.flow.first()
        timeText = "%02d:%02d".format(s.alarmHour, s.alarmMinute)
        loaded = true
    }

    val toast = { msg: String -> Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show() }
    val locationPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        toast(if (it.values.any { ok -> ok }) "Lokasi dibenarkan" else "Lokasi ditolak")
    }
    val calendarPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        toast(if (it) "Kalendar dibenarkan" else "Kalendar ditolak")
    }
    val notifPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        toast(if (it) "Notifikasi dibenarkan" else "Notifikasi ditolak")
    }
    val healthPerm = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { granted -> toast("Health Connect: ${granted.size}/3 dibenarkan") }

    val open = { url: String ->
        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        Unit
    }

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Tetapan", color = Fajar.Cream, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        if (!loaded) return@Column

        Section("1 · Kebenaran")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Lokasi") {
                locationPerm.launch(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                )
            }
            Chip("Kalendar") { calendarPerm.launch(Manifest.permission.READ_CALENDAR) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Notifikasi") {
                if (Build.VERSION.SDK_INT >= 33) {
                    notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    toast("Notifikasi sudah dibenarkan")
                }
            }
            Chip("Kesihatan") {
                when (HealthSource.status(ctx)) {
                    HealthConnectClient.SDK_AVAILABLE -> healthPerm.launch(HealthSource.PERMISSIONS)
                    HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                        open("market://details?id=com.google.android.apps.healthdata")
                    else -> toast("Health Connect tidak disokong pada peranti ini")
                }
            }
        }

        Section("2 · Kunci API (percuma)")
        OutlinedTextField(
            value = s.geminiKey, onValueChange = { s = s.copy(geminiKey = it) },
            label = { Text("Kunci Gemini") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = { open("https://aistudio.google.com/app/apikey") }) {
            Text("Dapatkan kunci Gemini →", color = Fajar.Copper)
        }
        OutlinedTextField(
            value = s.orsKey, onValueChange = { s = s.copy(orsKey = it) },
            label = { Text("Kunci OpenRouteService") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = { open("https://openrouteservice.org/dev/#/signup") }) {
            Text("Dapatkan kunci OpenRouteService →", color = Fajar.Copper)
        }

        Section("3 · Kejut pagi")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Hidupkan kejut pagi", color = Fajar.Cream, modifier = Modifier.weight(1f))
            Switch(
                checked = s.alarmEnabled,
                onCheckedChange = { s = s.copy(alarmEnabled = it) },
                colors = SwitchDefaults.colors(checkedTrackColor = Fajar.Copper),
            )
        }
        OutlinedTextField(
            value = timeText, onValueChange = { timeText = it },
            label = { Text("Masa (JJ:MM, contoh 06:30)") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Section("4 · Lokasi cuaca MET")
        OutlinedTextField(
            value = s.metPlace, onValueChange = { s = s.copy(metPlace = it) },
            label = { Text("Kosongkan = automatik (GPS)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        )

        Section("5 · Suara Bahasa Melayu")
        VoicePicker(
            voices = vm.speaker.malayVoices(),
            selected = s.voiceName,
            onPick = { s = s.copy(voiceName = it) },
        )
        TextButton(onClick = {
            vm.speaker.preferredVoice = s.voiceName.ifBlank { null }
            vm.speaker.speakMs("Selamat pagi. Ini suara JARVIS.")
        }) { Text("▶ Cuba suara", color = Fajar.Copper) }

        Button(
            onClick = {
                val parts = timeText.trim().split(":")
                val h = parts.getOrNull(0)?.toIntOrNull()
                val m = parts.getOrNull(1)?.toIntOrNull()
                if (h == null || m == null || h !in 0..23 || m !in 0..59) {
                    toast("Masa tidak sah. Guna format 06:30")
                    return@Button
                }
                val updated = s.copy(alarmHour = h, alarmMinute = m)
                scope.launch {
                    vm.settingsRepo.save(updated)
                    MorningAlarm.schedule(ctx, updated)
                    toast("Tetapan disimpan")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Fajar.Copper, contentColor = Fajar.Bg),
        ) { Text("Simpan", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun Section(title: String) {
    Text(title.uppercase(), color = Fajar.Copper, fontSize = 12.sp, letterSpacing = 2.sp,
        modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun Chip(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, border = BorderStroke(1.dp, Fajar.Copper)) {
        Text(label, color = Fajar.Cream)
    }
}

@Composable
private fun VoicePicker(voices: List<String>, selected: String, onPick: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, border = BorderStroke(1.dp, Fajar.Line)) {
            Text(selected.ifBlank { "Lalai" }, color = Fajar.Cream)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Lalai") }, onClick = { onPick(""); expanded = false })
            if (voices.isEmpty()) {
                DropdownMenuItem(text = { Text("Tiada suara BM — pasang di Tetapan TTS") }, onClick = { expanded = false })
            }
            voices.forEach { v ->
                DropdownMenuItem(text = { Text(v) }, onClick = { onPick(v); expanded = false })
            }
        }
    }
}
