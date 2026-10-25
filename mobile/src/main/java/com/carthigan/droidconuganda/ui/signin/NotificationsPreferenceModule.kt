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

package com.carthigan.droidconuganda.ui.signin

import androidx.lifecycle.ViewModel
import com.carthigan.droidconuganda.shared.di.ChildFragmentScoped
import com.carthigan.droidconuganda.shared.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 * Module that defines the notifications preference dialog fragment.
 * (The sign-in dialog fragments that used to live in this spot were removed
 * together with Firebase; this dialog remains and asks whether the user
 * wants session-reminder notifications.)
 */
@Module
internal abstract class NotificationsPreferenceModule {

    /**
     * Generates an [dagger.android.AndroidInjector] for the
     * [NotificationsPreferenceDialogFragment].
     */
    @ChildFragmentScoped
    @ContributesAndroidInjector
    internal abstract fun contributeNotificationsPreferenceDialogFragment():
        NotificationsPreferenceDialogFragment

    /**
     * The ViewModels are created by Dagger in a map. Via the @ViewModelKey, we define that we
     * want to get a [NotificationsPreferenceViewModel] class.
     */
    @Binds
    @IntoMap
    @ViewModelKey(NotificationsPreferenceViewModel::class)
    internal abstract fun bindNotificationsPreferenceViewModel(
        viewModel: NotificationsPreferenceViewModel
    ): ViewModel
}
