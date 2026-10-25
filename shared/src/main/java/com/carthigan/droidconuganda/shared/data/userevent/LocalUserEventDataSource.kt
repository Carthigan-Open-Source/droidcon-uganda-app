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

package com.carthigan.droidconuganda.shared.data.userevent

import android.content.Context
import com.carthigan.droidconuganda.model.SessionId
import com.carthigan.droidconuganda.model.userdata.UserEvent
import com.carthigan.droidconuganda.shared.domain.users.StarUpdatedStatus
import com.carthigan.droidconuganda.shared.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

/**
 * Local, on-device [UserEventDataSource]. Starred sessions are persisted in
 * SharedPreferences — no account and no cloud sync.
 */
@Singleton
class LocalUserEventDataSource(context: Context) : UserEventDataSource {

    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val starredIds: MutableStateFlow<Set<SessionId>> =
        MutableStateFlow(loadStarredIds())

    override fun getObservableUserEvents(userId: String): Flow<UserEventsResult> =
        starredIds.map { ids -> UserEventsResult(ids.map { UserEvent(id = it, isStarred = true) }) }

    override fun getObservableUserEvent(userId: String, eventId: SessionId): Flow<UserEventResult> =
        starredIds.map { ids ->
            UserEventResult(ids.firstOrNull { it == eventId }?.let { UserEvent(id = it, isStarred = true) })
        }

    override fun getUserEvents(userId: String): List<UserEvent> =
        starredIds.value.map { UserEvent(id = it, isStarred = true) }

    override fun getUserEvent(userId: String, eventId: SessionId): UserEvent? =
        if (eventId in starredIds.value) UserEvent(id = eventId, isStarred = true) else null

    override suspend fun starEvent(userId: String, userEvent: UserEvent): Result<StarUpdatedStatus> {
        val updated = starredIds.value.toMutableSet()
        val status = if (userEvent.isStarred) {
            updated.add(userEvent.id)
            StarUpdatedStatus.STARRED
        } else {
            updated.remove(userEvent.id)
            StarUpdatedStatus.UNSTARRED
        }
        prefs.edit().putStringSet(KEY_STARRED_IDS, updated).apply()
        starredIds.value = updated
        return Result.Success(status)
    }

    private fun loadStarredIds(): Set<SessionId> =
        prefs.getStringSet(KEY_STARRED_IDS, emptySet())?.toSet() ?: emptySet()

    companion object {
        private const val PREFS_NAME = "local_user_events"
        private const val KEY_STARRED_IDS = "starred_ids"
    }
}
