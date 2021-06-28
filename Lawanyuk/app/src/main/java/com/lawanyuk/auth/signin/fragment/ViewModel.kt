package com.lawanyuk.auth.signin.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawanyuk.auth.model.User
import com.lawanyuk.repository.Repository
import com.lawanyuk.util.lifecycle.Event
import com.lawanyuk.util.logw
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val _message = MutableLiveData<Event<Message>>()
    val message: LiveData<Event<Message>> = _message

    fun signIn(user: User) = viewModelScope.launch {
        _message.value = Event(Message.LOADING)
        try {
            Repository.signIn(user)
            _message.value = Event(Message.SUCCESS)
        } catch (e: Exception) {
            logw("signIn failed", e)
            _message.value = Event(Message.FAILURE)
        }
    }

    enum class Message {
        FAILURE, LOADING, SUCCESS
    }
}
