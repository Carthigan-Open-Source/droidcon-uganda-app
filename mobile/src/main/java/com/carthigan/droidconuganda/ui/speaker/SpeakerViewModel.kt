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

package com.carthigan.droidconuganda.ui.speaker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carthigan.droidconuganda.model.Speaker
import com.carthigan.droidconuganda.model.SpeakerId
import com.carthigan.droidconuganda.model.userdata.UserSession
import com.carthigan.droidconuganda.shared.analytics.AnalyticsActions
import com.carthigan.droidconuganda.shared.analytics.AnalyticsHelper
import com.carthigan.droidconuganda.shared.domain.sessions.LoadUserSessionsUseCase
import com.carthigan.droidconuganda.shared.domain.speakers.LoadSpeakerUseCase
import com.carthigan.droidconuganda.shared.domain.speakers.LoadSpeakerUseCaseResult
import com.carthigan.droidconuganda.shared.result.Result
import com.carthigan.droidconuganda.shared.result.data
import com.carthigan.droidconuganda.shared.result.updateOnSuccess
import com.carthigan.droidconuganda.shared.util.map
import com.carthigan.droidconuganda.ui.sessioncommon.EventActionsViewModelDelegate
import com.carthigan.droidconuganda.ui.signin.SignInViewModelDelegate
import com.carthigan.droidconuganda.util.cancelIfActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Loads a [Speaker] and their sessions, handles event actions.
 */
class SpeakerViewModel @Inject constructor(
    private val loadSpeakerUseCase: LoadSpeakerUseCase,
    private val loadSpeakerSessionsUseCase: LoadUserSessionsUseCase,
    signInViewModelDelegate: SignInViewModelDelegate,
    private val eventActionsViewModelDelegate: EventActionsViewModelDelegate,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel(),
    SignInViewModelDelegate by signInViewModelDelegate,
    EventActionsViewModelDelegate by eventActionsViewModelDelegate {

    // Keeps track of the coroutine that listens for speaker sessions
    private var loadSpeakerSessionsJob: Job? = null

    private val loadSpeakerUseCaseResult = MutableLiveData<LoadSpeakerUseCaseResult>()

    private val _speaker = MediatorLiveData<Speaker>()
    val speaker: LiveData<Speaker>
        get() = _speaker
    private val _loadFailed = MutableLiveData(false)
    val loadFailed: LiveData<Boolean> = _loadFailed

    private val _speakerUserSessions = MediatorLiveData<List<UserSession>>()
    val speakerUserSessions: LiveData<List<UserSession>>
        get() = _speakerUserSessions

    val hasProfileImage: LiveData<Boolean> = _speaker.map {
        !it?.imageUrl.isNullOrEmpty()
    }

    init {
        // If there's a new result with data, update speaker
        _speaker.addSource(loadSpeakerUseCaseResult) {
            _speaker.value = it.speaker
            loadSpeakerSessions(it)
        }
    }

    override fun onStarClicked(userSession: UserSession) {
        eventActionsViewModelDelegate.onStarClicked(userSession)

        // Only recording stars, not un-stars.  Since userEvent.isStarred reflects pre-click value,
        // checking for "old value starred, new value unstarred", in which case we don't record.
        if (userSession.userEvent.isStarred) {
            return
        }

        // Find the session
        val sessionId = userSession.userEvent.id
        val sessions = speakerUserSessions.value

        if (sessions != null) {
            val session = sessions.first { it.session.id == sessionId }.session
            analyticsHelper.logUiEvent(session.title, AnalyticsActions.STARRED)
        }
    }

    /**
     * Listens for a speaker session changes. When there are new changes,
     * _speakerUserSessions liveData is updated.
     */
    private fun loadSpeakerSessions(loadSpeakerUseCaseResult: LoadSpeakerUseCaseResult) {
        // If there's a new speaker, cancels listening for the old speaker's sessions
        loadSpeakerSessionsJob.cancelIfActive()

        loadSpeakerSessionsJob = viewModelScope.launch {
            loadSpeakerSessionsUseCase(
                getUserId() to loadSpeakerUseCaseResult.sessionIds
            ).collect {
                it.data?.let {
                    _speakerUserSessions.value = it.userSessions
                }
            }
        }
    }

    /**
     * Provides the speaker ID which initiates all data loading.
     */
    fun setSpeakerId(id: SpeakerId?) {
        if (id == null) {
            Timber.e("Speaker ID is null")
            return
        }
        viewModelScope.launch {
            _loadFailed.value = false
            val result = loadSpeakerUseCase(id)
            _loadFailed.value = result is Result.Error
            result.updateOnSuccess(loadSpeakerUseCaseResult)
        }
    }
}
