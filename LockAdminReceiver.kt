package com.linea.launcher.util

import android.app.admin.DeviceAdminReceiver

/** Device-admin receiver used only to lock the screen (force-lock policy). */
class LockAdminReceiver : DeviceAdminReceiver()
