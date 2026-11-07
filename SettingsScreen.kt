package com.linea.launcher.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.linea.launcher.data.LauncherPrefs
import com.linea.launcher.data.Settings
import com.linea.launcher.util.QuickLock
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(prefs: LauncherPrefs, settings: Settings, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Linea settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState())) {

            SectionHeader("Appearance")
            ToggleRow("Show app icons", settings.showIcons) { v -> scope.launch { prefs.setShowIcons(v) } }
            ToggleRow("Dynamic color (Material You)", settings.dynamicColor) { v -> scope.launch { prefs.setDynamic(v) } }
            ToggleRow("Dark theme", settings.dark == true) { v -> scope.launch { prefs.setDark(if (v) "on" else "off") } }
            ListItem(
                headlineContent = { Text("Follow system theme") },
                modifier = Modifier.padding(horizontal = 4.dp),
                trailingContent = {
                    Switch(checked = settings.dark == null, onCheckedChange = { on ->
                        scope.launch { prefs.setDark(if (on) "system" else "off") }
                    })
                },
            )
            ClockStyleRow(settings.clockStyle) { v -> scope.launch { prefs.setClockStyle(v) } }

            SectionHeader("Layout")
            ToggleRow("Alphabet on right side", settings.alphabetRight) { v -> scope.launch { prefs.setAlphabetRight(v) } }
            ToggleRow("Swipe up to search", settings.swipeUpSearch) { v -> scope.launch { prefs.setSwipeSearch(v) } }

            SectionHeader("Gestures")
            ToggleRow("Double-tap alphabet to lock", settings.quickLock) { v ->
                scope.launch { prefs.setQuickLock(v) }
                if (v && !QuickLock.isEnabled(context)) QuickLock.requestAdmin(context)
            }

            SectionHeader("About")
            ListItem(headlineContent = { Text("Linea Launcher 1.0.0") },
                supportingContent = { Text("Minimalist, ad-free, every feature unlocked.") })
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp))
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        modifier = Modifier.padding(horizontal = 4.dp),
        trailingContent = { Switch(checked = checked, onCheckedChange = onChange) },
    )
}

@Composable
private fun ClockStyleRow(current: String, onPick: (String) -> Unit) {
    val styles = listOf("minimal", "digital", "none")
    ListItem(
        headlineContent = { Text("Clock style") },
        supportingContent = { Text("Current: $current") },
        modifier = Modifier.padding(horizontal = 4.dp),
        trailingContent = {
            Text(
                text = styles[(styles.indexOf(current) + 1) % styles.size],
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
                    .clickable { onPick(styles[(styles.indexOf(current) + 1) % styles.size]) },
            )
        },
    )
}
