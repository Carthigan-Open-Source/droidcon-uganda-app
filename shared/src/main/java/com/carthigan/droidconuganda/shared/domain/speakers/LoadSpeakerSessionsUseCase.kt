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

package com.carthigan.droidconuganda.shared.domain.speakers

import com.carthigan.droidconuganda.model.SessionId
import com.carthigan.droidconuganda.model.Speaker
import com.carthigan.droidconuganda.model.SpeakerId
import com.carthigan.droidconuganda.shared.data.ConferenceDataRepository
import com.carthigan.droidconuganda.shared.di.DefaultDispatcher
import com.carthigan.droidconuganda.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/**
 * Loads a [Speaker] and the IDs of any [com.carthigan.droidconuganda.model.Session]s
 * they are speaking in.
 */
open class LoadSpeakerUseCase @Inject constructor(
    private val conferenceDataRepository: ConferenceDataRepository,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
) : UseCase<SpeakerId, LoadSpeakerUseCaseResult>(defaultDispatcher) {

    override fun execute(parameters: SpeakerId): LoadSpeakerUseCaseResult {
        val speaker = conferenceDataRepository.getOfflineConferenceData().speakers
            .firstOrNull { it.id == parameters }
            ?: throw SpeakerNotFoundException("No speaker found with id $parameters")
        val sessionIds = conferenceDataRepository.getOfflineConferenceData().sessions
            .filter { it.speakers.find { it.id == parameters } != null }
            .map { it.id }
            .toSet()
        return LoadSpeakerUseCaseResult(speaker, sessionIds)
    }
}

data class LoadSpeakerUseCaseResult(
    val speaker: Speaker,
    val sessionIds: Set<SessionId>
)

class SpeakerNotFoundException(message: String) : Throwable(message)
