package com.linea.launcher.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.linea.launcher.LauncherApp
import com.linea.launcher.data.AppInfo
import com.linea.launcher.data.Folder
import com.linea.launcher.data.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeState(
    val visibleApps: List<AppInfo> = emptyList(),
    val favorites: List<AppInfo> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val settings: Settings = Settings(),
    val query: String = "",
)

private data class AppData(
    val apps: List<AppInfo>,
    val favKeys: List<String>,
    val hidden: Set<String>,
)
private data class UserData(
    val renames: Map<String, String>,
    val folders: List<Folder>,
    val settings: Settings,
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as LauncherApp).repo
    private val prefs = (app as LauncherApp).prefs

    private val queryFlow = MutableStateFlow("")

    init { refresh() }

    private val appData = combine(repo.apps, prefs.favorites, prefs.hidden, ::AppData)
    private val userData = combine(prefs.renames, prefs.folders, prefs.settings, ::UserData)

    val state: StateFlow<HomeState> =
        combine(appData, userData, queryFlow) { a, u, query ->
            val named = a.apps.map { app -> u.renames[app.key]?.let { app.copy(label = it) } ?: app }
            val byKey = named.associateBy { it.key }
            val visible = named
                .filter { it.key !in a.hidden }
                .filter { query.isBlank() || it.label.contains(query, ignoreCase = true) }
                .sortedBy { it.sortKey.lowercase() }
            val favs = a.favKeys.mapNotNull { byKey[it] }
            HomeState(visible, favs, u.folders, u.settings, query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    fun setQuery(q: String) { queryFlow.value = q }
    fun launch(a: AppInfo) = repo.launch(a)
    fun appInfo(a: AppInfo) = repo.openAppInfo(a)
    fun uninstall(a: AppInfo) = repo.uninstall(a)
    fun toggleFavorite(a: AppInfo) = viewModelScope.launch { prefs.toggleFavorite(a.key) }
    fun hide(a: AppInfo) = viewModelScope.launch { prefs.setHidden(a.key, true) }
    fun rename(a: AppInfo, name: String) = viewModelScope.launch { prefs.rename(a.key, name) }
    fun saveFolder(f: Folder) = viewModelScope.launch { prefs.saveFolder(f) }
    fun deleteFolder(id: String) = viewModelScope.launch { prefs.deleteFolder(id) }
    fun refresh() = viewModelScope.launch { repo.refresh() }
}
