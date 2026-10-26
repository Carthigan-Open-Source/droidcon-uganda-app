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

package com.carthigan.droidconuganda.ui.sessiondetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.model.Session
import com.carthigan.droidconuganda.model.SessionId
import com.carthigan.droidconuganda.model.SpeakerId
import com.carthigan.droidconuganda.model.userdata.UserEvent
import com.carthigan.droidconuganda.model.userdata.UserSession
import com.carthigan.droidconuganda.shared.analytics.AnalyticsActions
import com.carthigan.droidconuganda.shared.analytics.AnalyticsHelper
import com.carthigan.droidconuganda.shared.domain.sessions.LoadUserSessionUseCase
import com.carthigan.droidconuganda.shared.domain.sessions.LoadUserSessionsUseCase
import com.carthigan.droidconuganda.shared.domain.sessions.ObserveConferenceDataUseCase
import com.carthigan.droidconuganda.shared.domain.settings.GetTimeZoneUseCase
import com.carthigan.droidconuganda.shared.domain.users.StarEventAndNotifyUseCase
import com.carthigan.droidconuganda.shared.domain.users.StarEventParameter
import com.carthigan.droidconuganda.shared.result.Event
import com.carthigan.droidconuganda.shared.result.Result
import com.carthigan.droidconuganda.shared.result.data
import com.carthigan.droidconuganda.shared.time.TimeProvider
import com.carthigan.droidconuganda.shared.util.SetIntervalLiveData.DefaultIntervalMapper
import com.carthigan.droidconuganda.shared.util.TimeUtils
import com.carthigan.droidconuganda.shared.util.map
import com.carthigan.droidconuganda.shared.util.setValueIfNew
import com.carthigan.droidconuganda.ui.SnackbarMessage
import com.carthigan.droidconuganda.ui.sessioncommon.EventActions
import com.carthigan.droidconuganda.ui.signin.SignInViewModelDelegate
import com.carthigan.droidconuganda.util.cancelIfActive
import com.carthigan.droidconuganda.util.ignoreFirst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.threeten.bp.Duration
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import javax.inject.Inject

private const val TEN_SECONDS = 10_000L
private const val SIXTY_SECONDS = 60_000L

/**
 * Loads [Session] data and exposes it to the session detail view.
 */
