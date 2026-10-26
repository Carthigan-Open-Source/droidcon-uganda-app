package com.carthigan.droidconuganda.tests.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carthigan.droidconuganda.R
import org.junit.Test

class ComposeStartupTest : ComposeConferenceTest() {
    @Test fun recreatedHostRestoresSearchAndHandlesSystemBack() {
        compose.onNodeWithContentDescription(text(R.string.search_schedule_hint)).performClick()
        compose.onNodeWithText(text(R.string.ui_search_hint)).assertIsDisplayed()

        compose.activityRule.scenario.recreate()
        compose.onNodeWithText(text(R.string.ui_search_hint)).assertIsDisplayed()

        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithContentDescription(text(R.string.search_schedule_hint)).assertIsDisplayed()
    }
}
