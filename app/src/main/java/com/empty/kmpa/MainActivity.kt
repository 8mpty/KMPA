package com.empty.kmpa

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.empty.kmpa.shizuku.ShizukuHelper
import com.empty.kmpa.ui.afterTextChanged
import com.empty.kmpa.ui.bindCopyRow
import com.empty.kmpa.ui.showAppPicker
import com.empty.kmpa.ui.showIntegrationHelp
import com.empty.kmpa.ui.toast
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private lateinit var packageInput: EditText
    private lateinit var classInput: EditText

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == ShizukuHelper.PERMISSION_REQUEST_CODE) {
            refreshStatus()
            toast(
                if (grantResult == PackageManager.PERMISSION_GRANTED) "Shizuku permission granted"
                else "Shizuku permission denied"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Shizuku.addRequestPermissionResultListener(permissionListener)
        setContentView(R.layout.activity_main)
        bindViews()
        refreshStatus()
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        super.onDestroy()
    }

    private fun bindViews() {
        status = findViewById(R.id.status)
        packageInput = findViewById(R.id.packageInput)
        classInput = findViewById(R.id.classInput)

        findViewById<View>(R.id.btnCheckPermission).setOnClickListener { requestShizukuPermission() }
        findViewById<View>(R.id.btnBrowseApps).setOnClickListener {
            showAppPicker { pkg, cls ->
                packageInput.setText(pkg)
                classInput.setText(cls)
            }
        }
        findViewById<View>(R.id.btnLaunch).setOnClickListener { launchFromFields() }
        findViewById<View>(R.id.btnHelp).setOnClickListener { showIntegrationHelp() }

        bindCopyRow(R.id.rowAction, getString(R.string.label_action_name), LaunchContract.ACTION_LAUNCH)
        bindCopyRow(R.id.rowPackage, getString(R.string.label_package), packageName)

        packageInput.afterTextChanged { updateExtras() }
        classInput.afterTextChanged { updateExtras() }
        updateExtras()
    }

    private fun updateExtras() {
        val pkg = packageInput.text.toString().trim()
        val cls = classInput.text.toString().trim()
        val container = findViewById<View>(R.id.extrasContainer)

        if (pkg.isEmpty() || cls.isEmpty()) {
            container.visibility = View.GONE
            return
        }

        bindCopyRow(R.id.rowExtra1Key, getString(R.string.label_extra1_key), LaunchContract.EXTRA_PACKAGE)
        bindCopyRow(R.id.rowExtra1Value, getString(R.string.label_extra1_value), pkg)
        bindCopyRow(R.id.rowExtra2Key, getString(R.string.label_extra2_key), LaunchContract.EXTRA_CLASS)
        bindCopyRow(R.id.rowExtra2Value, getString(R.string.label_extra2_value), cls)
        container.visibility = View.VISIBLE
    }

    private fun requestShizukuPermission() {
        if (!ShizukuHelper.isRunning) {
            toast("Shizuku is not running. Start it in the Shizuku app first.")
            refreshStatus()
            return
        }
        if (ShizukuHelper.hasPermission) {
            toast("Shizuku permission is already granted")
            refreshStatus()
            return
        }
        try {
            ShizukuHelper.requestPermission()
        } catch (e: Exception) {
            toast("Could not request Shizuku permission: ${e.message}")
        }
    }

    private fun refreshStatus() {
        status.text = getString(
            R.string.status_format,
            getString(if (ShizukuHelper.isRunning) R.string.shizuku_running else R.string.shizuku_not_running),
            getString(if (ShizukuHelper.hasPermission) R.string.permission_granted else R.string.permission_not_granted)
        )
    }

    private fun launchFromFields() {
        val pkg = packageInput.text.toString().trim()
        val cls = classInput.text.toString().trim()
        if (pkg.isBlank() || cls.isBlank()) {
            toast("Enter or select a package and activity class")
            return
        }
        if (!ShizukuHelper.hasPermission) {
            toast("Grant Shizuku permission first")
            return
        }
        try {
            ActivityLauncher.launch(this, ComponentName(pkg, cls), Bundle())
            toast("Launch request sent")
        } catch (e: Exception) {
            toast("Launch failed: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action != LaunchContract.ACTION_LAUNCH) return
        val pkg = intent.getStringExtra(LaunchContract.EXTRA_PACKAGE)
        val cls = intent.getStringExtra(LaunchContract.EXTRA_CLASS)
        if (pkg.isNullOrBlank() || cls.isNullOrBlank()) {
            toast("Missing target package/class in launch intent")
            return
        }
        packageInput.setText(pkg)
        classInput.setText(cls)
        if (ShizukuHelper.hasPermission) {
            try {
                ActivityLauncher.launch(this, ComponentName(pkg, cls), Bundle())
            } catch (e: Exception) {
                toast("Launch failed: ${e.message}")
            }
        } else {
            toast("Open the companion app and grant Shizuku permission before using key-map launches")
        }
    }
}