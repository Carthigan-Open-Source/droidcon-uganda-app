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

package com.carthigan.droidconuganda.shared.domain.sessions

import com.carthigan.droidconuganda.model.Session
import com.carthigan.droidconuganda.shared.data.userevent.SessionAndUserEventRepository
import com.carthigan.droidconuganda.shared.di.DefaultDispatcher
import com.carthigan.droidconuganda.shared.notifications.SessionAlarmManager
import com.carthigan.droidconuganda.shared.result.Result.Success
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sets a notification for each session that is starred by the user.
 */
@Singleton
class NotificationAlarmUpdater @Inject constructor(
    private val alarmManager: SessionAlarmManager,
    private val repository: SessionAndUserEventRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    // Coroutines scope for NotificationAlarmUpdater background work
    private val alarmUpdaterScope: CoroutineScope =
        CoroutineScope(defaultDispatcher + SupervisorJob())

    fun updateAll(userId: String) {
        alarmUpdaterScope.launch {
            val isUserAttendee = true // Do not filter out any sessions
            val events = repository.getObservableUserEvents(userId, isUserAttendee).first()
            when (events) {
                is Success -> processEvents(userId, events.data)
                is Error -> Timber.e(events.cause)
                else -> { /* Nothing to do */ }
            }
        }
    }

    private fun processEvents(
        userId: String,
        sessions: LoadUserSessionsByDayUseCaseResult
    ) {
        Timber.d("Setting all the alarms for user $userId")
        val startWork = System.currentTimeMillis()
        sessions.userSessionsPerDay.forEach { day ->
            day.value.forEach {
                if (it.userEvent.isStarred) {
                    alarmManager.setAlarmForSession(it.session)
                }
            }
        }
        Timber.d("Work finished in ${System.currentTimeMillis() - startWork} ms")
    }

    fun cancelAll() {
        alarmUpdaterScope.launch {
            val isUserAttendee = true // Do not filter out any sessions
            val events = repository.getObservableUserEvents(null, isUserAttendee).first()
            when (events) {
                is Success -> cancelAllSessions(events.data)
                is Error -> Timber.e(events.cause)
                else -> { /* Nothing to do */ }
            }
        }
    }

    private fun cancelAllSessions(sessions: LoadUserSessionsByDayUseCaseResult) {
        Timber.d("Cancelling all the alarms")
        sessions.userSessionsPerDay.forEach { day ->
            day.value.forEach {
                alarmManager.cancelAlarmForSession(it.session.id)
            }
        }
    }
}

@Singleton
open class StarNotificationAlarmUpdater @Inject constructor(
    private val alarmManager: SessionAlarmManager

) {
    open fun updateSession(session: Session, starred: Boolean) {
        if (starred) {
            alarmManager.setAlarmForSession(session)
        } else {
            alarmManager.cancelAlarmForSession(session.id)
        }
    }
}
