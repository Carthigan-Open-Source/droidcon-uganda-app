package com.carthigan.droidconuganda.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
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

/**
 * Observes a LiveData value using reference equality.
 *
 * Filter lists deliberately keep identity-based equality while their `isSelected` fields change,
 * so the default structural `observeAsState` would ignore a fresh list snapshot.
 */
@Composable
fun <T> LiveData<T>.observeAsStateReferential(initial: T): State<T> {
    val state = remember { mutableStateOf(initial, referentialEqualityPolicy()) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(this, owner) {
        val observer = Observer<T> { state.value = it }
        observe(owner, observer)
        onDispose { removeObserver(observer) }
    }
    return state
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
