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

package com.carthigan.droidconuganda.shared.domain.users

import com.carthigan.droidconuganda.model.ConferenceDay
import com.carthigan.droidconuganda.model.SessionId
import com.carthigan.droidconuganda.model.userdata.UserEvent
import com.carthigan.droidconuganda.model.userdata.UserSession
import com.carthigan.droidconuganda.shared.data.session.DefaultSessionRepository
import com.carthigan.droidconuganda.shared.data.userevent.DefaultSessionAndUserEventRepository
import com.carthigan.droidconuganda.shared.data.userevent.SessionAndUserEventRepository
import com.carthigan.droidconuganda.shared.domain.repository.TestUserEventDataSource
import com.carthigan.droidconuganda.shared.domain.sessions.LoadUserSessionUseCaseResult
import com.carthigan.droidconuganda.shared.domain.sessions.LoadUserSessionsByDayUseCaseResult
import com.carthigan.droidconuganda.shared.domain.sessions.StarNotificationAlarmUpdater
import com.carthigan.droidconuganda.shared.model.TestDataRepository
import com.carthigan.droidconuganda.shared.notifications.SessionAlarmManager
import com.carthigan.droidconuganda.shared.result.Result
import com.carthigan.droidconuganda.test.data.MainCoroutineRule
import com.carthigan.droidconuganda.test.data.TestData
import com.carthigan.droidconuganda.test.data.runTest
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import kotlinx.coroutines.flow.Flow
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [StarEventAndNotifyUseCase]
 */
class StarEventAndNotifyUseCaseTest {

    // Overrides Dispatchers.Main used in Coroutines
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @Test
    fun sessionIsStarredSuccessfully() = coroutineRule.runTest {
        val testUserEventRepository = DefaultSessionAndUserEventRepository(
            TestUserEventDataSource(), DefaultSessionRepository(TestDataRepository)
        )
        val useCase = StarEventAndNotifyUseCase(
            testUserEventRepository, mock {},
            coroutineRule.testDispatcher
        )

        val result = useCase(StarEventParameter("userIdTest", TestData.userSession1))

        Assert.assertEquals(result, Result.Success(StarUpdatedStatus.STARRED))
    }

    @Test
    fun sessionIsStarredUnsuccessfully() = coroutineRule.runTest {
        val alarmManager: SessionAlarmManager = mock()
        doNothing().whenever(alarmManager).cancelAlarmForSession(any())
        val starNotificationAlarmUpdater = StarNotificationAlarmUpdater(alarmManager)

        val useCase = StarEventAndNotifyUseCase(
            FailingSessionAndUserEventRepository,
            starNotificationAlarmUpdater,
            coroutineRule.testDispatcher
        )

        val result = useCase(StarEventParameter("userIdTest", TestData.userSession0))

        assertTrue(result is Result.Error)
    }

    @Test
    fun sessionIsStarredAndNotificationSet() = coroutineRule.runTest {
        val testUserEventRepository = DefaultSessionAndUserEventRepository(
            TestUserEventDataSource(), DefaultSessionRepository(TestDataRepository)
        )
        val updater: StarNotificationAlarmUpdater = mock {}

        doNothing().whenever(updater).updateSession(any(), any())

        val useCase = StarEventAndNotifyUseCase(
            testUserEventRepository, updater, coroutineRule.testDispatcher
        )

        useCase(StarEventParameter("userIdTest", TestData.userSession0))

        verify(updater).updateSession(TestData.userSession0.session, false)
    }
}

val FailingSessionAndUserEventRepository = object : SessionAndUserEventRepository {

    override suspend fun starEvent(
        userId: String,
        userEvent: UserEvent
    ): Result<StarUpdatedStatus> = Result.Error(Exception("Test"))

    override fun getObservableUserEvents(
        userId: String?,
        userIsAttendee: Boolean
    ): Flow<Result<LoadUserSessionsByDayUseCaseResult>> {
        throw NotImplementedError()
    }

    override fun getObservableUserEvent(
        userId: String?,
        eventId: SessionId
    ): Flow<Result<LoadUserSessionUseCaseResult>> {
        throw NotImplementedError()
    }

    override fun getUserEvents(userId: String?): List<UserEvent> {
        throw NotImplementedError()
    }

    override fun getUserSession(userId: String, sessionId: SessionId): UserSession {
        throw NotImplementedError()
    }

    override fun getConferenceDays(): List<ConferenceDay> {
        throw NotImplementedError()
    }
}
