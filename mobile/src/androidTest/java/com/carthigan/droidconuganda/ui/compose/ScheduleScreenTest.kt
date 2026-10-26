package com.carthigan.droidconuganda.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.carthigan.droidconuganda.model.ConferenceDay
import com.carthigan.droidconuganda.model.Session
import com.carthigan.droidconuganda.model.userdata.UserEvent
import com.carthigan.droidconuganda.model.userdata.UserSession
import com.carthigan.droidconuganda.ui.compose.theme.DroidconTheme
import com.carthigan.droidconuganda.ui.schedule.filters.EventFilter
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class ScheduleScreenTest {
    @get:Rule val compose = createComposeRule()
    private val start = ZonedDateTime.parse("2026-10-28T07:00:00+03:00")
    private val days = listOf(ConferenceDay(start, start.plusHours(12)))

    @Test fun ugandaMapFillsTheDateHeaderOnAPhone() {
        compose.setContent {
            val density = LocalDensity.current
            // A 300dp test host still provides a 450dp layout at this density.
            CompositionLocalProvider(LocalDensity provides Density(density.density / 1.5f, 1f)) {
                DroidconTheme {
                    Box(Modifier.width(400.dp).height(800.dp)) {
                        ScheduleScreen(days, 0, emptyList(), emptyList(), loading = false,
                            refreshing = false, failed = false, zone = ZoneId.of("Africa/Kampala"),
                            onDaySelected = {}, onSession = {}, onStar = {}, onSearch = {},
                            onRefresh = {}, onFilter = {}, onClearFilters = {})
                    }
                }
            }
        }
        compose.onNodeWithTag("schedule_list")
            .performScrollToNode(hasTestTag("schedule_uganda_map"))
        compose.onNodeWithTag("schedule_uganda_map").assertIsDisplayed()
            .assertContentDescriptionEquals("Map of Uganda")
            .assertWidthIsEqualTo(112.dp).assertHeightIsEqualTo(112.dp)
    }

    @Test fun ugandaMapRemainsVisibleWithLargeFontsOnANarrowPhone() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                DroidconTheme {
                    Box(Modifier.width(320.dp).height(640.dp)) {
                        ScheduleScreen(days, 0, emptyList(), emptyList(), loading = false,
                            refreshing = false, failed = false, zone = ZoneId.of("Africa/Kampala"),
                            onDaySelected = {}, onSession = {}, onStar = {}, onSearch = {},
                            onRefresh = {}, onFilter = {}, onClearFilters = {})
                    }
                }
            }
        }
        compose.onNodeWithTag("schedule_list")
            .performScrollToNode(hasTestTag("schedule_uganda_map"))
        compose.onNodeWithTag("schedule_uganda_map").assertIsDisplayed()
            .assertContentDescriptionEquals("Map of Uganda")
            .assertWidthIsAtLeast(64.dp).assertHeightIsAtLeast(64.dp)
    }

    @Test fun liveRibbonAppearsOnlyForTheFirstLiveSession() {
        val live = session("live", start.plusHours(1), start.plusHours(2))
        val later = session("later", start.plusHours(2), start.plusHours(3))
        compose.setContent {
            DroidconTheme {
                ScheduleScreen(days, 0, listOf(live, later), emptyList(), loading = false,
                    refreshing = false, failed = false, zone = ZoneId.of("Africa/Kampala"),
                    onDaySelected = {}, onSession = {}, onStar = {}, onSearch = {},
                    onRefresh = {}, onFilter = {}, onClearFilters = {}, liveSessionId = "live")
            }
        }
        compose.onNode(hasScrollAction()).performScrollToNode(hasTestTag("live_session_ribbon"))
        compose.onAllNodesWithTag("live_session_ribbon").assertCountEquals(1)
    }

    @Test fun liveRibbonIsAbsentWhenNoSessionIsLive() {
        val later = session("later", start.plusHours(2), start.plusHours(3))
        compose.setContent {
            DroidconTheme {
                ScheduleScreen(days, 0, listOf(later), emptyList(), loading = false,
                    refreshing = false, failed = false, zone = ZoneId.of("Africa/Kampala"),
                    onDaySelected = {}, onSession = {}, onStar = {}, onSearch = {},
                    onRefresh = {}, onFilter = {}, onClearFilters = {}, liveSessionId = null)
            }
        }
        compose.onAllNodesWithTag("live_session_ribbon").assertCountEquals(0)
    }

    @Test fun refreshRemainsReachableWithLargeFontsOnANarrowPhone() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                DroidconTheme {
                    Box(Modifier.width(320.dp).height(640.dp)) {
                        ScheduleScreen(days, 0, emptyList(), emptyList(), loading = false,
                            refreshing = false, failed = false, zone = ZoneId.of("Africa/Kampala"),
                            onDaySelected = {}, onSession = {}, onStar = {}, onSearch = {},
                            onRefresh = {}, onFilter = {}, onClearFilters = {})
                    }
                }
            }
        }
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Refresh schedule"))
        compose.onNodeWithText("Refresh schedule").assertWidthIsAtLeast(48.dp)
    }

    @Test fun failedInitialLoadOffersRetryInsteadOfEmptySchedule() {
        var refreshes = 0
        compose.setContent {
            DroidconTheme {
                ScheduleScreen(days, 0, null, emptyList(), loading = false,
                    refreshing = false, failed = true, zone = ZoneId.of("Africa/Kampala"),
                    onDaySelected = {}, onSession = {}, onStar = {}, onSearch = {},
                    onRefresh = { refreshes++ }, onFilter = {}, onClearFilters = {})
            }
        }
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Retry"))
        compose.onNodeWithText("Retry").performClick()
        compose.runOnIdle { assertEquals(1, refreshes) }
    }

    @Test fun emptySavedScheduleLetsAttendeeReturnToAllSessions() {
        var clears = 0
        compose.setContent {
            DroidconTheme {
                ScheduleScreen(days, 0, emptyList(), listOf(EventFilter.MyEventsFilter(true)),
                    loading = false, refreshing = false, failed = false,
                    zone = ZoneId.of("Africa/Kampala"), onDaySelected = {}, onSession = {},
                    onStar = {}, onSearch = {}, onRefresh = {}, onFilter = {},
                    onClearFilters = { clears++ })
            }
        }
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Make this day yours."))
        compose.onAllNodesWithText("All sessions").onLast().performClick()
        compose.runOnIdle { assertEquals(1, clears) }
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
