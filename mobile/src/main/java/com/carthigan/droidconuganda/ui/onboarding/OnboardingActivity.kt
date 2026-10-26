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

package com.carthigan.droidconuganda.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.carthigan.droidconuganda.model.Theme
import com.carthigan.droidconuganda.shared.result.EventObserver
import com.carthigan.droidconuganda.shared.util.viewModelProvider
import com.carthigan.droidconuganda.ui.MainActivity
import com.carthigan.droidconuganda.ui.ThemedActivityDelegate
import com.carthigan.droidconuganda.ui.compose.OnboardingScreen
import com.carthigan.droidconuganda.ui.compose.theme.DroidconTheme
import com.carthigan.droidconuganda.util.observeTheme
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class OnboardingActivity : DaggerAppCompatActivity() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var themedActivityDelegate: ThemedActivityDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onboardingViewModel: OnboardingViewModel = viewModelProvider(viewModelFactory)

        onboardingViewModel.navigateToMainActivity.observe(this, EventObserver {
            this.run {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        })

        observeTheme(themedActivityDelegate.theme)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        val appearance = getSharedPreferences("ui_appearance", Context.MODE_PRIVATE)
        initializeViewTreeOwners()
        setContent {
            val theme by themedActivityDelegate.theme.observeAsState(Theme.SYSTEM)
            var starting by remember { mutableStateOf(false) }
            DroidconTheme(theme, appearance.getBoolean("wallpaper_colors", false)) {
                OnboardingScreen(starting = starting, onStart = {
                    starting = true
                    onboardingViewModel.getStartedClick()
                })
            }
        }
    }
}
