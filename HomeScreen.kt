package com.linea.launcher.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linea.launcher.data.AppInfo
import com.linea.launcher.util.AppIconImage
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onOpenSettings: () -> Unit,
    onLongPressApp: (AppInfo) -> Unit,
    onOpenFolder: (String) -> Unit,
    onQuickLock: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // letter -> first index in the visible list
    val letterIndex = remember(state.visibleApps) {
        val m = HashMap<Char, Int>()
        state.visibleApps.forEachIndexed { i, a -> m.putIfAbsent(a.firstLetter, i) }
        m
    }

    Box(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(48.dp))

            // At-a-glance: live clock + date
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                com.linea.launcher.ui.glance.AtAGlance(state.settings.clockStyle, Modifier.weight(1f))
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Favorites
            if (state.favorites.isNotEmpty()) {
                Text("Favorites", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                state.favorites.forEach { app ->
                    AppRow(app, state.settings.showIcons,
                        onClick = { vm.launch(app) }, onLongClick = { onLongPressApp(app) })
                }
                Spacer(Modifier.height(8.dp))
            }

            // Pop-up folders
            if (state.folders.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.folders.forEach { f ->
                        Text("📁 ${f.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onOpenFolder(f.id) }
                                .padding(vertical = 8.dp, horizontal = 10.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Search
            OutlinedTextField(
                value = state.query,
                onValueChange = vm::setQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Search apps") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )

            Spacer(Modifier.height(8.dp))

            // App list + alphabet scroller overlay
            Box(Modifier.fillMaxSize()) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(state.visibleApps, key = { it.key }) { app ->
                        AppRow(app, state.settings.showIcons,
                            onClick = { vm.launch(app) }, onLongClick = { onLongPressApp(app) })
                    }
                }
                if (state.query.isBlank()) {
                    AlphabetScroller(
                        onLetter = { c ->
                            letterIndex[c]?.let { idx -> scope.launch { listState.scrollToItem(idx) } }
                        },
                        onDoubleTap = { if (state.settings.quickLock) onQuickLock() },
                        modifier = Modifier.align(
                            if (state.settings.alphabetRight) Alignment.CenterEnd else Alignment.CenterStart
                        ),
                    )
                }
            }
        }
    }
}
