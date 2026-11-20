package com.mohdrozaimi.jarvis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohdrozaimi.jarvis.ui.Fajar

/** Penerangan privasi yang diminta oleh Health Connect. */
class PrivacyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(Modifier.fillMaxSize().background(Fajar.Bg).padding(24.dp)) {
                Text("Privasi JARVIS", color = Fajar.Copper, fontSize = 24.sp)
                Text(
                    "JARVIS hanya MEMBACA langkah, tidur dan nadi rehat dari Health Connect " +
                        "untuk taklimat pagi anda. Data tidak disimpan di pelayan. Ringkasan data " +
                        "dihantar ke Google Gemini hanya untuk menjana cadangan pemakanan, dan hanya " +
                        "jika anda memasukkan kunci Gemini dalam Tetapan.",
                    color = Fajar.Cream,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}
