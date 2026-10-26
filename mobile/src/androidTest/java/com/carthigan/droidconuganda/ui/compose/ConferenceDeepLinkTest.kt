package com.carthigan.droidconuganda.ui.compose

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConferenceDeepLinkTest {
    @Test fun acceptsExistingSessionNotificationLinks() {
        assertEquals("1287249", sessionIdFromDeepLink(Uri.parse("adssched://sessions/1287249")))
    }

    @Test fun rejectsOtherHostsSchemesAndNestedPaths() {
        listOf("https://sessions/1287249", "adssched://other/1287249",
            "adssched://sessions/1287249/extra", "adssched://sessions/").forEach {
            assertNull(sessionIdFromDeepLink(Uri.parse(it)))
        }
    }
}
