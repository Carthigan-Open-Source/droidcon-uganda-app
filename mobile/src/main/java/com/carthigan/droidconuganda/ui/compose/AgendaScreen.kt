package com.carthigan.droidconuganda.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.shared.util.TimeUtils
import com.carthigan.droidconuganda.ui.agenda.AgendaViewModel
import com.carthigan.droidconuganda.ui.compose.components.ConferenceMessage
import com.carthigan.droidconuganda.ui.compose.components.PageHeading
import com.carthigan.droidconuganda.ui.compose.theme.LocalConferencePalette

@Composable
fun AgendaRoute(viewModel: AgendaViewModel) {
    val agenda by viewModel.agenda.observeAsState()
    val loading by viewModel.isLoading.observeAsState(true)
    val failed by viewModel.loadFailed.observeAsState(false)
    val zone by viewModel.timeZoneId.observeAsState(TimeUtils.CONFERENCE_TIMEZONE)
    val palette = LocalConferencePalette.current
    OnResumeEffect { viewModel.refreshAgenda() }

    LazyColumn(Modifier.fillMaxSize().testTag("agenda_list"), contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { PageHeading(stringResource(R.string.title_agenda), stringResource(R.string.ui_agenda_subtitle)) }
        item { TextButton(onClick = viewModel::refreshAgenda, enabled = !loading,
            modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(if (loading) R.string.ui_loading else R.string.ui_refresh))
        } }
        if (failed && !agenda.isNullOrEmpty()) item {
            Text(stringResource(R.string.ui_cached_agenda), Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        when {
            failed && agenda.isNullOrEmpty() -> item { ConferenceMessage(
                stringResource(R.string.ui_schedule_unavailable), stringResource(R.string.ui_schedule_error_body),
                actionLabel = stringResource(R.string.ui_retry), onAction = viewModel::refreshAgenda) }
            loading && agenda == null -> item { ConferenceMessage(stringResource(R.string.ui_loading_schedule),
                stringResource(R.string.ui_loading_schedule_body), loading = true) }
            agenda.isNullOrEmpty() -> item { ConferenceMessage(stringResource(R.string.ui_agenda_empty),
                stringResource(R.string.ui_agenda_empty_body)) }
            else -> agenda.orEmpty().sortedBy { it.startTime }.groupBy {
                it.startTime.withZoneSameInstant(zone).toLocalDate()
            }.forEach { (_, blocks) ->
                item { Text(conferenceDate(blocks.first().startTime, zone, "EEEE, d MMM"),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp).semantics { heading() }) }
                items(blocks) { block ->
                    val (container, content) = when (block.type) {
                        "meal", "break" -> palette.sage to palette.onSage
                        "session", "keynote" -> palette.amber to palette.onAmber
                        else -> palette.lavender to palette.onLavender
                    }
                    Surface(color = container, contentColor = content, shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Column(Modifier.padding(24.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(conferenceTime(block.startTime, zone), style = MaterialTheme.typography.headlineMedium)
                                Text(conferenceTime(block.endTime, zone), style = MaterialTheme.typography.bodyLarge)
                            }
                            Spacer(Modifier.height(18.dp))
                            Text(block.title, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}
