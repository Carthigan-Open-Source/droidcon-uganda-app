package com.carthigan.droidconuganda.tests.ui

import androidx.annotation.StringRes
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import com.carthigan.droidconuganda.tests.FixedTimeRule
import com.carthigan.droidconuganda.tests.SetPreferencesRule
import com.carthigan.droidconuganda.tests.SyncTaskExecutorRule
import com.carthigan.droidconuganda.ui.MainActivity
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

abstract class ComposeConferenceTest(time: FixedTimeRule = FixedTimeRule()) {
    val compose = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val rules: TestRule = RuleChain.outerRule(SyncTaskExecutorRule())
        .around(time).around(SetPreferencesRule()).around(compose)

    fun text(@StringRes resource: Int): String = compose.activity.getString(resource)

    fun scrollTo(list: String, text: String) {
        compose.onNodeWithTag(list).performScrollToNode(hasText(text))
    }
}
