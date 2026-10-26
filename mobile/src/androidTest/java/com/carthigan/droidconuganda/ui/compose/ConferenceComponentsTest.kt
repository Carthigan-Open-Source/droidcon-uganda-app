package com.carthigan.droidconuganda.ui.compose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carthigan.droidconuganda.model.ConferenceDay
import com.carthigan.droidconuganda.model.Session
import com.carthigan.droidconuganda.model.userdata.UserEvent
import com.carthigan.droidconuganda.model.userdata.UserSession
import com.carthigan.droidconuganda.ui.compose.components.ConferenceDayPicker
import com.carthigan.droidconuganda.ui.compose.components.ConferenceMessage
import com.carthigan.droidconuganda.ui.compose.components.SessionCard
import com.carthigan.droidconuganda.ui.compose.theme.DroidconTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class ConferenceComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun choosingAnotherDayUpdatesSelection() {
        val selected = mutableStateOf(0)
        val start = ZonedDateTime.parse("2026-10-28T07:00:00+03:00")
        val days = listOf(
            ConferenceDay(start, start.plusHours(12)),
            ConferenceDay(start.plusDays(1), start.plusDays(1).plusHours(12))
        )
        compose.setContent {
            DroidconTheme {
                ConferenceDayPicker(days, selected.value, { selected.value = it })
            }
        }
        compose.onNodeWithText("Oct 29").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(1, selected.value) }
    }

    @Test fun bookmarkDoesNotAlsoOpenSession() {
        var opens = 0
        var stars = 0
        val start = ZonedDateTime.parse("2026-10-28T10:55:00+03:00")
        val session = Session(
            id = "test-session", startTime = start, endTime = start.plusMinutes(40),
            title = "A session title long enough to wrap across several lines on a small screen",
            abstract = "Session description", room = null, sessionUrl = "",
            liveStreamUrl = null, isLivestream = false, youTubeUrl = "", tags = emptyList(),
            displayTags = emptyList(), speakers = emptySet(), photoUrl = "", relatedSessions = emptySet()
        )
        compose.setContent {
            DroidconTheme {
                SessionCard(
                    UserSession(session, UserEvent(id = session.id)), ZoneId.of("Africa/Kampala"),
                    onClick = { opens++ }, onStar = { stars++ }
                )
            }
        }
        compose.onNodeWithContentDescription("Save session").performClick()
        compose.runOnIdle {
            assertEquals(1, stars)
            assertEquals(0, opens)
        }
        compose.onNodeWithText(session.title).performClick()
        compose.runOnIdle { assertEquals(1, opens) }
    }

    @Test fun retryActionIsAvailableInErrorState() {
        var retries = 0
        compose.setContent {
            DroidconTheme {
                ConferenceMessage(
                    title = "Schedule unavailable", message = "Try loading the schedule again.",
                    actionLabel = "Retry", onAction = { retries++ }
                )
            }
        }
        compose.onNodeWithText("Retry").performClick()
        compose.runOnIdle { assertEquals(1, retries) }
    }
}
