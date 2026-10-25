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

package com.carthigan.droidconuganda.shared.data.signin

import android.net.Uri

/**
 * The local pseudo-user. The app has no account system: every install is
 * treated as a single "signed in" local attendee so that per-user features
 * (starring sessions) keep working without Firebase Auth.
 */
object LocalUserInfo : AuthenticatedUserInfo {

    const val LOCAL_USER_ID = "local_user"

    override fun isSignedIn(): Boolean = true

    override fun getEmail(): String? = null

    override fun getLastSignInTimestamp(): Long? = null

    override fun getCreationTimestamp(): Long? = null

    override fun isAnonymous(): Boolean? = true

    override fun getPhoneNumber(): String? = null

    override fun getUid(): String? = LOCAL_USER_ID

    override fun isEmailVerified(): Boolean? = null

    override fun getDisplayName(): String? = "Droidcon Uganda Attendee"

    override fun getPhotoUrl(): Uri? = null

    override fun getProviderId(): String? = "local"
}
