package com.mohdrozaimi.jarvis

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.mohdrozaimi.jarvis.briefing.BriefingViewModel
import com.mohdrozaimi.jarvis.ui.BriefingScreen
import com.mohdrozaimi.jarvis.ui.Fajar
import com.mohdrozaimi.jarvis.ui.JarvisTheme
import com.mohdrozaimi.jarvis.ui.SettingsScreen

class MainActivity : ComponentActivity() {

    private val vm: BriefingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { JarvisTheme { JarvisApp(vm) } }
        handle(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    private fun handle(i: Intent?) {
        if (i?.getBooleanExtra(EXTRA_AUTO_BRIEF, false) == true) {
            i.removeExtra(EXTRA_AUTO_BRIEF)
            vm.runBriefing(speak = true)
        }
    }

    companion object {
        const val EXTRA_AUTO_BRIEF = "auto_brief"
    }
}

@Composable
private fun JarvisApp(vm: BriefingViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        containerColor = Fajar.Bg,
        bottomBar = {
            NavigationBar(containerColor = Fajar.Panel) {
                listOf("◐" to "Taklimat", "⚙" to "Tetapan").forEachIndexed { i, (icon, label) ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Text(icon, fontSize = 20.sp) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Fajar.Bg,
                            selectedTextColor = Fajar.Copper,
                            indicatorColor = Fajar.Copper,
                            unselectedIconColor = Fajar.Muted,
                            unselectedTextColor = Fajar.Muted,
                        ),
                    )
                }
            }
        },
    ) { pad ->
        val m = Modifier.fillMaxSize().padding(pad)
        if (tab == 0) BriefingScreen(vm, m) else SettingsScreen(vm, m)
    }
}