class SessionDetailViewModel @Inject constructor(
    private val signInViewModelDelegate: SignInViewModelDelegate,
    private val loadUserSessionUseCase: LoadUserSessionUseCase,
    private val loadRelatedSessionUseCase: LoadUserSessionsUseCase,
    private val starEventUseCase: StarEventAndNotifyUseCase,
    observeConferenceDataUseCase: ObserveConferenceDataUseCase,
    private val getTimeZoneUseCase: GetTimeZoneUseCase,
    private val timeProvider: TimeProvider,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel(), SessionDetailEventListener, EventActions,
    SignInViewModelDelegate by signInViewModelDelegate {

    // Keeps track of the coroutine that listens for a user session
    private var loadUserSessionJob: Job? = null

    // Keeps track of the coroutine that listens for related user sessions
    private var loadRelatedSessionJob: Job? = null

    private val sessionTimeRelativeState: LiveData<TimeUtils.SessionRelativeTimeState>

    private val _timeZoneId = MutableLiveData(ZoneId.systemDefault())
    val timeZoneId: LiveData<ZoneId> = _timeZoneId

    fun currentTime(): ZonedDateTime = timeProvider.nowZoned()

    private val _errorMessage = MediatorLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    private val _snackBarMessage = MediatorLiveData<Event<SnackbarMessage>>()
    val snackBarMessage: LiveData<Event<SnackbarMessage>>
        get() = _snackBarMessage

    private val _navigateToSignInDialogAction = MutableLiveData<Event<Unit>>()
    val navigateToSignInDialogAction: LiveData<Event<Unit>>
        get() = _navigateToSignInDialogAction

    val navigateToYouTubeAction = MutableLiveData<Event<String>>()

    private val _session = MediatorLiveData<Session>()
    val session: LiveData<Session>
        get() = _session
    private val _loadFailed = MutableLiveData(false)
    val loadFailed: LiveData<Boolean> = _loadFailed

    private val _userEvent = MediatorLiveData<UserEvent>()
    val userEvent: LiveData<UserEvent>
        get() = _userEvent

    private val _relatedUserSessions = MediatorLiveData<List<UserSession>>()
    val relatedUserSessions: LiveData<List<UserSession>>
        get() = _relatedUserSessions

    val showRateButton: LiveData<Boolean>
    val hasPhotoOrVideo: LiveData<Boolean>
    val isPlayable: LiveData<Boolean>
    val hasSpeakers: LiveData<Boolean>
    val hasRelated: LiveData<Boolean>
    val timeUntilStart: LiveData<Duration?>

    private val sessionId = MutableLiveData<SessionId?>()

    private val _navigateToSessionAction = MutableLiveData<Event<SessionId>>()
    val navigateToSessionAction: LiveData<Event<SessionId>>
        get() = _navigateToSessionAction

    private val _navigateToSpeakerDetail = MutableLiveData<Event<SpeakerId>>()
    val navigateToSpeakerDetail: LiveData<Event<SpeakerId>>
        get() = _navigateToSpeakerDetail

    init {
        /**
         *  If the user changes, load new data for them.
         *  It's safe to ignore the first element since setting the sessionId already
         *  refreshes the sessions CurrentFirebaseUser will always emit the current user as soon
         *  as collect happens since the implementation is backed by a ConflatedBroadcastChannel.
         */
        viewModelScope.launch {
            currentFirebaseUser.ignoreFirst().collect {
                Timber.d("CurrentFirebaseUser changed, refreshing")
                refreshUserSession()
            }
        }

        viewModelScope.launch {
            // Refresh screen if conference data changes.
            observeConferenceDataUseCase(Any()).collect {
                Timber.d("Detected new data in conference data repository")
                refreshUserSession()
            }
        }

        // If the session ID changes, load new data for it
        _session.addSource(sessionId) {
            Timber.d("SessionId changed, refreshing")
            refreshUserSession()
        }

        // TODO this should also be called when session state is stale (b/74242921)
        // If there's a new session, update the relative time status (before, during, after...)
        sessionTimeRelativeState = session.map { currentSession ->
            TimeUtils.getSessionState(currentSession, ZonedDateTime.now())
        }

        hasPhotoOrVideo = session.map { currentSession ->
            !currentSession?.photoUrl.isNullOrEmpty() || !currentSession?.youTubeUrl.isNullOrEmpty()
        }

        isPlayable = session.map { currentSession ->
            currentSession.hasVideo
        }

        showRateButton = sessionTimeRelativeState.map { currentState ->
            // TODO: uncomment when rate session logic is hooked up
            // currentState == TimeUtils.SessionRelativeTimeState.AFTER
            false
        }

        hasSpeakers = session.map { currentSession ->
            currentSession.speakers.isNotEmpty()
        }

        hasRelated = session.map { currentSession ->
            currentSession.relatedSessions.isNotEmpty()
        }

        // Updates periodically with a special [IntervalLiveData]
        timeUntilStart = DefaultIntervalMapper.mapAtInterval(session, TEN_SECONDS) { session ->
            session?.startTime?.let { startTime ->
                val duration = Duration.between(timeProvider.now(), startTime)
                val minutes = duration.toMinutes()
                when (minutes) {
                    in 1..5 -> duration
                    else -> null
                }
            }
        }
    }

    override fun onStarClicked() {

        val userEventSnapshot = userEvent.value ?: return
        val sessionSnapshot = session.value ?: return
        onStarClicked(UserSession(sessionSnapshot, userEventSnapshot))
    }

    override fun onLoginClicked() {
        if (!isSignedIn()) {
            Timber.d("Showing Sign-in dialog")
            _navigateToSignInDialogAction.value = Event(Unit)
        }
    }

    // copied from SchedVM, TODO refactor
    override fun openEventDetail(id: SessionId) {
        _navigateToSessionAction.value = Event(id)
    }

    override fun onStarClicked(userSession: UserSession) {
        if (!isSignedIn()) {
            Timber.d("Showing Sign-in dialog after star click")
            _navigateToSignInDialogAction.value = Event(Unit)
            return
        }
        val newIsStarredState = !userSession.userEvent.isStarred

        val sessionTitle = session.value?.title
        if (sessionTitle != null && newIsStarredState) {
            analyticsHelper.logUiEvent(sessionTitle, AnalyticsActions.STARRED)
        } else {
            Timber.d("Session title is null, can't log")
        }

        // Update the snackbar message optimistically.
        val snackbarMessage = if (newIsStarredState) {
            SnackbarMessage(R.string.event_starred, R.string.got_it)
        } else {
            SnackbarMessage(R.string.event_unstarred)
        }
        _snackBarMessage.postValue(Event(snackbarMessage))

        viewModelScope.launch {
            getUserId()?.let {
                val result = starEventUseCase(
                    StarEventParameter(
                        it, userSession.copy(
                            userEvent = userSession.userEvent.copy(isStarred = newIsStarredState)
                        )
                    )
                )

                // Show an error message if a star request fails
                if (result is Result.Error) {
                    _snackBarMessage.postValue(Event(SnackbarMessage(R.string.event_star_error)))
                }
            }
        }
    }

    override fun onSpeakerClicked(speakerId: SpeakerId) {
        _navigateToSpeakerDetail.postValue(Event(speakerId))
    }

    // TODO: write tests b/74611561
    fun setSessionId(newSessionId: SessionId?) {
        if (newSessionId == null) {
            Timber.e("Session ID is null")
            return
        }
        sessionId.setValueIfNew(newSessionId)
    }

    /**
     * Called by the UI when play button is clicked
     */
    fun onPlayVideo() {
        val currentSession = session.value
        if (currentSession?.hasVideo == true) {
            navigateToYouTubeAction.value = Event(requireSession().youTubeUrl)
        }
    }

    private fun refreshUserSession() {
        getSessionId()?.let {
            Timber.d("Refreshing data with session ID $it and user ${getUserId()}")
            listenForUserSessionChanges(it)
        }
    }

    fun retry() {
        refreshUserSession()
    }

    private fun listenForUserSessionChanges(sessionId: SessionId) {
        // Cancels listening for the old session
        loadUserSessionJob.cancelIfActive()

        loadUserSessionJob = viewModelScope.launch {
            _loadFailed.value = false
            // Fech the time zone in case it's the cause of the data refresh.
            fetchTimeZone()
            loadUserSessionUseCase(getUserId() to sessionId).collect { loadResult ->
                _loadFailed.value = loadResult is Result.Error
                // If there's a new result with data, update the session
                loadResult.data?.userSession?.session?.let { session ->
                    _session.value = session
                }
                // If there's a new result with data, update the UserEvent
                loadResult.data?.userSession?.userEvent?.let { userEvent ->
                    _userEvent.value = userEvent
                }

                // Cancels listening for old related user sessions
                loadRelatedSessionJob.cancelIfActive()

                // If there's a new Session, then load any related sessions
                loadResult.data?.userSession?.session?.let { session ->
                    val related = session.relatedSessions
                    if (related.isNotEmpty()) {
                        listenForRelatedSessions(related)
                    }
                }
            }
        }
    }

    private suspend fun fetchTimeZone() {
        val result = getTimeZoneUseCase(Unit)
        withContext(Dispatchers.Main) {
            result.data?.let { inConferenceTimeZone ->
                _timeZoneId.value = if (inConferenceTimeZone) {
                    TimeUtils.CONFERENCE_TIMEZONE
                } else {
                    ZoneId.systemDefault()
                }
            }
        }
    }

    private suspend fun listenForRelatedSessions(related: Set<SessionId>) {
        // if this fails, we don't want to propagate the error and
        // stop listening for user session changes
        supervisorScope {
            loadRelatedSessionJob = launch {
                loadRelatedSessionUseCase(getUserId() to related).collect {
                    it.data?.let {
                        _relatedUserSessions.value = it.userSessions
                    }
                }
            }
        }
    }

    /**
     * Returns the current session ID or null if not available.
     */
    private fun getSessionId(): SessionId? {
        return sessionId.value
    }

    private fun requireSession(): Session {
        return session.value ?: throw IllegalStateException("Session should not be null")
    }
}

interface SessionDetailEventListener {

    fun onStarClicked()

    fun onLoginClicked()

    fun onSpeakerClicked(speakerId: SpeakerId)
}
