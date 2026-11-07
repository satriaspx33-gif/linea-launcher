package com.linea.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/** Queries launchable apps via LauncherApps and reacts to install/remove. */
class AppRepository(private val context: Context) {

    private val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(p: String?, u: UserHandle?) { refreshBlocking() }
        override fun onPackageAdded(p: String?, u: UserHandle?) { refreshBlocking() }
        override fun onPackageChanged(p: String?, u: UserHandle?) { refreshBlocking() }
        override fun onPackagesAvailable(p: Array<out String>?, u: UserHandle?, r: Boolean) { refreshBlocking() }
        override fun onPackagesUnavailable(p: Array<out String>?, u: UserHandle?, r: Boolean) { refreshBlocking() }
    }

    fun register() = launcherApps.registerCallback(callback)
    fun unregister() = launcherApps.unregisterCallback(callback)

    private fun refreshBlocking() {
        _apps.value = loadInternal()
    }

    suspend fun refresh() = withContext(Dispatchers.IO) {
        _apps.value = loadInternal()
    }

    private fun loadInternal(): List<AppInfo> {
        val self = context.packageName
        val user: UserHandle = Process.myUserHandle()
        return launcherApps.getActivityList(null, user)
            .asSequence()
            .filter { it.componentName.packageName != self }
            .map {
                AppInfo(
                    packageName = it.componentName.packageName,
                    className = it.componentName.className,
                    label = it.label?.toString().orEmpty(),
                    icon = runCatching { it.getBadgedIcon(0) }.getOrNull(),
                )
            }
            .sortedBy { it.sortKey.lowercase() }
            .toList()
    }

    fun launch(app: AppInfo) {
        val user = Process.myUserHandle()
        val component = ComponentName(app.packageName, app.className)
        runCatching { launcherApps.startMainActivity(component, user, null, null) }
    }

    fun openAppInfo(app: AppInfo) {
        val user = Process.myUserHandle()
        runCatching { launcherApps.startAppDetailsActivity(
            ComponentName(app.packageName, app.className), user, null, null) }
    }

    fun uninstall(app: AppInfo) {
        val intent = Intent(Intent.ACTION_DELETE)
            .setData(android.net.Uri.parse("package:${app.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
