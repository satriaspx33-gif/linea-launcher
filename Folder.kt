package com.linea.launcher.data

import org.json.JSONArray
import org.json.JSONObject

/** A pop-up folder. Unlimited in Linea (no paywall). */
data class Folder(
    val id: String,
    val name: String,
    val items: List<String>,      // app keys "pkg/class"
    val widgetId: Int? = null,    // hosted widget, optional
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("items", JSONArray(items))
        widgetId?.let { put("widgetId", it) }
    }
    companion object {
        fun fromJson(o: JSONObject): Folder {
            val arr = o.optJSONArray("items") ?: JSONArray()
            val items = (0 until arr.length()).map { arr.getString(it) }
            val wid = if (o.has("widgetId")) o.getInt("widgetId") else null
            return Folder(o.getString("id"), o.optString("name", "Folder"), items, wid)
        }
    }
}
