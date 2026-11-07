package com.linea.launcher.util

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast

/** Double-tap-to-lock gesture. Needs the one-time DeviceAdmin grant. */
object QuickLock {
    fun admin(context: Context) = ComponentName(context, LockAdminReceiver::class.java)

    fun isEnabled(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isAdminActive(admin(context))
    }

    fun lock(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (dpm.isAdminActive(admin(context))) dpm.lockNow()
        else requestAdmin(context)
    }

    fun requestAdmin(context: Context) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin(context))
            .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Linea uses this only to lock your screen with the double-tap gesture.")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
            .onFailure { Toast.makeText(context, "Could not open device admin", Toast.LENGTH_SHORT).show() }
    }
}
