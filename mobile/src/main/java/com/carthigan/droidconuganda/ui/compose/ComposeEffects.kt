package com.carthigan.droidconuganda.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.carthigan.droidconuganda.shared.result.Event
import com.carthigan.droidconuganda.shared.result.EventObserver

@Composable
fun <T> EventEffect(events: LiveData<Event<T>>, onEvent: (T) -> Unit) {
    val owner = LocalLifecycleOwner.current
    val callback = rememberUpdatedState(onEvent)
    DisposableEffect(events, owner) {
        val observer = EventObserver<T> { callback.value(it) }
        events.observe(owner, observer)
        onDispose { events.removeObserver(observer) }
    }
}

@Composable
fun OnResumeEffect(onResume: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    val callback = rememberUpdatedState(onResume)
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) callback.value()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}
