package com.carthigan.droidconuganda.tests.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.shared.util.TimeUtils
import com.carthigan.droidconuganda.tests.FixedTimeProvider
import com.carthigan.droidconuganda.tests.FixedTimeRule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ComposeScheduleScrollTest(private val duringConference: Boolean) : ComposeConferenceTest(
    if (duringConference) FixedTimeRule(FixedTimeProvider(TimeUtils.ConferenceDays.first().end.minusMinutes(15)))
    else FixedTimeRule()
) {
    companion object {
        @JvmStatic @Parameterized.Parameters fun data(): Collection<Boolean> = listOf(true, false)
    }

    @Test fun currentSessionIsShownOnlyWhileTheConferenceIsUnderway() {
        if (duringConference) {
            compose.waitUntil(5_000) {
                compose.onAllNodesWithText("Last session day 1").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Last session day 1").assertIsDisplayed()
        } else {
            compose.onNodeWithText(text(R.string.ui_all_sessions)).assertIsDisplayed()
        }
    }
}
