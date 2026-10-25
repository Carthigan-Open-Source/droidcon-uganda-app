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

package com.carthigan.droidconuganda.test.util.fakes

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.carthigan.droidconuganda.shared.data.signin.AuthenticatedUserInfo
import com.carthigan.droidconuganda.shared.result.Event
import com.carthigan.droidconuganda.shared.result.Result
import com.carthigan.droidconuganda.ui.signin.SignInViewModelDelegate
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class FakeSignInViewModelDelegate : SignInViewModelDelegate {

    private var lastUserId: String? = null
    private val userState = MutableStateFlow<Result<AuthenticatedUserInfo>?>(null)

    override val currentFirebaseUser: Flow<Result<AuthenticatedUserInfo>> =
        userState.filterNotNull()
    override val currentUserImageUri = MutableLiveData<Uri?>()
    override val performSignInEvent = MutableLiveData<Event<Any>>()
    override val performSignOutEvent = MutableLiveData<Event<Any>>()
    override val shouldShowNotificationsPrefAction = MutableLiveData<Event<Boolean>>()

    var injectIsSignedIn = true
    var signInRequestsEmitted = 0
    var signOutRequestsEmitted = 0

    override fun isSignedIn(): Boolean = injectIsSignedIn

    override fun observeSignedInUser() = TODO("Not implemented")

    override suspend fun emitSignInRequest() {
        signInRequestsEmitted++
    }

    override suspend fun emitSignOutRequest() {
        signOutRequestsEmitted++
    }

    override fun getUserId() = lastUserId

    fun loadUser(id: String) {
        val mockUser = mock<AuthenticatedUserInfo> {
            on { getUid() }.doReturn(id)
            on { getPhotoUrl() }.doReturn(mock<Uri> {})
            on { isSignedIn() }.doReturn(true)
        }
        lastUserId = id
        userState.value = Result.Success(mockUser)
    }

    fun closeChannel() {
        // StateFlow has no close; kept for source compatibility.
    }
}
