package com.linea.launcher.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.linea.launcher.LauncherApp
import com.linea.launcher.data.Settings
import com.linea.launcher.ui.theme.LineaTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as LauncherApp
        setContent {
            val settings by app.prefs.settings.collectAsState(initial = Settings())
            LineaTheme(darkOverride = settings.dark, dynamicColor = settings.dynamicColor) {
                SettingsScreen(prefs = app.prefs, settings = settings, onBack = { finish() })
            }
        }
    }
}
