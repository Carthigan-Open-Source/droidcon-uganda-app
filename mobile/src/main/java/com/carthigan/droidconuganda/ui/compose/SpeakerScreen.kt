package com.carthigan.droidconuganda.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.ui.SnackbarMessage
import com.carthigan.droidconuganda.ui.compose.components.BackButton
import com.carthigan.droidconuganda.ui.compose.components.ConferenceHtmlText
import com.carthigan.droidconuganda.ui.compose.components.ConferenceMessage
import com.carthigan.droidconuganda.ui.compose.components.SessionCard
import com.carthigan.droidconuganda.ui.compose.components.SpeakerAvatar
import com.carthigan.droidconuganda.ui.compose.theme.LocalConferencePalette
import com.carthigan.droidconuganda.ui.speaker.SpeakerViewModel
import org.threeten.bp.ZoneId

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeakerRoute(speakerId: String, viewModel: SpeakerViewModel, zone: ZoneId,
    onBack: () -> Unit, onSession: (String) -> Unit, onOpenUrl: (String) -> Unit,
    onMessage: (SnackbarMessage) -> Unit) {
    val speaker by viewModel.speaker.observeAsState()
    val sessions by viewModel.speakerUserSessions.observeAsState(emptyList())
    val failed by viewModel.loadFailed.observeAsState(false)
    val palette = LocalConferencePalette.current
    LaunchedEffect(speakerId) { viewModel.setSpeakerId(speakerId) }
    EventEffect(viewModel.snackBarMessage, onMessage)

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Text(stringResource(R.string.ui_speaker_details), Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.titleMedium)
        }
        LazyColumn(Modifier.weight(1f).testTag("speaker_list"), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
            val current = speaker
            if (current == null) {
                item { ConferenceMessage(
                    stringResource(if (failed) R.string.ui_details_unavailable else R.string.ui_loading),
                    stringResource(if (failed) R.string.ui_details_error_body else R.string.ui_loading_details),
                    loading = !failed, actionLabel = if (failed) stringResource(R.string.ui_retry) else null,
                    onAction = if (failed) ({ viewModel.setSpeakerId(speakerId) }) else null) }
            } else {
                item {
                    Surface(color = palette.lavender, contentColor = palette.onLavender,
                        shape = RoundedCornerShape(36.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            SpeakerAvatar(current, 88.dp)
                            Text(current.name, style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.semantics { heading() })
                            if (current.company.isNotBlank()) Text(current.company, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                if (current.abstract.isNotBlank()) item { ConferenceHtmlText(current.abstract, onOpenUrl) }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            R.string.speaker_link_website to current.websiteUrl,
                            R.string.speaker_link_twitter to current.twitterUrl,
                            R.string.speaker_link_github to current.githubUrl,
                            R.string.speaker_link_linkedin to current.linkedInUrl
                        ).forEach { (label, url) ->
                            if (!url.isNullOrBlank()) TextButton(onClick = { onOpenUrl(url) }) { Text(stringResource(label)) }
                        }
                    }
                }
                if (sessions.isNotEmpty()) {
                    item { Text(stringResource(R.string.ui_speaker_sessions), style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() }) }
                    items(sessions, key = { it.session.id }) { userSession ->
                        SessionCard(userSession, zone, { onSession(userSession.session.id) },
                            { viewModel.onStarClicked(userSession) })
                    }
                }
            }
        }
    }
}
