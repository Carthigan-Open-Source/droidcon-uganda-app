package com.carthigan.droidconuganda.tests.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.shared.data.FakeConferenceDataSource
import com.carthigan.droidconuganda.shared.util.TimeUtils
import com.carthigan.droidconuganda.ui.compose.conferenceDate
import org.junit.Test

class ComposeConferenceNavigationTest : ComposeConferenceTest() {
    @Test fun bothDaysShowTheirSessions() {
        TimeUtils.ConferenceDays.forEachIndexed { index, day ->
            compose.onNodeWithText(conferenceDate(day.start, day.start.zone, "MMM d"))
                .performClick().assertIsSelected()
            val title = "First session day ${index + 1}"
            scrollTo("schedule_list", title)
            compose.onNodeWithText(title).assertIsDisplayed()
        }
    }

    @Test fun sessionSpeakersAndRelatedTalksRemainReachable() {
        scrollTo("schedule_list", FakeConferenceDataSource.FAKE_SESSION_NAME)
        compose.onNodeWithText(FakeConferenceDataSource.FAKE_SESSION_NAME).performClick()
        scrollTo("session_detail_list", FakeConferenceDataSource.FAKE_SESSION_SPEAKER_NAME)
        compose.onNodeWithText(FakeConferenceDataSource.FAKE_SESSION_SPEAKER_NAME).performClick()
        scrollTo("speaker_list", text(R.string.ui_speaker_sessions))
        compose.onNodeWithContentDescription(text(R.string.navigate_up)).assertIsDisplayed().performClick()
        scrollTo("session_detail_list", "First session day 2")
        compose.onNodeWithText("First session day 2").assertIsDisplayed()
    }

    @Test fun topicFiltersCanBeAppliedAndCleared() {
        val name = FakeConferenceDataSource.FAKE_SESSION_TAG_NAME
        compose.onNodeWithText(text(R.string.filters)).performClick()
        val unchecked = compose.activity.getString(R.string.a11y_filter_not_applied, name)
        val checked = compose.activity.getString(R.string.a11y_filter_applied, name)
        compose.onNodeWithContentDescription(unchecked).performClick()
        compose.onNodeWithContentDescription(checked).assertIsSelected()
        compose.onNodeWithText(text(R.string.ui_close)).performClick()
        scrollTo("schedule_list", FakeConferenceDataSource.FAKE_SESSION_NAME)
        compose.onNodeWithText(FakeConferenceDataSource.FAKE_SESSION_NAME).assertIsDisplayed()
        scrollTo("schedule_list", compose.activity.getString(R.string.ui_filters_active, 1))
        compose.onNodeWithText(compose.activity.getString(R.string.ui_filters_active, 1)).performClick()
        compose.onNodeWithText(text(R.string.clear_all)).performClick()
        compose.onNodeWithContentDescription(unchecked).assertExists()
    }

    @Test fun agendaAndInfoDestinationsAndFaqDisclosureWork() {
        compose.onNodeWithText(text(R.string.title_agenda)).performClick()
        compose.onNodeWithText(text(R.string.ui_agenda_subtitle)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.title_info)).performClick()
        scrollTo("info_list", text(R.string.ui_venue))
        compose.onNodeWithText(text(R.string.ui_venue)).assertIsDisplayed()
        scrollTo("info_list", text(R.string.faq_title))
        compose.onNodeWithText(text(R.string.faq_title)).performClick()
        scrollTo("info_list", text(R.string.dates_location_title))
        compose.onNodeWithText(text(R.string.dates_location_title)).performClick().assert(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, text(R.string.expanded)))
        scrollTo("info_list", text(R.string.about_title))
        compose.onNodeWithText(text(R.string.about_title)).performClick()
        scrollTo("info_list", text(R.string.settings_oss_licenses))
        compose.onNodeWithText(text(R.string.settings_oss_licenses)).performClick()
        compose.onNodeWithText(text(R.string.ui_close)).performClick()
        scrollTo("info_list", text(R.string.settings_title))
        compose.onNodeWithText(text(R.string.settings_title)).performClick()
        scrollTo("info_list", text(R.string.settings_enable_notifications))
        compose.onNodeWithText(text(R.string.settings_enable_notifications)).assertIsDisplayed()
    }
}
