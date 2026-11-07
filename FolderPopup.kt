package com.linea.launcher.ui.folder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.linea.launcher.data.AppInfo
import com.linea.launcher.data.Folder
import com.linea.launcher.util.AppIconImage

/** Pop-up folder: a grid of apps. Widget slot is a TODO (AppWidgetHost). */
@Composable
fun FolderPopup(
    folder: Folder,
    appsByKey: Map<String, AppInfo>,
    onLaunch: (AppInfo) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val items = folder.items.mapNotNull { appsByKey[it] }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = { TextButton(onClick = onDelete) { Text("Delete") } },
        title = { Text(folder.name) },
        text = {
            if (items.isEmpty()) {
                Text("This pop-up is empty. Long-press an app and choose \"Add to folder\".")
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxWidth()) {
                    items(items) { app ->
                        Column(
                            modifier = Modifier.padding(8.dp).clickable { onLaunch(app) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            AppIconImage(app.icon, Modifier.size(44.dp).clip(CircleShape), app.label)
                            Text(app.label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        },
    )
}
