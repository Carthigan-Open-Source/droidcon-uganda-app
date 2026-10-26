package com.carthigan.droidconuganda.ui.compose

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.model.Session
import com.carthigan.droidconuganda.model.Theme
import com.carthigan.droidconuganda.shared.util.TimeUtils
import com.carthigan.droidconuganda.ui.MainActivityViewModel
import com.carthigan.droidconuganda.ui.SnackbarMessage
import com.carthigan.droidconuganda.ui.agenda.AgendaViewModel
import com.carthigan.droidconuganda.ui.compose.components.ConferenceIcon
import com.carthigan.droidconuganda.ui.compose.theme.DroidconTheme
import com.carthigan.droidconuganda.ui.info.SettingsViewModel
import com.carthigan.droidconuganda.ui.messages.SnackbarMessageManager
import com.carthigan.droidconuganda.ui.prefs.SnackbarPreferenceViewModel
import com.carthigan.droidconuganda.ui.schedule.ScheduleViewModel
import com.carthigan.droidconuganda.ui.search.SearchViewModel
import com.carthigan.droidconuganda.ui.sessiondetail.SessionDetailViewModel
import com.carthigan.droidconuganda.ui.signin.NotificationsPreferenceViewModel
import com.carthigan.droidconuganda.ui.speaker.SpeakerViewModel
import com.carthigan.droidconuganda.util.InstantAppUtils
import kotlinx.coroutines.launch

private data class PrimaryDestination(val route: String, val title: Int, val icon: Int)
private val PrimaryDestinations = listOf(
    PrimaryDestination("schedule", R.string.title_schedule, R.drawable.ic_nav_schedule),
    PrimaryDestination("agenda", R.string.title_agenda, R.drawable.ic_nav_agenda),
    PrimaryDestination("info", R.string.title_info, R.drawable.ic_nav_info)
)

