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

package com.carthigan.droidconuganda.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carthigan.droidconuganda.model.Session
import com.carthigan.droidconuganda.model.SessionId
import com.carthigan.droidconuganda.model.SessionType
import com.carthigan.droidconuganda.model.SpeakerId
import com.carthigan.droidconuganda.shared.analytics.AnalyticsActions
import com.carthigan.droidconuganda.shared.analytics.AnalyticsHelper
import com.carthigan.droidconuganda.shared.domain.search.SearchDbUseCase
import com.carthigan.droidconuganda.shared.domain.search.SearchUseCase
import com.carthigan.droidconuganda.shared.domain.search.Searchable
import com.carthigan.droidconuganda.shared.domain.search.Searchable.SearchedSession
import com.carthigan.droidconuganda.shared.domain.search.Searchable.SearchedSpeaker
import com.carthigan.droidconuganda.shared.result.Event
import com.carthigan.droidconuganda.shared.result.Result
import com.carthigan.droidconuganda.shared.result.successOr
import com.carthigan.droidconuganda.ui.search.SearchResultType.SESSION
import com.carthigan.droidconuganda.ui.search.SearchResultType.SPEAKER
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val analyticsHelper: AnalyticsHelper,
    private val loadSearchResultsUseCase: SearchUseCase,
    private val loadDbSearchResultsUseCase: SearchDbUseCase
) : ViewModel(), SearchResultActionHandler {

    // TODO: Add Dagger Flags
//    @Inject
//    @JvmField
//    @SearchUsingRoomEnabledFlag
    var searchUsingRoomFeatureEnabled: Boolean = true

    private val _navigateToSessionAction = MutableLiveData<Event<SessionId>>()
    val navigateToSessionAction: LiveData<Event<SessionId>> = _navigateToSessionAction

    private val _navigateToSpeakerAction = MutableLiveData<Event<SpeakerId>>()
    val navigateToSpeakerAction: LiveData<Event<SpeakerId>> = _navigateToSpeakerAction

    private val _searchResults = MediatorLiveData<List<SearchResult>>()
    val searchResults: LiveData<List<SearchResult>> = _searchResults

    private val _isEmpty = MediatorLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty
    private var searchJob: Job? = null
    private var searchGeneration = 0
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private val _loadFailed = MutableLiveData(false)
    val loadFailed: LiveData<Boolean> = _loadFailed

    override fun openSearchResult(searchResult: SearchResult) {
        when (searchResult.type) {
            SESSION -> {
                val sessionId = searchResult.objectId
                analyticsHelper.logUiEvent("Session: $sessionId",
                    AnalyticsActions.SEARCH_RESULT_CLICK)
                _navigateToSessionAction.value = Event(sessionId)
            }
            SPEAKER -> {
                val speakerId = searchResult.objectId
                analyticsHelper.logUiEvent("Speaker: $speakerId",
                    AnalyticsActions.SEARCH_RESULT_CLICK)
                _navigateToSpeakerAction.value = Event(speakerId)
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        val generation = ++searchGeneration
        searchJob?.cancel()
        if (newQuery.trim().length < 2) {
            onQueryCleared()
            return
        }
        analyticsHelper.logUiEvent("Query: $newQuery", AnalyticsActions.SEARCH_QUERY_SUBMIT)
        executeSearch(newQuery, generation)
    }

    private fun executeSearch(query: String, generation: Int) {
        _isLoading.value = true
        _loadFailed.value = false
        searchJob = viewModelScope.launch {
            try {
                val result = if (searchUsingRoomFeatureEnabled) {
                    loadDbSearchResultsUseCase(query.trim())
                } else {
                    loadSearchResultsUseCase(query.trim())
                }
                if (generation == searchGeneration) {
                    _loadFailed.value = result is Result.Error
                    processSearchResult(result)
                }
            } finally {
                if (generation == searchGeneration) _isLoading.value = false
            }
        }
    }

    private fun processSearchResult(result: Result<List<Searchable>>) {
        val searchResults = (result as? Result.Success)?.data ?: emptyList()
        _searchResults.value = searchResults.map { searched ->
            when (searched) {
                is SearchedSession -> {
                    val session = searched.session
                    SearchResult(
                        session.title,
                        sessionSearchSubtitle(session),
                        SESSION,
                        session.id
                    )
                }
                is SearchedSpeaker -> {
                    val speaker = searched.speaker
                    SearchResult(
                        speaker.name,
                        "Speaker",
                        SPEAKER,
                        speaker.id
                    )
                }
            }
        }

        _isEmpty.value = result.successOr(null).isNullOrEmpty()
    }

    private fun sessionSearchSubtitle(session: Session): String {
        val speakers = session.speakers.joinToString(", ") { it.name }.trim()
        if (speakers.isNotEmpty()) return speakers
        val tagName = session.displayTags.firstOrNull()?.displayName.orEmpty()
        if (tagName.isNotEmpty()) return tagName
        return session.type.takeIf { it != SessionType.UNKNOWN }?.displayName.orEmpty()
    }

    private fun onQueryCleared() {
        _isLoading.value = false
        _loadFailed.value = false
        _searchResults.value = emptyList()
        // Explicitly set false to not show the "No results" state
        _isEmpty.value = false
    }
}
