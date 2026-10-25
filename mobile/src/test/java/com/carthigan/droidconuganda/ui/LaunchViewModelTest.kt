/*
 * Copyright 2018 Google LLC
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

@file:Suppress("FunctionName")

package com.carthigan.droidconuganda.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.carthigan.droidconuganda.androidtest.util.LiveDataTestUtil
import com.carthigan.droidconuganda.shared.data.prefs.PreferenceStorage
import com.carthigan.droidconuganda.shared.domain.prefs.OnboardingCompletedUseCase
import com.carthigan.droidconuganda.test.data.MainCoroutineRule
import com.carthigan.droidconuganda.test.data.runTest
import com.carthigan.droidconuganda.test.util.SyncTaskExecutorRule
import com.carthigan.droidconuganda.ui.LaunchDestination.MAIN_ACTIVITY
import com.carthigan.droidconuganda.ui.LaunchDestination.ONBOARDING
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the [LaunchViewModel].
 */
class LaunchViewModelTest {

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
    fun notCompletedOnboarding_navigatesToOnboarding() = coroutineRule.runTest {
        // Given that user has *not* completed onboarding
        val prefs = mock<PreferenceStorage> {
            on { onboardingCompleted }.doReturn(false)
        }
        val onboardingCompletedUseCase =
            OnboardingCompletedUseCase(prefs, coroutineRule.testDispatcher)
        val viewModel = LaunchViewModel(onboardingCompletedUseCase)

        // When launchDestination is observed
        // Then verify user is navigated to the onboarding activity
        val navigateEvent = LiveDataTestUtil.getValue(viewModel.launchDestination)
        assertEquals(ONBOARDING, navigateEvent?.getContentIfNotHandled())
    }

    @Test
    fun hasCompletedOnboarding_navigatesToMainActivity() = coroutineRule.runTest {
        // Given that user *has* completed onboarding
        val prefs = mock<PreferenceStorage> {
            on { onboardingCompleted }.doReturn(true)
        }
        val onboardingCompletedUseCase =
            OnboardingCompletedUseCase(prefs, coroutineRule.testDispatcher)
        val viewModel = LaunchViewModel(onboardingCompletedUseCase)

        // When launchDestination is observed
        // Then verify user is navigated to the main activity
        val navigateEvent = LiveDataTestUtil.getValue(viewModel.launchDestination)
        assertEquals(MAIN_ACTIVITY, navigateEvent?.getContentIfNotHandled())
    }

    @Test
    fun error_navigatesToMainActivity() = coroutineRule.runTest {
        // Given that checking if the user has completed onboarding gives an error
        val prefs = mock<PreferenceStorage> {
            on { onboardingCompleted }.doThrow(IllegalStateException("Error"))
        }
        val onboardingCompletedUseCase =
            OnboardingCompletedUseCase(prefs, coroutineRule.testDispatcher)
        val viewModel = LaunchViewModel(onboardingCompletedUseCase)

        // When launchDestination is observed
        // Then verify user is navigated to the main activity
        val navigateEvent = LiveDataTestUtil.getValue(viewModel.launchDestination)
        assertEquals(MAIN_ACTIVITY, navigateEvent?.getContentIfNotHandled())
    }
}