@Composable
fun DroidconApp(
    factory: ViewModelProvider.Factory, mainViewModel: MainActivityViewModel,
    snackbarManager: SnackbarMessageManager, incomingSession: Pair<Int, String>?,
    onOpenUrl: (String) -> Unit, onShare: (Session) -> Unit, onNotificationSettings: () -> Unit,
    onInstallApp: () -> Unit
) {
    val context = LocalContext.current
    val schedule: ScheduleViewModel = viewModel(factory = factory)
    val settings: SettingsViewModel = viewModel(factory = factory)
    val snackbarPreference: SnackbarPreferenceViewModel = viewModel(factory = factory)
    val notificationPreference: NotificationsPreferenceViewModel = viewModel(factory = factory)
    val theme by mainViewModel.theme.observeAsState(Theme.SYSTEM)
    val zone by schedule.timeZoneId.observeAsState(TimeUtils.CONFERENCE_TIMEZONE)
    val notificationsEnabled by settings.enableNotifications.observeAsState(false)
    val appearance = remember(context) { context.getSharedPreferences("ui_appearance", Context.MODE_PRIVATE) }
    var wallpaperColors by rememberSaveable { mutableStateOf(appearance.getBoolean("wallpaper_colors", false)) }
    var showAttendance by rememberSaveable { mutableStateOf(false) }
    var showNotifications by rememberSaveable { mutableStateOf(false) }
    var showLicenses by rememberSaveable { mutableStateOf(false) }
    var showInstall by rememberSaveable { mutableStateOf(false) }
    val isInstantApp = remember(context) { InstantAppUtils.isInstantApp(context) }
    var resumeRevision by remember { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: "schedule"
    val primary = PrimaryDestinations.any { it.route == route }

    val showMessage: (SnackbarMessage) -> Unit = { message ->
        scope.launch {
            val result = snackbar.showSnackbar(
                message = context.getString(message.messageId),
                actionLabel = message.actionId?.let(context::getString),
                withDismissAction = true,
                duration = if (message.longDuration) SnackbarDuration.Long else SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed && message.actionId == R.string.dont_show) {
                snackbarPreference.onStopClicked()
            }
            snackbarManager.loadNextMessage()
        }
    }
    DroidconTheme(theme, wallpaperColors) {
        val notificationToggle = rememberNotificationToggle(onToggle = { enabled ->
            settings.toggleEnableNotifications(enabled, isInstantApp && enabled)
            if (showNotifications) {
                notificationPreference.onDismissed()
                showNotifications = false
            }
        }, onDenied = {
            scope.launch {
                if (snackbar.showSnackbar(context.getString(R.string.ui_notification_permission_denied),
                        actionLabel = context.getString(R.string.ui_open_settings),
                        duration = SnackbarDuration.Long) == SnackbarResult.ActionPerformed) onNotificationSettings()
            }
        })
        val requestNotifications: (Boolean) -> Unit = { enabled ->
            if (enabled && isInstantApp) showInstall = true else notificationToggle(enabled)
        }

        EventEffect(mainViewModel.navigateToUserAttendeeDialogAction) { showAttendance = true }
        EventEffect(schedule.shouldShowNotificationsPrefAction) { if (it) showNotifications = true }
        EventEffect(settings.showSignIn) { showInstall = true }
        EventEffect(schedule.snackBarMessage, showMessage)
        EventEffect(snackbarManager.observeNextMessage(), showMessage)
        OnResumeEffect { resumeRevision++ }
        LaunchedEffect(notificationsEnabled, resumeRevision) {
            if (notificationsEnabled && !NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                settings.toggleEnableNotifications(false, false)
            }
        }
        LaunchedEffect(incomingSession) {
            incomingSession?.let { (_, id) -> nav.navigate(sessionRoute(id)) { launchSingleTop = true } }
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val rail = maxWidth >= 840.dp || (maxWidth >= 600.dp && maxHeight < 480.dp)
            val navigatePrimary: (String) -> Unit = { destination ->
                nav.navigate(destination) {
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            Scaffold(containerColor = MaterialTheme.colorScheme.background,
                // safeDrawing includes the IME; individual screens must not add imePadding again.
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbar) },
                bottomBar = {
                    if (primary && !rail) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        PrimaryDestinations.forEach { destination ->
                            NavigationBarItem(selected = destination.route == route,
                                onClick = { navigatePrimary(destination.route) },
                                icon = { ConferenceIcon(destination.icon, null) },
                                label = { Text(stringResource(destination.title)) })
                        }
                    }
                }) { insets ->
                Row(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets)) {
                    if (primary && rail) NavigationRail(modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.background, windowInsets = WindowInsets(0, 0, 0, 0)) {
                        PrimaryDestinations.forEach { destination ->
                            NavigationRailItem(selected = destination.route == route,
                                onClick = { navigatePrimary(destination.route) },
                                icon = { ConferenceIcon(destination.icon, null) },
                                label = { Text(stringResource(destination.title)) })
                        }
                    }
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                        NavHost(nav, startDestination = "schedule",
                            modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                            enterTransition = { fadeIn(tween(180)) }, exitTransition = { fadeOut(tween(120)) }) {
                            composable("schedule") {
                                ScheduleRoute(schedule, { nav.navigate(sessionRoute(it)) },
                                    { nav.navigate("search") }, { error -> scope.launch { snackbar.showSnackbar(error) } })
                            }
                            composable("agenda") {
                                val vm: AgendaViewModel = viewModel(factory = factory)
                                AgendaRoute(vm)
                            }
                            composable("info") {
                                InfoRoute(settings, theme, wallpaperColors,
                                    onDynamicColor = { enabled ->
                                        wallpaperColors = enabled
                                        appearance.edit().putBoolean("wallpaper_colors", enabled).apply()
                                    }, onOpenUrl = onOpenUrl, onLicenses = { showLicenses = true },
                                    onNotificationsChanged = requestNotifications)
                            }
                            composable("search") {
                                val vm: SearchViewModel = viewModel(factory = factory)
                                SearchRoute(vm, { nav.popBackStack() }, { nav.navigate(sessionRoute(it)) },
                                    { nav.navigate(speakerRoute(it)) })
                            }
                            composable("session/{sessionId}", arguments = listOf(navArgument("sessionId") {
                                type = NavType.StringType
                            })) { entry ->
                                val vm: SessionDetailViewModel = viewModel(factory = factory)
                                SessionDetailRoute(entry.arguments?.getString("sessionId").orEmpty(), vm,
                                    { nav.popBackStack() }, { nav.navigate(speakerRoute(it)) },
                                    { nav.navigate(sessionRoute(it)) }, onShare, onOpenUrl, showMessage,
                                    { showNotifications = true })
                            }
                            composable("speaker/{speakerId}", arguments = listOf(navArgument("speakerId") {
                                type = NavType.StringType
                            })) { entry ->
                                val vm: SpeakerViewModel = viewModel(factory = factory)
                                SpeakerRoute(entry.arguments?.getString("speakerId").orEmpty(), vm, zone,
                                    { nav.popBackStack() }, { nav.navigate(sessionRoute(it)) }, onOpenUrl, showMessage)
                            }
                        }
                    }
                }
            }
        }
        if (showLicenses) LicensesDialog({ showLicenses = false }, onOpenUrl)
        if (showInstall) AlertDialog(onDismissRequest = { showInstall = false },
            title = { Text(stringResource(R.string.installApp)) },
            text = { Text(stringResource(R.string.notifications_preference_dialog_content_instant)) },
            confirmButton = { TextButton(onClick = { showInstall = false; onInstallApp() }) {
                Text(stringResource(R.string.installApp))
            } }, dismissButton = { TextButton(onClick = { showInstall = false }) {
                Text(stringResource(R.string.installApp_no))
            } })
        if (showAttendance) AlertDialog(onDismissRequest = { showAttendance = false },
            title = { Text(stringResource(R.string.user_attendee_dialog_title)) },
            text = { Text(stringResource(R.string.user_attendee_dialog_description)) },
            confirmButton = { TextButton(onClick = { settings.toggleUserIsAttending(true); showAttendance = false }) {
                Text(stringResource(R.string.user_attendee_dialog_in_person))
            } }, dismissButton = { TextButton(onClick = { settings.toggleUserIsAttending(false); showAttendance = false }) {
                Text(stringResource(R.string.user_attendee_dialog_remote))
            } })
        else if (showNotifications) AlertDialog(onDismissRequest = {
            notificationPreference.onDismissed(); showNotifications = false
        }, title = { Text(stringResource(R.string.notifications_preference_dialog_title)) },
            text = { Text(stringResource(R.string.notifications_preference_dialog_content)) },
            confirmButton = { TextButton(onClick = {
                notificationPreference.onDismissed()
                showNotifications = false
                requestNotifications(true)
            }) { Text(stringResource(R.string.yes)) } },
            dismissButton = { TextButton(onClick = { notificationToggle(false) }) { Text(stringResource(R.string.not_now)) } })
    }
}
