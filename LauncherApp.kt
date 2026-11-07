package com.linea.launcher

import android.app.Application
import com.linea.launcher.data.AppRepository
import com.linea.launcher.data.LauncherPrefs

class LauncherApp : Application() {
    lateinit var repo: AppRepository
        private set
    lateinit var prefs: LauncherPrefs
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = LauncherPrefs(this)
        repo = AppRepository(this)
        repo.register()
    }
}
