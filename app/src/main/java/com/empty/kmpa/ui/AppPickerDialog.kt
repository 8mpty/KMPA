package com.empty.kmpa.ui

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Context.showAppPicker(onSelected: (packageName: String, activityName: String) -> Unit) {
    val pm = packageManager

    fun appLabel(info: PackageInfo): String =
        info.applicationInfo?.loadLabel(pm)?.toString() ?: info.packageName

    val packages = try {
        @Suppress("DEPRECATION")
        pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)
            .filter { it.activities?.isNotEmpty() == true }
            .sortedBy { appLabel(it).lowercase() }
    } catch (e: Exception) {
        toast("Could not list packages: ${e.message}")
        return
    }

    val labels = packages.map { "${appLabel(it)}\n${it.packageName}" }.toTypedArray()

    MaterialAlertDialogBuilder(this)
        .setTitle("Choose an app")
        .setItems(labels) { _, which ->
            val pkg = packages[which]
            val activities = pkg.activities.orEmpty().sortedBy { it.name }

            val activityLabels = activities.map {
                val visibility = if (it.exported) "exported" else "not exported"
                "${it.name.substringAfterLast('.')}  [$visibility]\n${it.name}"
            }.toTypedArray()

            MaterialAlertDialogBuilder(this)
                .setTitle(appLabel(pkg))
                .setItems(activityLabels) { _, activityIndex ->
                    onSelected(pkg.packageName, activities[activityIndex].name)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        .setNegativeButton("Cancel", null)
        .show()
}