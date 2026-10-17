package com.empty.kmpa.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

object ShizukuHelper {
    const val PERMISSION_REQUEST_CODE = 7412

    val isRunning: Boolean
        get() = Shizuku.pingBinder()

    val hasPermission: Boolean
        get() = isRunning && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    fun requestPermission() = Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
}