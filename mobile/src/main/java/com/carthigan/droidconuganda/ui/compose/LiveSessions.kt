package com.carthigan.droidconuganda.ui.compose

import com.carthigan.droidconuganda.model.Session
import com.carthigan.droidconuganda.model.userdata.UserSession
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime

/**
 * The first session in list order that is live at [now].
 *
 * Start is inclusive and end is exclusive, so a session hands off to the next one exactly at the
 * boundary.
 */
fun firstLiveSession(sessions: List<UserSession>, now: ZonedDateTime): UserSession? =
    sessions.firstOrNull { userSession ->
        !now.isBefore(userSession.session.startTime) && now.isBefore(userSession.session.endTime)
    }

fun isSessionLive(session: Session, now: ZonedDateTime): Boolean =
    !now.isBefore(session.startTime) && now.isBefore(session.endTime)

/**
 * Milliseconds until the next session starts or ends, or [Long.MAX_VALUE] when no boundary remains.
 */
fun millisUntilNextSessionBoundary(sessions: List<UserSession>, now: ZonedDateTime): Long =
    millisUntilNextBoundary(sessions.flatMap { listOf(it.session.startTime, it.session.endTime) }, now)

fun millisUntilNextSessionBoundary(session: Session, now: ZonedDateTime): Long =
    millisUntilNextBoundary(listOf(session.startTime, session.endTime), now)

private fun millisUntilNextBoundary(boundaries: List<ZonedDateTime>, now: ZonedDateTime): Long {
    val nextBoundary = boundaries.filter { it.isAfter(now) }.minOrNull()
        ?: return Long.MAX_VALUE
    return Duration.between(now, nextBoundary).toMillis().coerceAtLeast(1L)
}
