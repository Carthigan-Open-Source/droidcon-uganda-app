package com.carthigan.droidconuganda.ui.compose

import com.carthigan.droidconuganda.model.Session
import com.carthigan.droidconuganda.model.userdata.UserEvent
import com.carthigan.droidconuganda.model.userdata.UserSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.ZonedDateTime

class LiveSessionTest {
    private val day = ZonedDateTime.parse("2026-10-28T09:00:00+03:00")

    @Test fun firstLiveSessionSkipsFinishedSessions() {
        val sessions = listOf(
            session("09:00", day, day.plusHours(1)),
            session("10:00", day.plusHours(1), day.plusHours(2)),
            session("11:00", day.plusHours(2), day.plusHours(3))
        )

        assertEquals("10:00", firstLiveSession(sessions, day.plusMinutes(90))?.session?.id)
    }

    @Test fun firstLiveSessionIsNullBetweenSessions() {
        val sessions = listOf(
            session("09:00", day, day.plusHours(1)),
            session("10:30", day.plusMinutes(90), day.plusMinutes(150))
        )

        assertNull(firstLiveSession(sessions, day.plusMinutes(75)))
    }

    @Test fun firstLiveSessionUsesListOrderForConcurrentTracks() {
        val sessions = listOf(
            session("main", day.plusHours(1), day.plusHours(2)),
            session("second track", day.plusMinutes(75), day.plusMinutes(135))
        )

        assertEquals("main", firstLiveSession(sessions, day.plusMinutes(90))?.session?.id)
    }

    @Test fun firstLiveSessionIncludesStartAndExcludesEnd() {
        val sessions = listOf(
            session("09:00", day, day.plusHours(1)),
            session("10:00", day.plusHours(1), day.plusHours(2))
        )

        assertEquals("09:00", firstLiveSession(sessions, day.plusHours(1).minusNanos(1))?.session?.id)
        assertEquals("10:00", firstLiveSession(sessions, day.plusHours(1))?.session?.id)
    }

    @Test fun detailSessionIsLiveOnlyBetweenItsOwnBoundaries() {
        val userSession = session("10:00", day.plusHours(1), day.plusHours(2))

        assertEquals(false, isSessionLive(userSession.session, day.plusHours(1).minusNanos(1)))
        assertEquals(true, isSessionLive(userSession.session, day.plusHours(1)))
        assertEquals(true, isSessionLive(userSession.session, day.plusHours(1).plusMinutes(30)))
        assertEquals(false, isSessionLive(userSession.session, day.plusHours(2)))
    }

    @Test fun detailBoundaryUsesTheSessionStartOrEnd() {
        val userSession = session("10:00", day.plusHours(1), day.plusHours(2))

        assertEquals(30 * 60_000L,
            millisUntilNextSessionBoundary(userSession.session, day.plusMinutes(90)))
        assertEquals(Long.MAX_VALUE,
            millisUntilNextSessionBoundary(userSession.session, day.plusHours(2)))
    }

    @Test fun millisUntilNextBoundaryUsesTheNearestStartOrEnd() {
        val sessions = listOf(
            session("10:00", day.plusHours(1), day.plusHours(2)),
            session("11:00", day.plusHours(2), day.plusHours(3))
        )

        assertEquals(30 * 60_000L,
            millisUntilNextSessionBoundary(sessions, day.plusMinutes(90)))
        assertEquals(15 * 60_000L,
            millisUntilNextSessionBoundary(sessions, day.plusMinutes(105)))
    }

    @Test fun millisUntilNextBoundaryWithoutFutureSessionsIsMaxValue() {
        val sessions = listOf(session("10:00", day.plusHours(1), day.plusHours(2)))

        assertEquals(Long.MAX_VALUE,
            millisUntilNextSessionBoundary(sessions, day.plusHours(3)))
    }

    private fun session(id: String, start: ZonedDateTime, end: ZonedDateTime) = UserSession(
        Session(
            id = id,
            startTime = start,
            endTime = end,
            title = id,
            abstract = "",
            room = null,
            sessionUrl = "",
            liveStreamUrl = null,
            isLivestream = false,
            youTubeUrl = "",
            tags = emptyList(),
            displayTags = emptyList(),
            speakers = emptySet(),
            photoUrl = "",
            relatedSessions = emptySet()
        ),
        UserEvent(id = id)
    )
}
