package com.carthigan.droidconuganda.tests.ui

import android.app.Activity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.tests.FixedTimeRule
import com.carthigan.droidconuganda.tests.SetPreferencesRule
import com.carthigan.droidconuganda.tests.SyncTaskExecutorRule
import com.carthigan.droidconuganda.ui.MainActivity
import com.carthigan.droidconuganda.ui.onboarding.OnboardingActivity
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

class ComposeOnboardingStartupTest {
    private val compose = createAndroidComposeRule<OnboardingActivity>()

    @get:Rule
    val rules: TestRule = RuleChain.outerRule(SyncTaskExecutorRule())
        .around(FixedTimeRule()).around(SetPreferencesRule()).around(compose)

    @Test fun completingOnboardingOpensScheduleWithoutObserverCollision() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val monitor = instrumentation.addMonitor(MainActivity::class.java.name, null, false)
        val startLabel = compose.activity.getString(R.string.ui_open_schedule)
        val searchDescription = compose.activity.getString(R.string.search_schedule_hint)
        var mainActivity: Activity? = null
        try {
            compose.onNodeWithText(startLabel).assertIsDisplayed().performClick()
            mainActivity = instrumentation.waitForMonitorWithTimeout(monitor, 5_000)
            assertNotNull("Completing onboarding must open MainActivity", mainActivity)
            compose.onNodeWithContentDescription(searchDescription).assertIsDisplayed()
        } finally {
            instrumentation.runOnMainSync { mainActivity?.finish() }
            instrumentation.removeMonitor(monitor)
        }
    }
}
