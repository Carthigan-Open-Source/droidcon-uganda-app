package com.carthigan.droidconuganda.ui.compose

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.carthigan.droidconuganda.BuildConfig
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.model.Theme
import com.carthigan.droidconuganda.shared.R as SharedR
import com.carthigan.droidconuganda.shared.util.TimeUtils
import com.carthigan.droidconuganda.ui.compose.components.ConferenceHtmlText
import com.carthigan.droidconuganda.ui.compose.components.PageHeading
import com.carthigan.droidconuganda.ui.compose.components.SelectionPill
import com.carthigan.droidconuganda.ui.compose.theme.LocalConferencePalette
import com.carthigan.droidconuganda.ui.info.SettingsViewModel

private val FaqEntries = listOf(
    R.string.dates_location_title to R.string.dates_location_description,
    R.string.attending_sessions_title to R.string.attending_sessions_description,
    R.string.badges_title to R.string.badges_description,
    R.string.parking_title to R.string.parking_description,
    R.string.lost_found_title to R.string.lost_found_description,
    R.string.faq_meals_title to R.string.faq_meals_description,
    R.string.attire_title to R.string.attire_description,
    R.string.accessibility_title to R.string.accessibility_description,
    R.string.event_conduct_title to R.string.event_conduct_description,
    R.string.childcare_title to R.string.childcare_description,
    R.string.youth_attendance_title to R.string.youth_attendance_description,
    R.string.soliciting_title to R.string.soliciting_description
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfoRoute(settings: SettingsViewModel, theme: Theme, dynamicColor: Boolean,
    onDynamicColor: (Boolean) -> Unit, onOpenUrl: (String) -> Unit,
    onLicenses: () -> Unit, onNotificationsChanged: (Boolean) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(R.string.event_title, R.string.faq_title, R.string.about_title, R.string.settings_title)
    LazyColumn(Modifier.fillMaxSize().testTag("info_list"), contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { PageHeading(stringResource(R.string.ui_info_heading), stringResource(R.string.ui_info_subtitle)) }
        item {
            FlowRow(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { index, title ->
                    SelectionPill(stringResource(title), tab == index, { tab = index })
                }
            }
        }
        when (tab) {
            0 -> {
                item { VenueCard(onOpenUrl) }
                item {
                    InfoTextCard(stringResource(R.string.ui_local_bookmarks),
                        stringResource(R.string.ui_local_bookmarks_body))
                }
                item { InfoTextCard(stringResource(R.string.attending_sessions_title),
                    stringResource(R.string.attending_sessions_description), onOpenUrl) }
                item {
                    val url = stringResource(R.string.faq_url)
                    Button(onClick = { onOpenUrl(url) }, modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(stringResource(R.string.faq_label))
                    }
                }
            }
            1 -> items(FaqEntries, key = { it.first }) { (title, body) -> FaqCard(title, body, onOpenUrl) }
            2 -> {
                item {
                    Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(SharedR.string.app_name), style = MaterialTheme.typography.headlineMedium)
                        Text(stringResource(R.string.ui_built_with_compose), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(R.string.settings_version_label, BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE.toString()), style = MaterialTheme.typography.bodyMedium)
                        listOf(R.string.faq_label to R.string.faq_url,
                            R.string.settings_tos to R.string.tos_url,
                            R.string.settings_privacy_policy to R.string.privacy_policy_url).forEach { (label, urlRes) ->
                            val url = stringResource(urlRes)
                            TextButton(onClick = { onOpenUrl(url) }) { Text(stringResource(label)) }
                        }
                        TextButton(onClick = onLicenses) { Text(stringResource(R.string.settings_oss_licenses)) }
                    }
                }
            }
            3 -> item { SettingsContent(settings, theme, dynamicColor, onDynamicColor, onNotificationsChanged) }
        }
    }
}

@Composable
private fun VenueCard(onOpenUrl: (String) -> Unit) {
    val palette = LocalConferencePalette.current
    val mapUrl = stringResource(R.string.ui_map_url)
    Surface(color = palette.sage, contentColor = palette.onSage, shape = RoundedCornerShape(36.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val days = TimeUtils.ConferenceDays
            Text("${conferenceDate(days.first().start, days.first().start.zone, "d MMM")} – " +
                conferenceDate(days.last().start, days.last().start.zone, "d MMM yyyy"),
                style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.ui_venue), style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() })
            Text(stringResource(R.string.ui_conference_location), style = MaterialTheme.typography.bodyLarge)
            FilledTonalButton(onClick = { onOpenUrl(mapUrl) }) { Text(stringResource(R.string.ui_directions)) }
        }
    }
}

@Composable
private fun InfoTextCard(title: String, body: String, onOpenUrl: (String) -> Unit = {}) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            ConferenceHtmlText(body, onOpenUrl)
        }
    }
}

@Composable
private fun FaqCard(titleId: Int, bodyId: Int, onOpenUrl: (String) -> Unit) {
    var expanded by rememberSaveable(titleId) { mutableStateOf(false) }
    val disclosure = stringResource(if (expanded) R.string.expanded else R.string.collapsed)
    Surface(onClick = { expanded = !expanded }, shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .semantics { stateDescription = disclosure }) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(titleId), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(if (expanded) "−" else "+", Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.headlineSmall)
            }
            if (expanded) ConferenceHtmlText(stringResource(bodyId), onOpenUrl)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsContent(settings: SettingsViewModel, theme: Theme, dynamicColor: Boolean,
    onDynamicColor: (Boolean) -> Unit, onNotificationsChanged: (Boolean) -> Unit) {
    val conferenceTime by settings.preferConferenceTimeZone.observeAsState(true)
    val notifications by settings.enableNotifications.observeAsState(false)
    val analytics by settings.sendUsageStatistics.observeAsState(false)
    val attending by settings.userIsAttending.observeAsState(false)
    val themes by settings.availableThemes.observeAsState(listOf(Theme.SYSTEM, Theme.LIGHT, Theme.DARK))
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.ui_appearance),
            style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(8.dp).semantics { heading() })
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            themes.forEach { choice ->
                val name = when (choice) {
                    Theme.LIGHT -> R.string.settings_theme_light
                    Theme.DARK -> R.string.settings_theme_dark
                    Theme.BATTERY_SAVER -> R.string.settings_theme_battery
                    Theme.SYSTEM -> R.string.settings_theme_system
                }
                SelectionPill(stringResource(name), theme == choice, { settings.setTheme(choice) })
            }
        }
        if (Build.VERSION.SDK_INT >= 31) SettingSwitch(stringResource(R.string.ui_wallpaper_colors),
            dynamicColor, onDynamicColor, stringResource(R.string.ui_wallpaper_colors_body))
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.ui_preferences), Modifier.padding(8.dp), style = MaterialTheme.typography.titleLarge)
        SettingSwitch(stringResource(R.string.ui_conference_time), conferenceTime, settings::toggleTimeZone)
        SettingSwitch(stringResource(R.string.settings_enable_notifications), notifications, onNotificationsChanged)
        SettingSwitch(stringResource(R.string.attending_switch_label), attending, settings::toggleUserIsAttending)
        SettingSwitch(stringResource(R.string.settings_send_anonymous_usage_statistics), analytics,
            settings::toggleSendUsageStatistics)
    }
}

@Composable
private fun SettingSwitch(title: String, checked: Boolean, onChange: (Boolean) -> Unit, subtitle: String? = null) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp)) {
        Row(Modifier.fillMaxWidth().toggleable(checked, role = Role.Switch, onValueChange = onChange).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked, onCheckedChange = null)
        }
    }
}
