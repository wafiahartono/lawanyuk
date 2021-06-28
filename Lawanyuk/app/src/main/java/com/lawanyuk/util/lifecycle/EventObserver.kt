package com.lawanyuk.util.lifecycle

import androidx.lifecycle.Observer

class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.getContent()?.let { value ->
            onEventUnhandledContent(value)
        }
    }
}
