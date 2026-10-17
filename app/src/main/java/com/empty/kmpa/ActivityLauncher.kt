package com.empty.kmpa

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
object ActivityLauncher {
    private const val TAG = "PrivateActivity"

    fun launch(context: Context, component: ComponentName, extras: Bundle?) {
        check(Shizuku.pingBinder()) { "Shizuku is not running" }
        check(Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            "Shizuku permission not granted"
        }
        grantWriteSecureSettings(context.packageName)

        val assistantKey = "assistant"
        val originalAssistant = Settings.Secure.getString(context.contentResolver, assistantKey)
        try {
            val changed = Settings.Secure.putString(
                context.contentResolver,
                assistantKey,
                component.flattenToString()
            )
            if (!changed) throw IllegalStateException("Could not set temporary Assistant component")

            val searchManager = context.getSystemService(Context.SEARCH_SERVICE) as SearchManager
            val payload = extras ?: Bundle()
            try {
                HiddenApiBypass.invoke(SearchManager::class.java, searchManager, "launchAssist", payload)
            } catch (first: Throwable) {
                Log.w(TAG, "launchAssist unavailable; trying startAssist", first)
                HiddenApiBypass.invoke(SearchManager::class.java, searchManager, "startAssist", payload)
            }
            android.os.Handler(context.mainLooper).postDelayed({
                Settings.Secure.putString(context.contentResolver, assistantKey, originalAssistant)
            }, 700)
        } catch (t: Throwable) {
            Settings.Secure.putString(context.contentResolver, assistantKey, originalAssistant)
            throw IllegalStateException(
                "Assistant launch failed. The target may be blocked by this Android/OEM version: ${t.message}", t
            )
        }
    }

    private fun grantWriteSecureSettings(packageName: String) {
        try {
            val binder = SystemServiceHelper.getSystemService("package")
            val wrapped = ShizukuBinderWrapper(binder)
            val stub = Class.forName("android.content.pm.IPackageManager\$Stub")
            val packageManagerInterface = HiddenApiBypass.invoke(stub, null, "asInterface", wrapped)
                ?: throw IllegalStateException("Could not access package manager binder")
            HiddenApiBypass.invoke(
                packageManagerInterface.javaClass,
                packageManagerInterface,
                "grantRuntimePermission",
                packageName,
                android.Manifest.permission.WRITE_SECURE_SETTINGS,
                0
            )
        } catch (t: Throwable) {
            Log.w(TAG, "Unable to grant WRITE_SECURE_SETTINGS via Shizuku; continuing to check settings access", t)
        }
    }
}
