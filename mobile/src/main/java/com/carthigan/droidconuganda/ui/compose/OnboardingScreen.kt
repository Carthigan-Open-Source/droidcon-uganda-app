package com.carthigan.droidconuganda.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.shared.R as SharedR
import com.carthigan.droidconuganda.shared.util.TimeUtils
import com.carthigan.droidconuganda.ui.compose.components.ConferenceIcon
import com.carthigan.droidconuganda.ui.compose.theme.LocalConferencePalette
import java.util.Locale

@Composable
fun OnboardingScreen(starting: Boolean, onStart: () -> Unit) {
    val palette = LocalConferencePalette.current
    val days = TimeUtils.ConferenceDays
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start) {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)) {
                item { Text(stringResource(SharedR.string.app_name), style = MaterialTheme.typography.titleMedium) }
                item { Text(stringResource(R.string.ui_welcome_title), style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.semantics { heading() }) }
                item {
                    Surface(color = palette.amber, contentColor = palette.onAmber,
                        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp, bottomEnd = 12.dp, bottomStart = 36.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("${days.first().start.dayOfMonth}–${days.last().start.dayOfMonth}",
                                style = MaterialTheme.typography.displayMedium)
                            Text(conferenceDate(days.first().start, days.first().start.zone, "MMM yyyy")
                                .uppercase(Locale.getDefault()), style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(R.string.ui_venue), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.ui_conference_location), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                item { Text(stringResource(R.string.ui_welcome_body), style = MaterialTheme.typography.bodyLarge) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        ConferenceIcon(R.drawable.ic_bookmark_outline, null)
                        Text(stringResource(R.string.ui_local_bookmarks_body), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Button(onClick = onStart, enabled = !starting,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                Text(stringResource(if (starting) R.string.ui_loading else R.string.ui_open_schedule),
                    Modifier.padding(vertical = 10.dp))
            }
        }
        }
    }
}
