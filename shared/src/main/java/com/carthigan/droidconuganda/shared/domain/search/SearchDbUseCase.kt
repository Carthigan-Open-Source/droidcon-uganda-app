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

package com.carthigan.droidconuganda.shared.domain.search

import com.carthigan.droidconuganda.shared.data.ConferenceDataRepository
import com.carthigan.droidconuganda.shared.data.db.AppDatabase
import com.carthigan.droidconuganda.shared.data.prefs.PreferenceStorage
import com.carthigan.droidconuganda.shared.data.prefs.UserIsAttendee.REMOTE
import com.carthigan.droidconuganda.shared.data.session.SessionRepository
import com.carthigan.droidconuganda.shared.di.IoDispatcher
import com.carthigan.droidconuganda.shared.domain.UseCase
import com.carthigan.droidconuganda.shared.domain.search.Searchable.SearchedSession
import com.carthigan.droidconuganda.shared.domain.search.Searchable.SearchedSpeaker
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

private const val MAX_WORD_COUNT_QUERY = 5
/**
 * Performs a search in the database from a query string.
 */
class SearchDbUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val conferenceRepository: ConferenceDataRepository,
    private val appDatabase: AppDatabase,
    private val preferenceStorage: PreferenceStorage,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : UseCase<String, List<Searchable>>(ioDispatcher) {

    override fun execute(parameters: String): List<Searchable> {
        // Add '*' the query for FTS with multiple tokens
        val query = if (parameters.isNotEmpty()) {
            parameters.split(", ", " ", ",").take(MAX_WORD_COUNT_QUERY).map {
                "${it.lowercase()}*"
            }.joinToString(" AND ")
        } else {
            parameters
        }

        val sessionResults = appDatabase.sessionFtsDao().searchAllSessions(query).toSet()
        val speakerResults = appDatabase.speakerFtsDao().searchAllSpeakers(query).toSet()
        // Show all events if userIsAttendee is IN_PERSON or NO_ANSWER (means dialog dismissed)
        val isOnsiteAttendee = preferenceStorage.userIsAttendee != REMOTE
        val searchedSessions = sessionRepository.getSessions(isOnsiteAttendee)
            .filter { session -> session.id in sessionResults }
            // Keynotes come first, followed by sessions, app reviews, game reviews ...
            .sortedBy { it.type }
            .map { SearchedSession(it) }
        val conferenceData = conferenceRepository.getOfflineConferenceData()
        val searchedSpeakers = conferenceData.speakers.filter {
            it.id in speakerResults
        }.map { SearchedSpeaker(it) }

        return searchedSessions.plus(searchedSpeakers)
    }
}
