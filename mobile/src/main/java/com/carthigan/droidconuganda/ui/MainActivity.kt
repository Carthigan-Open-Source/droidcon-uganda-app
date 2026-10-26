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
package com.carthigan.droidconuganda.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.carthigan.droidconuganda.R
import com.carthigan.droidconuganda.model.Session
import com.carthigan.droidconuganda.shared.util.viewModelProvider
import com.carthigan.droidconuganda.ui.compose.DroidconApp
import com.carthigan.droidconuganda.ui.compose.sessionIdFromDeepLink
import com.carthigan.droidconuganda.ui.dialogs.InstallAppStoreLauncher
import com.carthigan.droidconuganda.ui.messages.SnackbarMessageManager
import com.carthigan.droidconuganda.ui.schedule.ScheduleViewModel
import com.carthigan.droidconuganda.util.observeTheme
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var snackbarMessageManager: SnackbarMessageManager

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private var incomingSession by mutableStateOf<Pair<Int, String>?>(null)
    private var intentSequence = 0
    private var scheduleViewModel: ScheduleViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainActivityViewModel: MainActivityViewModel = viewModelProvider(viewModelFactory)
        scheduleViewModel = viewModelProvider<ScheduleViewModel>(viewModelFactory)
        observeTheme(mainActivityViewModel.theme)

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        if (savedInstanceState == null) {
            receiveSessionLink(intent)
            mainActivityViewModel.refreshConferenceData()
        }
        // AppCompat 1.1 bypasses ComponentActivity's view-tree owner setup in setContentView.
        initializeViewTreeOwners()
        setContent {
            DroidconApp(viewModelFactory, mainActivityViewModel, snackbarMessageManager, incomingSession,
                onOpenUrl = ::openUrl, onShare = ::shareSession, onNotificationSettings = {
                    val settings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null))
                    startActivity(settings)
                }, onInstallApp = { InstallAppStoreLauncher().showDialog(this) })
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        receiveSessionLink(intent)
    }

    private fun receiveSessionLink(intent: Intent?) {
        sessionIdFromDeepLink(intent?.data)?.let { id -> incomingSession = ++intentSequence to id }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        scheduleViewModel?.userHasInteracted = true
    }

    private fun openUrl(url: String) {
        val uri = Uri.parse(url)
        if (uri.scheme !in listOf("https", "http") || uri.host.isNullOrBlank()) return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.ui_unable_to_open_link, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareSession(session: Session) {
        val url = session.sessionUrl.ifBlank { getString(R.string.faq_url) }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text_session_detail, session.title, url))
        }
        try {
            startActivity(Intent.createChooser(send, getString(R.string.intent_chooser_session_detail)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.ui_unable_to_open_link, Toast.LENGTH_SHORT).show()
        }
    }
}
