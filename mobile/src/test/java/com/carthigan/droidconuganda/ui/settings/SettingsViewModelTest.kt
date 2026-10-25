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

package com.carthigan.droidconuganda.ui.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.carthigan.droidconuganda.model.TestDataRepository
import com.carthigan.droidconuganda.model.Theme
import com.carthigan.droidconuganda.shared.data.prefs.PreferenceStorage
import com.carthigan.droidconuganda.shared.data.prefs.UserIsAttendee.IN_PERSON
import com.carthigan.droidconuganda.shared.data.prefs.UserIsAttendee.REMOTE
import com.carthigan.droidconuganda.shared.domain.prefs.NotificationsPrefSaveActionUseCase
import com.carthigan.droidconuganda.shared.domain.prefs.UserIsAttendeePrefSaveActionUseCase
import com.carthigan.droidconuganda.shared.domain.settings.GetAnalyticsSettingUseCase
import com.carthigan.droidconuganda.shared.domain.settings.GetAvailableThemesUseCase
import com.carthigan.droidconuganda.shared.domain.settings.GetNotificationsSettingUseCase
import com.carthigan.droidconuganda.shared.domain.settings.GetThemeUseCase
import com.carthigan.droidconuganda.shared.domain.settings.GetTimeZoneUseCase
import com.carthigan.droidconuganda.shared.domain.settings.GetUserIsAttendeeSettingUseCase
import com.carthigan.droidconuganda.shared.domain.settings.SetAnalyticsSettingUseCase
import com.carthigan.droidconuganda.shared.domain.settings.SetThemeUseCase
import com.carthigan.droidconuganda.shared.domain.settings.SetTimeZoneUseCase
import com.carthigan.droidconuganda.test.data.MainCoroutineRule
import com.carthigan.droidconuganda.test.data.runTest
import com.carthigan.droidconuganda.test.util.SyncTaskExecutorRule
import com.carthigan.droidconuganda.test.util.fakes.FakePreferenceStorage
import com.carthigan.droidconuganda.ui.info.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

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
    fun `set theme updates current theme`() = coroutineRule.runTest {
        val storage = FakePreferenceStorage()
        val viewModel = createSettingsViewModel(storage)

        for (theme in Theme.values()) {
            viewModel.setTheme(theme)
            assertEquals(theme.storageKey, storage.selectedTheme)
        }
    }

    @Test
    fun `saving IN_PERSON UserIsAttendee updates setting`() = coroutineRule.runTest {
        val storage = FakePreferenceStorage()

        val viewModel = createSettingsViewModel(storage)
        viewModel.toggleUserIsAttending(true)

        assertEquals(storage.userIsAttendee, IN_PERSON)
    }

    @Test
    fun `saving REMOTE UserIsAttendee updates setting`() = coroutineRule.runTest {
        val storage = FakePreferenceStorage()

        val viewModel = createSettingsViewModel(storage)
        viewModel.toggleUserIsAttending(false)

        assertEquals(storage.userIsAttendee, REMOTE)
    }

    private fun createSettingsViewModel(
        storage: PreferenceStorage = FakePreferenceStorage()
    ): SettingsViewModel {
        return SettingsViewModel(
            SetTimeZoneUseCase(storage, TestDataRepository, coroutineRule.testDispatcher),
            GetTimeZoneUseCase(storage, coroutineRule.testDispatcher),
            NotificationsPrefSaveActionUseCase(storage, coroutineRule.testDispatcher),
            GetNotificationsSettingUseCase(storage, coroutineRule.testDispatcher),
            SetAnalyticsSettingUseCase(storage, coroutineRule.testDispatcher),
            GetAnalyticsSettingUseCase(storage, coroutineRule.testDispatcher),
            SetThemeUseCase(storage, coroutineRule.testDispatcher),
            GetThemeUseCase(storage, coroutineRule.testDispatcher),
            GetAvailableThemesUseCase(coroutineRule.testDispatcher),
            UserIsAttendeePrefSaveActionUseCase(
                storage, TestDataRepository, coroutineRule.testDispatcher
            ),
            GetUserIsAttendeeSettingUseCase(storage, coroutineRule.testDispatcher)
        )
    }
}
