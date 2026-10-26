package com.carthigan.droidconuganda.ui.compose

import android.net.Uri

fun sessionIdFromDeepLink(uri: Uri?): String? {
    if (uri?.scheme != "adssched" || uri.host != "sessions" || uri.pathSegments.size != 1) return null
    return uri.pathSegments.single().takeIf { it.matches(Regex("[A-Za-z0-9_-]{1,128}")) }
}

internal fun sessionRoute(id: String) = "session/${Uri.encode(id)}"
internal fun speakerRoute(id: String) = "speaker/${Uri.encode(id)}"
