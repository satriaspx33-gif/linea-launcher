package com.linea.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.linea.launcher.data.AppInfo
import com.linea.launcher.data.Folder
import com.linea.launcher.ui.folder.FolderPopup
import com.linea.launcher.ui.home.HomeScreen
import com.linea.launcher.ui.home.HomeViewModel
import com.linea.launcher.ui.settings.SettingsActivity
import com.linea.launcher.ui.theme.LineaTheme
import com.linea.launcher.util.QuickLock
import java.util.UUID

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LauncherRoot() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun LauncherRoot() {
        val vm: HomeViewModel = viewModel()
        val app = application as LauncherApp
        val settings by app.prefs.settings.collectAsState(initial = com.linea.launcher.data.Settings())
        val appsState by app.repo.apps.collectAsState()

        var sheetApp by remember { mutableStateOf<AppInfo?>(null) }
        var renameApp by remember { mutableStateOf<AppInfo?>(null) }
        var renameText by remember { mutableStateOf("") }
        var openFolderId by remember { mutableStateOf<String?>(null) }
        var pickForFolder by remember { mutableStateOf<AppInfo?>(null) }

        LineaTheme(darkOverride = settings.dark, dynamicColor = settings.dynamicColor) {
            Surface(color = Color.Transparent) {
                HomeScreen(
                    vm = vm,
                    onOpenSettings = { startActivity(Intent(this, SettingsActivity::class.java)) },
                    onLongPressApp = { sheetApp = it },
                    onOpenFolder = { openFolderId = it },
                    onQuickLock = { QuickLock.lock(this) },
                )
            }

            // Long-press action sheet
            sheetApp?.let { app0 ->
                val sheet = rememberModalBottomSheetState()
                ModalBottomSheet(onDismissRequest = { sheetApp = null }, sheetState = sheet) {
                    Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                        Text(app0.label, style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp))
                        ListItem(headlineContent = { Text("Add / remove favorite") },
                            modifier = clickRow { vm.toggleFavorite(app0); sheetApp = null })
                        ListItem(headlineContent = { Text("Hide from list") },
                            modifier = clickRow { vm.hide(app0); sheetApp = null })
                        ListItem(headlineContent = { Text("Rename") },
                            modifier = clickRow { renameApp = app0; renameText = app0.label; sheetApp = null })
                        ListItem(headlineContent = { Text("Add to new pop-up folder") },
                            modifier = clickRow {
                                vm.saveFolder(Folder(UUID.randomUUID().toString(), app0.label, listOf(app0.key)))
                                sheetApp = null
                            })
                        ListItem(headlineContent = { Text("App info") },
                            modifier = clickRow { vm.appInfo(app0); sheetApp = null })
                        ListItem(headlineContent = { Text("Uninstall") },
                            modifier = clickRow { vm.uninstall(app0); sheetApp = null })
                    }
                }
            }

            // Rename dialog
            renameApp?.let { app0 ->
                AlertDialog(
                    onDismissRequest = { renameApp = null },
                    title = { Text("Rename ${app0.label}") },
                    text = {
                        OutlinedTextField(value = renameText, onValueChange = { renameText = it },
                            singleLine = true)
                    },
                    confirmButton = {
                        TextButton(onClick = { vm.rename(app0, renameText); renameApp = null }) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { vm.rename(app0, null); renameApp = null }) { Text("Reset") }
                    },
                )
            }

            // Folder popup
            openFolderId?.let { id ->
                val state by vm.state.collectAsState()
                val folder = state.folders.firstOrNull { it.id == id }
                val byKey = appsState.associateBy { it.key }
                if (folder != null) {
                    FolderPopup(
                        folder = folder,
                        appsByKey = byKey,
                        onLaunch = { vm.launch(it); openFolderId = null },
                        onDelete = { vm.deleteFolder(id); openFolderId = null },
                        onDismiss = { openFolderId = null },
                    )
                } else openFolderId = null
            }
        }
    }

    private fun clickRow(onClick: () -> Unit): Modifier =
        Modifier.fillMaxWidth().clickable(onClick = onClick)
}
