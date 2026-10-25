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

package com.carthigan.droidconuganda.util

import android.app.Activity
import com.carthigan.droidconuganda.shared.analytics.AnalyticsHelper
import timber.log.Timber
import javax.inject.Inject

/**
 * No-op analytics implementation. The app no longer ships Firebase
 * Analytics; events are only logged to Timber in debug builds.
 */
class StubAnalyticsHelper @Inject constructor() : AnalyticsHelper {

    override fun sendScreenView(screenName: String, activity: Activity) {
        Timber.d("Analytics: screen view %s", screenName)
    }

    override fun logUiEvent(itemId: String, action: String) {
        Timber.d("Analytics: %s - %s", action, itemId)
    }

    override fun setUserSignedIn(isSignedIn: Boolean) {
        // No-op: no analytics backend.
    }
}
