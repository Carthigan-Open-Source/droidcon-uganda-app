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

import com.carthigan.droidconuganda.model.SessionId
import com.carthigan.droidconuganda.model.userdata.UserSession
import com.carthigan.droidconuganda.shared.data.userevent.DefaultSessionAndUserEventRepository
import com.carthigan.droidconuganda.shared.di.IoDispatcher
import com.carthigan.droidconuganda.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/**
 * A [UseCase] that returns the [UserSession]s for a user.
 */
class LoadUserSessionSyncUseCase @Inject constructor(
    private val userEventRepository: DefaultSessionAndUserEventRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : UseCase<Pair<String, SessionId>, UserSession>(ioDispatcher) {

    override fun execute(parameters: Pair<String, SessionId>): UserSession {
        val (userId, eventId) = parameters

        return userEventRepository.getUserSession(userId, eventId)
    }
}
