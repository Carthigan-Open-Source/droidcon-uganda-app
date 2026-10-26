package com.carthigan.droidconuganda.ui.compose

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.carthigan.droidconuganda.R

@Composable
fun rememberNotificationToggle(onToggle: (Boolean) -> Unit, onDenied: () -> Unit): (Boolean) -> Unit {
    val context = LocalContext.current
    val toggle = rememberUpdatedState(onToggle)
    val denied = rememberUpdatedState(onDenied)
    var showRationale by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val enabled = granted && NotificationManagerCompat.from(context).areNotificationsEnabled()
        toggle.value(enabled)
        if (!enabled) denied.value()
    }
    if (showRationale) {
        AlertDialog(onDismissRequest = { showRationale = false; toggle.value(false) },
            title = { Text(stringResource(R.string.notifications_preference_dialog_title)) },
            text = { Text(stringResource(R.string.notifications_preference_dialog_content)) },
            confirmButton = { TextButton(onClick = {
                showRationale = false
                if (Build.VERSION.SDK_INT >= 33) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }) { Text(stringResource(R.string.yes)) } },
            dismissButton = { TextButton(onClick = { showRationale = false; toggle.value(false) }) {
                Text(stringResource(R.string.not_now))
            } })
    }
    return { enabled ->
        when {
            !enabled -> toggle.value(false)
            Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED -> {
                val activity = context.findActivity()
                if (activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.POST_NOTIFICATIONS)) showRationale = true
                else launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            NotificationManagerCompat.from(context).areNotificationsEnabled() -> toggle.value(true)
            else -> { toggle.value(false); denied.value() }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
