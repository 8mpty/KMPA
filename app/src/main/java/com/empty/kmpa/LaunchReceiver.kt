package com.empty.kmpa

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import rikka.shizuku.Shizuku

class LaunchReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != LaunchContract.ACTION_LAUNCH) return
        val pkg = intent.getStringExtra(LaunchContract.EXTRA_PACKAGE)
        val cls = intent.getStringExtra(LaunchContract.EXTRA_CLASS)
        if (pkg.isNullOrBlank() || cls.isNullOrBlank()) {
            Toast.makeText(context, "Private Activity: target package/activity missing", Toast.LENGTH_LONG).show()
            return
        }
        if (!Shizuku.pingBinder() || Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Private Activity: open companion and grant Shizuku permission first", Toast.LENGTH_LONG).show()
            return
        }
        try {
            ActivityLauncher.launch(context, ComponentName(pkg, cls), Bundle())
        } catch (e: Exception) {
            Toast.makeText(context, "Private Activity launch failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
