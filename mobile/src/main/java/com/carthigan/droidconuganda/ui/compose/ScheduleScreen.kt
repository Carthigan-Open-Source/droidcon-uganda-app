package com.carthigan.droidconuganda.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.model.ConferenceDay
import com.carthigan.droidconuganda.model.userdata.UserSession
import com.carthigan.droidconuganda.shared.R as SharedR
import com.carthigan.droidconuganda.shared.domain.sessions.EventLocation
import com.carthigan.droidconuganda.shared.util.TimeUtils
import com.carthigan.droidconuganda.ui.compose.components.ConferenceDayPicker
import com.carthigan.droidconuganda.ui.compose.components.ConferenceIcon
import com.carthigan.droidconuganda.ui.compose.components.ConferenceMessage
import com.carthigan.droidconuganda.ui.compose.components.SelectionPill
import com.carthigan.droidconuganda.ui.compose.components.SessionCard
import com.carthigan.droidconuganda.ui.compose.components.UgandaMap
import com.carthigan.droidconuganda.ui.schedule.ScheduleViewModel
import com.carthigan.droidconuganda.ui.schedule.filters.EventFilter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.threeten.bp.ZoneId
import java.util.Locale

@Composable
fun ScheduleRoute(viewModel: ScheduleViewModel, onSession: (String) -> Unit,
    onSearch: () -> Unit, onError: (String) -> Unit) {
    var selectedDay by rememberSaveable { mutableIntStateOf(0) }
    var scrollRequest by remember { mutableStateOf<EventLocation?>(null) }
    val days = TimeUtils.ConferenceDays
    val dayData by remember(viewModel, selectedDay) {
        viewModel.getSessionTimeDataForDay(selectedDay)
    }.observeAsState()
    val filters by viewModel.eventFilters.observeAsState(emptyList())
    val loading by viewModel.isLoading.observeAsState(true)
    val refreshing by viewModel.isRefreshing.observeAsState(false)
    val failed by viewModel.loadFailed.observeAsState(false)
    val zone by viewModel.timeZoneId.observeAsState(TimeUtils.CONFERENCE_TIMEZONE)
    val currentEvent by viewModel.currentEvent.observeAsState()
    var liveSessionId by remember(dayData?.list) {
        mutableStateOf(firstLiveSession(dayData?.list.orEmpty(), viewModel.currentTime())?.session?.id)
    }
    val liveScope = rememberCoroutineScope()
    LifecycleResumeEffect(dayData?.list) {
        val ticker = liveScope.launch {
            while (isActive) {
                val sessions = dayData?.list.orEmpty()
                val now = viewModel.currentTime()
                liveSessionId = firstLiveSession(sessions, now)?.session?.id
                delay(millisUntilNextSessionBoundary(sessions, now))
            }
        }
        onPauseOrDispose { ticker.cancel() }
    }
    LaunchedEffect(currentEvent) {
        if (!viewModel.userHasInteracted) currentEvent?.let {
            selectedDay = it.day.coerceIn(days.indices)
            scrollRequest = it
        }
    }
    OnResumeEffect { viewModel.initializeTimeZone() }
    val errorMessage = stringResource(R.string.ui_schedule_error_body)
    EventEffect(viewModel.errorMessage) { onError(errorMessage) }

    ScheduleScreen(
        days = days, selectedDay = selectedDay, sessions = dayData?.list,
        filters = filters, loading = loading, refreshing = refreshing, failed = failed, zone = zone,
        onDaySelected = { selectedDay = it.coerceIn(days.indices); scrollRequest = null }, onSession = onSession,
        onStar = viewModel::onStarClicked, onSearch = onSearch, onRefresh = viewModel::onSwipeRefresh,
        onFilter = { viewModel.toggleFilter(it, !it.isSelected) }, onClearFilters = viewModel::clearFilters,
        scrollToSessionIndex = scrollRequest?.takeIf { it.day == selectedDay }?.sessionIndex,
        onScrollHandled = { scrollRequest = null },
        liveSessionId = liveSessionId
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    days: List<ConferenceDay>, selectedDay: Int, sessions: List<UserSession>?,
    filters: List<EventFilter>, loading: Boolean, refreshing: Boolean, failed: Boolean, zone: ZoneId,
    onDaySelected: (Int) -> Unit, onSession: (String) -> Unit, onStar: (UserSession) -> Unit,
    onSearch: () -> Unit, onRefresh: () -> Unit, onFilter: (EventFilter) -> Unit,
    onClearFilters: () -> Unit,
    scrollToSessionIndex: Int? = null,
    onScrollHandled: () -> Unit = {},
    liveSessionId: String? = null
) {
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val pinnedFilter = filters.filterIsInstance<EventFilter.MyEventsFilter>().firstOrNull()
    val savedOnly = pinnedFilter?.isSelected == true
    val selectedTopics = filters.filterIsInstance<EventFilter.TagFilter>().filter { it.isSelected }
    val listState = rememberLazyListState()
    LaunchedEffect(selectedDay, scrollToSessionIndex, sessions) {
        if (scrollToSessionIndex != null && !sessions.isNullOrEmpty()) {
            // Brand/date header, section heading, and wrapping action row precede the sessions.
            listState.scrollToItem(3 + scrollToSessionIndex.coerceIn(sessions.indices))
            onScrollHandled()
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().testTag("schedule_list"), state = listState,
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(Modifier.padding(horizontal = 24.dp)) {
                Row(Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(SharedR.string.app_name), Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium)
                    FilledTonalIconButton(onClick = onSearch) {
                        ConferenceIcon(R.drawable.ic_search, stringResource(R.string.search_schedule_hint))
                    }
                }
                ConferenceDayPicker(days, selectedDay, onDaySelected)
                Spacer(Modifier.height(24.dp))
                ScheduleDateHeader(days[selectedDay], zone)
                Spacer(Modifier.height(20.dp))
                if (pinnedFilter != null) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectionPill(stringResource(R.string.ui_all_sessions), !savedOnly,
                            onClick = { if (savedOnly) onFilter(pinnedFilter) })
                        SelectionPill(stringResource(R.string.ui_my_schedule), savedOnly,
                            onClick = { if (!savedOnly) onFilter(pinnedFilter) })
                    }
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(stringResource(if (savedOnly) R.string.ui_my_schedule else R.string.ui_sessions_heading),
                    style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                if (sessions != null) Text(
                    pluralStringResource(R.plurals.ui_session_count, sessions.size, sessions.size),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            FlowRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { showFilters = true }, enabled = filters.isNotEmpty()) {
                    ConferenceIcon(R.drawable.ic_filter, null)
                    Text(if (selectedTopics.isEmpty()) stringResource(R.string.filters)
                        else stringResource(R.string.ui_filters_active, selectedTopics.size))
                }
                TextButton(onClick = onRefresh, enabled = !refreshing) {
                    Text(stringResource(if (refreshing) R.string.ui_loading else R.string.ui_refresh))
                }
            }
        }
        when {
            failed && sessions == null -> item {
                ConferenceMessage(stringResource(R.string.ui_schedule_unavailable),
                    stringResource(R.string.ui_schedule_error_body),
                    actionLabel = stringResource(R.string.ui_retry), onAction = onRefresh)
            }
            loading && sessions == null -> item {
                ConferenceMessage(stringResource(R.string.ui_loading_schedule),
                    stringResource(R.string.ui_loading_schedule_body), loading = true)
            }
            sessions.isNullOrEmpty() -> item {
                ConferenceMessage(
                    stringResource(if (savedOnly && selectedTopics.isEmpty()) R.string.ui_no_saved_sessions
                        else R.string.ui_no_matching_sessions),
                    stringResource(if (savedOnly && selectedTopics.isEmpty()) R.string.ui_no_saved_sessions_body
                        else R.string.ui_no_matching_sessions_body),
                    actionLabel = stringResource(R.string.ui_all_sessions), onAction = onClearFilters)
            }
            else -> items(sessions, key = { it.session.id }) { userSession ->
                SessionCard(userSession, zone, onClick = { onSession(userSession.session.id) },
                    onStar = { onStar(userSession) }, modifier = Modifier.padding(horizontal = 16.dp),
                    isHappeningNow = userSession.session.id == liveSessionId)
            }
        }
    }
    if (showFilters) {
        ModalBottomSheet(onDismissRequest = { showFilters = false }) {
            LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { Text(stringResource(R.string.filters), style = MaterialTheme.typography.headlineMedium) }
                filters.filterIsInstance<EventFilter.TagFilter>().groupBy { it.getFilterCategory() }.forEach { (category, group) ->
                    item {
                        Text(stringResource(category.resId), style = MaterialTheme.typography.titleMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            group.forEach { filter ->
                                val description = stringResource(if (filter.isSelected) R.string.a11y_filter_applied
                                    else R.string.a11y_filter_not_applied, filter.getText())
                                FilterChip(selected = filter.isSelected, onClick = { onFilter(filter) },
                                    label = { Text(filter.getText()) }, modifier = Modifier.heightIn(min = 48.dp)
                                        .semantics { contentDescription = description })
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = onClearFilters) { Text(stringResource(R.string.clear_all)) }
                        Button(onClick = { showFilters = false }) { Text(stringResource(R.string.ui_close)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleDateHeader(day: ConferenceDay, zone: ZoneId) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val stacked = maxWidth < 340.dp || LocalDensity.current.fontScale > 1.3f
        val date: @Composable () -> Unit = {
            Column {
                Text(conferenceDate(day.start, day.start.zone, "EEEE"), style = MaterialTheme.typography.bodyLarge)
                Text(conferenceDate(day.start, day.start.zone, "d"),
                    style = if (stacked) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge)
                Text(conferenceDate(day.start, day.start.zone, "MMM").uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.displayMedium)
            }
        }
        val location: @Composable () -> Unit = {
            Column {
                Text(stringResource(R.string.ui_conference_location), style = MaterialTheme.typography.titleMedium)
                Text(zone.id, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        val map: @Composable () -> Unit = {
            UgandaMap(
                modifier = Modifier.size(if (stacked) 64.dp else 112.dp)
                    .testTag("schedule_uganda_map")
            )
        }
        if (stacked) Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            date()
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                map()
                Column(Modifier.weight(1f)) { location() }
            }
        }
        else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom) {
            date()
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                map()
                location()
            }
        }
    }
}
