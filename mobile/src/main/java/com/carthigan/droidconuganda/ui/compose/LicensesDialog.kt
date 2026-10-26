package com.carthigan.droidconuganda.ui.compose

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.carthigan.droidconuganda.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun LicensesDialog(onDismiss: () -> Unit, onOpenUrl: (String) -> Unit) {
    val context = LocalContext.current
    val fallback = stringResource(R.string.ui_licenses_error)
    val html by produceState<String?>(null) {
        value = withContext(Dispatchers.IO) {
            try { context.assets.open("licenses.html").bufferedReader().use { it.readText() } }
            catch (_: IOException) { fallback }
        }
    }
    val foreground = MaterialTheme.colorScheme.onSurface.toArgb() and 0xFFFFFF
    val background = MaterialTheme.colorScheme.surface.toArgb()
    val link = MaterialTheme.colorScheme.primary.toArgb() and 0xFFFFFF
    val fontScale = LocalDensity.current.fontScale
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxHeight(0.85f)) {
            Column {
                Text(stringResource(R.string.settings_oss_licenses), style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(24.dp))
                val content = html
                if (content != null) AndroidView(
                    factory = { ctx -> WebView(ctx).apply {
                        settings.javaScriptEnabled = false
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        settings.blockNetworkLoads = true
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                onOpenUrl(request.url.toString())
                                return true
                            }
                        }
                    } },
                    update = { view ->
                        view.setBackgroundColor(background)
                        view.settings.textZoom = (fontScale * 100).toInt()
                        val style = "<style>body{font-family:sans-serif;padding:16px;line-height:1.5;color:#%06x;background:#%06x}a{color:#%06x}pre{white-space:pre-wrap;overflow-wrap:anywhere}</style>"
                            .format(foreground, background and 0xFFFFFF, link)
                        val document = style + content
                        if (view.tag != document) {
                            view.tag = document
                            view.loadDataWithBaseURL(null, document, "text/html", "UTF-8", null)
                        }
                    }, modifier = Modifier.weight(1f).fillMaxWidth(),
                    onRelease = { it.stopLoading(); it.destroy() }
                ) else Text(stringResource(R.string.ui_loading), Modifier.weight(1f).padding(24.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.padding(16.dp)) { Text(stringResource(R.string.ui_close)) }
            }
        }
    }
}
