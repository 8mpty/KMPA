package com.empty.kmpa.ui

import android.content.Context
import com.empty.kmpa.LaunchContract
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Context.showIntegrationHelp() {
    val help = """
        Key Mapper → add an Intent action:
        • Type: Broadcast receiver
        • Action: ${LaunchContract.ACTION_LAUNCH}
        • Package: $packageName
        • Extra string: ${LaunchContract.EXTRA_PACKAGE} = target package name
        • Extra string: ${LaunchContract.EXTRA_CLASS} = fully qualified activity class

        Example:
        ${LaunchContract.EXTRA_PACKAGE} = com.android.settings
        ${LaunchContract.EXTRA_CLASS} = com.android.settings.Settings

        Some Key Mapper versions expose different fields for Intent extras. If you cannot specify extras, configure the target in this app and use the launcher activity as a regular app action instead.
    """.trimIndent()

    MaterialAlertDialogBuilder(this)
        .setTitle("Key Mapper setup")
        .setMessage(help)
        .setPositiveButton("OK", null)
        .show()
}