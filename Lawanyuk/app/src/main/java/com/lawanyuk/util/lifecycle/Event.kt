package com.lawanyuk.util.lifecycle

open class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContent(): T? {
        return if (hasBeenHandled) null
        else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}
