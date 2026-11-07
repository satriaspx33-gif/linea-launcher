package com.linea.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "linea_prefs")

/** Launcher settings + user data. All free; no Pro gate. */
data class Settings(
    val dark: Boolean? = null,            // null = follow system
    val dynamicColor: Boolean = true,
    val showIcons: Boolean = true,
    val clockStyle: String = "minimal",   // minimal | digital | none
    val swipeUpSearch: Boolean = true,
    val quickLock: Boolean = false,
    val alphabetRight: Boolean = true,
    val iconPack: String? = null,
)

class LauncherPrefs(private val context: Context) {

    private object Keys {
        val DARK = stringPreferencesKey("dark")             // "system" | "on" | "off"
        val DYNAMIC = booleanPreferencesKey("dynamic")
        val SHOW_ICONS = booleanPreferencesKey("show_icons")
        val CLOCK = stringPreferencesKey("clock_style")
        val SWIPE_SEARCH = booleanPreferencesKey("swipe_search")
        val QUICK_LOCK = booleanPreferencesKey("quick_lock")
        val ALPHABET_RIGHT = booleanPreferencesKey("alphabet_right")
        val ICON_PACK = stringPreferencesKey("icon_pack")
        val FAVORITES = stringPreferencesKey("favorites")    // ordered JSON array
        val HIDDEN = stringSetPreferencesKey("hidden")
        val RENAMES = stringPreferencesKey("renames")        // JSON object
        val FOLDERS = stringPreferencesKey("folders")        // JSON array
    }

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            dark = when (p[Keys.DARK]) { "on" -> true; "off" -> false; else -> null },
            dynamicColor = p[Keys.DYNAMIC] ?: true,
            showIcons = p[Keys.SHOW_ICONS] ?: true,
            clockStyle = p[Keys.CLOCK] ?: "minimal",
            swipeUpSearch = p[Keys.SWIPE_SEARCH] ?: true,
            quickLock = p[Keys.QUICK_LOCK] ?: false,
            alphabetRight = p[Keys.ALPHABET_RIGHT] ?: true,
            iconPack = p[Keys.ICON_PACK],
        )
    }

    val favorites: Flow<List<String>> = context.dataStore.data.map { p ->
        p[Keys.FAVORITES]?.let { parseArray(it) } ?: emptyList()
    }
    val hidden: Flow<Set<String>> = context.dataStore.data.map { it[Keys.HIDDEN] ?: emptySet() }
    val renames: Flow<Map<String, String>> = context.dataStore.data.map { p ->
        p[Keys.RENAMES]?.let { parseMap(it) } ?: emptyMap()
    }
    val folders: Flow<List<Folder>> = context.dataStore.data.map { p ->
        val raw = p[Keys.FOLDERS] ?: return@map emptyList()
        val arr = JSONArray(raw)
        (0 until arr.length()).map { Folder.fromJson(arr.getJSONObject(it)) }
    }

    suspend fun setDark(v: String) = edit { it[Keys.DARK] = v }
    suspend fun setDynamic(v: Boolean) = edit { it[Keys.DYNAMIC] = v }
    suspend fun setShowIcons(v: Boolean) = edit { it[Keys.SHOW_ICONS] = v }
    suspend fun setClockStyle(v: String) = edit { it[Keys.CLOCK] = v }
    suspend fun setSwipeSearch(v: Boolean) = edit { it[Keys.SWIPE_SEARCH] = v }
    suspend fun setQuickLock(v: Boolean) = edit { it[Keys.QUICK_LOCK] = v }
    suspend fun setAlphabetRight(v: Boolean) = edit { it[Keys.ALPHABET_RIGHT] = v }
    suspend fun setIconPack(v: String?) = edit { p -> if (v == null) p.remove(Keys.ICON_PACK) else p[Keys.ICON_PACK] = v }

    suspend fun toggleFavorite(key: String) = edit { p ->
        val cur = p[Keys.FAVORITES]?.let { parseArray(it) }?.toMutableList() ?: mutableListOf()
        if (!cur.remove(key)) cur.add(key)
        p[Keys.FAVORITES] = JSONArray(cur).toString()
    }

    suspend fun setHidden(key: String, hide: Boolean) = edit { p ->
        val cur = (p[Keys.HIDDEN] ?: emptySet()).toMutableSet()
        if (hide) cur.add(key) else cur.remove(key)
        p[Keys.HIDDEN] = cur
    }

    suspend fun rename(key: String, label: String?) = edit { p ->
        val o = p[Keys.RENAMES]?.let { JSONObject(it) } ?: JSONObject()
        if (label.isNullOrBlank()) o.remove(key) else o.put(key, label)
        p[Keys.RENAMES] = o.toString()
    }

    suspend fun saveFolder(folder: Folder) = edit { p ->
        val arr = p[Keys.FOLDERS]?.let { JSONArray(it) } ?: JSONArray()
        var replaced = false
        for (i in 0 until arr.length()) {
            if (arr.getJSONObject(i).getString("id") == folder.id) { arr.put(i, folder.toJson()); replaced = true }
        }
        if (!replaced) arr.put(folder.toJson())
        p[Keys.FOLDERS] = arr.toString()
    }

    suspend fun deleteFolder(id: String) = edit { p ->
        val arr = p[Keys.FOLDERS]?.let { JSONArray(it) } ?: return@edit
        val keep = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("id") != id) keep.put(o)
        }
        p[Keys.FOLDERS] = keep.toString()
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
    private fun parseArray(s: String): List<String> {
        val a = JSONArray(s); return (0 until a.length()).map { a.getString(it) }
    }
    private fun parseMap(s: String): Map<String, String> {
        val o = JSONObject(s); return o.keys().asSequence().associateWith { o.getString(it) }
    }
}
