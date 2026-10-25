/*
 * Copyright 2019 Google LLC
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

package com.carthigan.droidconuganda.test.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Gets the current value of a Flow backed by a ConflatedBroadcastChannel.
 *
 * Flow.broadcastIn was removed from kotlinx.coroutines; a conflated source
 * delivers its latest value to a new subscriber immediately, so collecting the
 * first emission (with a timeout so an empty source still returns null)
 * reproduces the old poll() behaviour.
 */
fun <T> Flow<T>.getValueFromConflatedBroadcastChannel(scope: CoroutineScope): T? {
    // scope is kept for source compatibility with the previous implementation.
    return runBlocking {
        withTimeoutOrNull(100) {
            this@getValueFromConflatedBroadcastChannel.firstOrNull()
        }
    }
}
