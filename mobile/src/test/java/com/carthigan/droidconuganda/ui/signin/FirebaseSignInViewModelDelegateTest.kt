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

package com.carthigan.droidconuganda.ui.signin

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.carthigan.droidconuganda.androidtest.util.LiveDataTestUtil
import com.carthigan.droidconuganda.shared.data.signin.AuthenticatedUserInfo
import com.carthigan.droidconuganda.shared.domain.prefs.NotificationsPrefIsShownUseCase
import com.carthigan.droidconuganda.shared.result.Result
import com.carthigan.droidconuganda.shared.result.data
import com.carthigan.droidconuganda.test.data.MainCoroutineRule
import com.carthigan.droidconuganda.test.data.runTest
import com.carthigan.droidconuganda.test.util.SyncTaskExecutorRule
import com.carthigan.droidconuganda.test.util.fakes.FakePreferenceStorage
import com.carthigan.droidconuganda.ui.schedule.FakeObserveUserAuthStateUseCase
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [FirebaseSignInViewModelDelegate]
 */
class FirebaseSignInViewModelDelegateTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule var syncTaskExecutorRule = SyncTaskExecutorRule()

    // Overrides Dispatchers.Main used in Coroutines
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @Test
    fun testSignedOut() = coroutineRule.runTest {
        val subject = FirebaseSignInViewModelDelegate(
            FakeObserveUserAuthStateUseCase(Result.Success(null), coroutineRule.testDispatcher),
            createNotificationsPrefIsShownUseCase(),
            coroutineRule.testDispatcher,
            coroutineRule.testDispatcher
        )

        val currentFirebaseUser = subject.currentFirebaseUser.first()

        assertEquals(null, currentFirebaseUser.data?.getUid())
        assertEquals(null, LiveDataTestUtil.getValue(subject.currentUserImageUri))
        assertFalse(subject.isSignedIn())
    }

    @Test
    fun testSignedInRegistered() = coroutineRule.runTest {

        val user = mock<AuthenticatedUserInfo> {
            on { getUid() }.doReturn("123")
            on { getPhotoUrl() }.doReturn(mock<Uri> {})
            on { isSignedIn() }.doReturn(true)
        }
        val subject = FirebaseSignInViewModelDelegate(
            FakeObserveUserAuthStateUseCase(Result.Success(user), coroutineRule.testDispatcher),
            createNotificationsPrefIsShownUseCase(),
            coroutineRule.testDispatcher,
            coroutineRule.testDispatcher
        )

        val currentFirebaseUser = subject.currentFirebaseUser.first()

        assertEquals(user.getUid(), currentFirebaseUser.data?.getUid())
        assertEquals(user.getPhotoUrl(), LiveDataTestUtil.getValue(subject.currentUserImageUri))
        assertTrue(subject.isSignedIn())
    }

    @Test
    fun testSignedInNotRegistered() = coroutineRule.runTest {

        val user = mock<AuthenticatedUserInfo> {
            on { getUid() }.doReturn("123")
            on { getPhotoUrl() }.doReturn(mock<Uri> {})
            on { isSignedIn() }.doReturn(true)
        }
        val subject = FirebaseSignInViewModelDelegate(
            FakeObserveUserAuthStateUseCase(Result.Success(user), coroutineRule.testDispatcher),
            createNotificationsPrefIsShownUseCase(),
            coroutineRule.testDispatcher,
            coroutineRule.testDispatcher
        )

        val currentFirebaseUser = subject.currentFirebaseUser.first()

        assertEquals(user.getUid(), currentFirebaseUser.data?.getUid())
        assertEquals(user.getPhotoUrl(), LiveDataTestUtil.getValue(subject.currentUserImageUri))
        assertTrue(subject.isSignedIn())
    }

    @Test
    fun testPostSignIn() = coroutineRule.runTest {
        val subject = FirebaseSignInViewModelDelegate(
            FakeObserveUserAuthStateUseCase(Result.Success(null), coroutineRule.testDispatcher),
            createNotificationsPrefIsShownUseCase(),
            coroutineRule.testDispatcher,
            coroutineRule.testDispatcher
        )

        subject.emitSignInRequest()

        // Check that the emitted event is a sign in request
        assertNotNull(LiveDataTestUtil.getValue(subject.performSignInEvent)?.peekContent())
    }

    @Test
    fun testPostSignOut() = coroutineRule.runTest {
        val subject = FirebaseSignInViewModelDelegate(
            FakeObserveUserAuthStateUseCase(Result.Success(null), coroutineRule.testDispatcher),
            createNotificationsPrefIsShownUseCase(),
            coroutineRule.testDispatcher,
            coroutineRule.testDispatcher
        )

        subject.emitSignOutRequest()
        assertNotNull(LiveDataTestUtil.getValue(subject.performSignOutEvent)?.peekContent())
    }

    private fun createNotificationsPrefIsShownUseCase(): NotificationsPrefIsShownUseCase {
        return NotificationsPrefIsShownUseCase(
            FakePreferenceStorage(), coroutineRule.testDispatcher
        )
    }
}
