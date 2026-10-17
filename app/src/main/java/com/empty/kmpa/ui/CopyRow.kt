package com.empty.kmpa.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import com.empty.kmpa.R

fun Activity.bindCopyRow(@IdRes rowId: Int, label: String, value: String) {
    val row = findViewById<View>(rowId)
    row.findViewById<TextView>(R.id.copyLabel).text = label
    row.findViewById<TextView>(R.id.copyValue).text = value
    row.findViewById<View>(R.id.btnCopy).setOnClickListener { copyToClipboard(label, value) }
}

fun Context.copyToClipboard(label: String, value: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        toast(getString(R.string.copied_format, label))
    }
}