package com.carthigan.droidconuganda.ui.compose

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.Locale

class ConferenceTextTest {
    @Test fun timeUsesTheSelectedZoneRatherThanTheStoredOffset() {
        val time = ZonedDateTime.parse("2026-10-28T07:55:00Z")
        assertEquals("10:55", conferenceTime(time, ZoneId.of("Africa/Kampala"), Locale.US))
        assertEquals("07:55", conferenceTime(time, ZoneId.of("UTC"), Locale.US))
    }

    @Test fun initialsIgnoreExtraWhitespaceAndHandleMissingNames() {
        assertEquals("EG", speakerInitials("  Eden   Gilbert "))
        assertEquals("E", speakerInitials("Eden"))
        assertEquals("?", speakerInitials("   "))
    }
}
