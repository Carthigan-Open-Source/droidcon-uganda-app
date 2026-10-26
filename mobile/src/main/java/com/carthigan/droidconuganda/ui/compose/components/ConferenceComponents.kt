package com.carthigan.droidconuganda.ui.compose.components

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.model.ConferenceDay
import com.carthigan.droidconuganda.model.Session
import com.carthigan.droidconuganda.model.Speaker
import com.carthigan.droidconuganda.model.Tag
import com.carthigan.droidconuganda.model.userdata.UserSession
import com.carthigan.droidconuganda.ui.compose.conferenceDate
import com.carthigan.droidconuganda.ui.compose.conferenceTime
import com.carthigan.droidconuganda.ui.compose.speakerInitials
import com.carthigan.droidconuganda.ui.compose.theme.LocalConferencePalette
import org.threeten.bp.ZoneId

@Composable
fun ConferenceIcon(@DrawableRes resource: Int, description: String?, modifier: Modifier = Modifier) {
    Icon(painterResource(resource), description, modifier.size(24.dp))
}

@Composable
fun BackButton(onClick: () -> Unit) {
    FilledTonalIconButton(onClick, Modifier.size(48.dp)) {
        ConferenceIcon(R.drawable.ic_arrow_back, stringResource(R.string.navigate_up))
    }
}

@Composable
fun PageHeading(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { heading() })
        if (subtitle != null) {
            Spacer(Modifier.height(8.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConferenceDayPicker(days: List<ConferenceDay>, selectedDay: Int, onDaySelected: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        days.forEachIndexed { index, day ->
            SelectionPill(
                label = conferenceDate(day.start, day.start.zone, "MMM d"),
                selected = selectedDay == index,
                onClick = { onDaySelected(index) }
            )
        }
    }
}

@Composable
fun SelectionPill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp).semantics {
            this.selected = selected
            role = Role.Tab
        },
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (selected) MaterialTheme.colorScheme.background
            else MaterialTheme.colorScheme.onSurface
    ) {
        Box(Modifier.padding(horizontal = 22.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun sessionColors(session: Session): Pair<Color, Color> {
    val palette = LocalConferencePalette.current
    val topic = session.tags.firstOrNull { it.category == Tag.CATEGORY_TOPIC }?.tagName.orEmpty()
    return when {
        topic.contains("android") -> palette.amber to palette.onAmber
        topic.contains("flutter") || topic.contains("kmp") || topic.contains("kotlin") ->
            palette.sage to palette.onSage
        else -> palette.lavender to palette.onLavender
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionCard(
    userSession: UserSession,
    zone: ZoneId,
    onClick: () -> Unit,
    onStar: () -> Unit,
    modifier: Modifier = Modifier,
    isHappeningNow: Boolean = false
) {
    val session = userSession.session
    val (container, content) = sessionColors(session)
    val liveDescription = stringResource(R.string.ui_happening_now)
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().semantics {
                if (isHappeningNow) stateDescription = liveDescription
            },
            shape = RoundedCornerShape(28.dp), color = container, contentColor = content) {
        Column(Modifier.padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(session.displayTags.joinToString(" · ") { it.displayName }
                    .ifBlank { session.type.displayName },
                    Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                IconToggleButton(checked = userSession.userEvent.isStarred,
                    onCheckedChange = { onStar() }, modifier = Modifier.size(48.dp)) {
                    ConferenceIcon(
                        if (userSession.userEvent.isStarred) R.drawable.ic_bookmark_filled
                        else R.drawable.ic_bookmark_outline,
                        stringResource(if (userSession.userEvent.isStarred) R.string.ui_remove_session
                            else R.string.ui_save_session)
                    )
                }
            }
            Text(session.title, style = MaterialTheme.typography.titleLarge)
            if (session.speakers.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SpeakerAvatar(session.speakers.first(), 32.dp)
                    Text(session.speakers.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(20.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${conferenceTime(session.startTime, zone)} – ${conferenceTime(session.endTime, zone)}",
                    style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.ui_duration_minutes, session.duration / 60_000),
                    style = MaterialTheme.typography.bodyMedium)
                session.room?.let { Text(it.name, style = MaterialTheme.typography.bodyMedium) }
            }
        }
        }
        if (isHappeningNow) {
            LiveSessionRibbon(Modifier.matchParentSize())
        }
    }
}

@Composable
fun SpeakerAvatar(speaker: Speaker, size: Dp = 48.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center) {
        Text(speakerInitials(speaker.name), color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge)
        if (speaker.imageUrl.isNotBlank()) {
            AndroidView(
                factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
                modifier = Modifier.size(size),
                update = { view ->
                    if (view.tag != speaker.imageUrl) {
                        view.tag = speaker.imageUrl
                        Glide.with(view).load(speaker.imageUrl).circleCrop().into(view)
                    }
                },
                onRelease = { Glide.with(it).clear(it) }
            )
        }
    }
}

@Composable
fun SpeakerRow(speaker: Speaker, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            SpeakerAvatar(speaker, 56.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(speaker.name, style = MaterialTheme.typography.titleMedium)
                if (speaker.company.isNotBlank()) Text(speaker.company,
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConferenceMessage(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(modifier.fillMaxWidth().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (loading) LoadingIndicator()
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
        Text(message, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (actionLabel != null && onAction != null) Button(onClick = onAction) { Text(actionLabel) }
    }
}
