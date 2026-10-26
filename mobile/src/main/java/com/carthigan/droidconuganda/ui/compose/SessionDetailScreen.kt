package com.carthigan.droidconuganda.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.model.Session
import com.carthigan.droidconuganda.shared.util.TimeUtils
import com.carthigan.droidconuganda.ui.SnackbarMessage
import com.carthigan.droidconuganda.ui.compose.components.BackButton
import com.carthigan.droidconuganda.ui.compose.components.ConferenceHtmlText
import com.carthigan.droidconuganda.ui.compose.components.ConferenceIcon
import com.carthigan.droidconuganda.ui.compose.components.ConferenceMessage
import com.carthigan.droidconuganda.ui.compose.components.LiveSessionRibbon
import com.carthigan.droidconuganda.ui.compose.components.SessionCard
import com.carthigan.droidconuganda.ui.compose.components.SpeakerRow
import com.carthigan.droidconuganda.ui.compose.components.sessionColors
import com.carthigan.droidconuganda.ui.sessiondetail.SessionDetailViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionDetailRoute(
    sessionId: String, viewModel: SessionDetailViewModel, onBack: () -> Unit,
    onSpeaker: (String) -> Unit, onSession: (String) -> Unit, onShare: (Session) -> Unit,
    onOpenUrl: (String) -> Unit, onMessage: (SnackbarMessage) -> Unit,
    onNotifications: () -> Unit
) {
    val session by viewModel.session.observeAsState()
    val userEvent by viewModel.userEvent.observeAsState()
    val related by viewModel.relatedUserSessions.observeAsState(emptyList())
    val zone by viewModel.timeZoneId.observeAsState(TimeUtils.CONFERENCE_TIMEZONE)
    val failed by viewModel.loadFailed.observeAsState(false)
    val current = session
    val liveDescription = stringResource(R.string.ui_happening_now)
    var isLive by remember(current?.id) {
        mutableStateOf(current?.let { isSessionLive(it, viewModel.currentTime()) } == true)
    }
    val liveScope = rememberCoroutineScope()
    LifecycleResumeEffect(current?.id) {
        val ticker = liveScope.launch {
            while (isActive) {
                val value = current ?: break
                val now = viewModel.currentTime()
                isLive = isSessionLive(value, now)
                delay(millisUntilNextSessionBoundary(value, now))
            }
        }
        onPauseOrDispose { ticker.cancel() }
    }
    LaunchedEffect(sessionId) { viewModel.setSessionId(sessionId) }
    EventEffect(viewModel.snackBarMessage, onMessage)
    EventEffect(viewModel.navigateToYouTubeAction, onOpenUrl)
    EventEffect(viewModel.shouldShowNotificationsPrefAction) { if (it) onNotifications() }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Text(stringResource(R.string.ui_session_details), Modifier.weight(1f).padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium)
            session?.let { current ->
                IconButton(onClick = { onShare(current) }) {
                    ConferenceIcon(R.drawable.ic_share, stringResource(R.string.menu_item_share_session))
                }
            }
        }
        if (current == null) {
            ConferenceMessage(
                stringResource(if (failed) R.string.ui_details_unavailable else R.string.ui_loading),
                stringResource(if (failed) R.string.ui_details_error_body else R.string.ui_loading_details),
                loading = !failed, actionLabel = if (failed) stringResource(R.string.ui_retry) else null,
                onAction = if (failed) ({ viewModel.retry() }) else null)
            return@Column
        }
        LazyColumn(Modifier.weight(1f).testTag("session_detail_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item {
                val (container, content) = sessionColors(current)
                Box(Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().semantics {
                            if (isLive) stateDescription = liveDescription
                        },
                        color = container,
                        contentColor = content,
                        shape = RoundedCornerShape(36.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            Text(current.displayTags.joinToString(" · ") { it.displayName }
                                .ifEmpty { current.type.displayName }, style = MaterialTheme.typography.labelLarge)
                            Text(current.title, style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.semantics { heading() })
                            Text(conferenceDate(current.startTime, zone, "EEEE, d MMMM"),
                                style = MaterialTheme.typography.titleMedium)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${conferenceTime(current.startTime, zone)} – ${conferenceTime(current.endTime, zone)}",
                                    style = MaterialTheme.typography.headlineSmall)
                                Text(stringResource(R.string.ui_duration_minutes, current.duration / 60_000),
                                    style = MaterialTheme.typography.bodyLarge)
                            }
                            current.room?.let { Text(it.name, style = MaterialTheme.typography.titleMedium) }
                            Text(zone.id, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (isLive) {
                        LiveSessionRibbon(Modifier.matchParentSize(), cornerRadius = 36.dp)
                    }
                }
            }
            if (current.abstract.isNotBlank()) item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.ui_about_session), style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() })
                    ConferenceHtmlText(current.abstract, onOpenUrl)
                }
            }
            if (current.speakers.isNotEmpty()) {
                item { Text(stringResource(R.string.session_detail_speakers_header),
                    style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() }) }
                items(current.speakers.toList(), key = { it.id }) { speaker ->
                    SpeakerRow(speaker, onClick = { onSpeaker(speaker.id) })
                }
            }
            if (current.hasVideo) item {
                FilledTonalButton(onClick = { onOpenUrl(current.youTubeUrl) }) {
                    ConferenceIcon(R.drawable.ic_play, null)
                    Text(stringResource(R.string.ui_watch_session), Modifier.padding(start = 8.dp))
                }
            }
            if (related.isNotEmpty()) {
                item { Text(stringResource(R.string.session_detail_related_header),
                    style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() }) }
                items(related, key = { it.session.id }) { userSession ->
                    SessionCard(userSession, zone, { onSession(userSession.session.id) },
                        { viewModel.onStarClicked(userSession) })
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
        Surface(color = MaterialTheme.colorScheme.background) {
            Button(onClick = { viewModel.onStarClicked() }, enabled = userEvent != null,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                ConferenceIcon(if (userEvent?.isStarred == true) R.drawable.ic_bookmark_filled
                    else R.drawable.ic_bookmark_outline, null)
                Text(stringResource(if (userEvent?.isStarred == true) R.string.ui_saved_to_schedule
                    else R.string.ui_save_to_schedule), Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp))
            }
        }
    }
}
