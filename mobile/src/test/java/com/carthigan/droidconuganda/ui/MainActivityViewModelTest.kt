/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.carthigan.droidconuganda.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.carthigan.droidconuganda.androidtest.util.LiveDataTestUtil
import com.carthigan.droidconuganda.model.TestDataRepository
import com.carthigan.droidconuganda.shared.data.prefs.PreferenceStorage
import com.carthigan.droidconuganda.shared.data.prefs.UserIsAttendee.IN_PERSON
import com.carthigan.droidconuganda.shared.data.prefs.UserIsAttendee.NO_ANSWER
import com.carthigan.droidconuganda.shared.data.prefs.UserIsAttendee.REMOTE
import com.carthigan.droidconuganda.shared.domain.RefreshConferenceDataUseCase
import com.carthigan.droidconuganda.shared.domain.settings.GetUserIsAttendeeSettingUseCase
import com.carthigan.droidconuganda.test.data.MainCoroutineRule
import com.carthigan.droidconuganda.test.util.SyncTaskExecutorRule
import com.carthigan.droidconuganda.test.util.fakes.FakeAnalyticsHelper
import com.carthigan.droidconuganda.test.util.fakes.FakePreferenceStorage
import com.carthigan.droidconuganda.test.util.fakes.FakeThemedActivityDelegate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [MainActivityViewModel].
 */
@ExperimentalCoroutinesApi
class MainActivityViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule
    var syncTaskExecutorRule = SyncTaskExecutorRule()

    // Overrides Dispatchers.Main used in Coroutines
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @Test
    fun loadPreferences_userInPerson_doesNotOpenAttendeeDialog() {
        val prefs = FakePreferenceStorage(userIsAttendee = IN_PERSON)
        val viewModel = createViewModel(prefs)

        // Can't use LiveDataTestUtil here because it would take 2 seconds
        val result = viewModel.navigateToUserAttendeeDialogAction.value

        assertNull(result?.getContentIfNotHandled())
    }

    @Test
    fun loadPreferences_userRemote_doesNotOpenAttendeeDialog() {
        val prefs = FakePreferenceStorage(userIsAttendee = REMOTE)
        val viewModel = createViewModel(prefs)

        // Can't use LiveDataTestUtil here because it would take 2 seconds
        val result = viewModel.navigateToUserAttendeeDialogAction.value

        assertNull(result?.getContentIfNotHandled())
    }

    @Test
    fun loadPreferences_userNoAnswer_opensAttendeeDialog() {
        val prefs = FakePreferenceStorage(userIsAttendee = NO_ANSWER)
        val viewModel = createViewModel(prefs)

        val result = LiveDataTestUtil.getValue(viewModel.navigateToUserAttendeeDialogAction)

        assertNotNull(result?.getContentIfNotHandled())
    }

    private fun createViewModel(
        preferenceStorage: PreferenceStorage = FakePreferenceStorage()
    ): MainActivityViewModel {
        val userIsAttendeeSettingUseCase = GetUserIsAttendeeSettingUseCase(
            preferenceStorage,
            coroutineRule.testDispatcher
        )
        val refreshConferenceDataUseCase = RefreshConferenceDataUseCase(
            TestDataRepository,
            coroutineRule.testDispatcher
        )
        return MainActivityViewModel(
            userIsAttendeeSettingUseCase,
            FakeThemedActivityDelegate(),
            refreshConferenceDataUseCase,
            FakeAnalyticsHelper()
        )
    }
}
