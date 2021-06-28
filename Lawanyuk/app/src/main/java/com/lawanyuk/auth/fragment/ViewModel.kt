package com.lawanyuk.auth.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawanyuk.repository.Repository
import com.lawanyuk.util.lifecycle.Event
import com.lawanyuk.util.logw
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val _googleSignInMessage = MutableLiveData<Event<GoogleSignInMessage>>()
    val googleSignInMessage: LiveData<Event<GoogleSignInMessage>> = _googleSignInMessage

    private val _anonymousSignInMessage = MutableLiveData<Event<AnonymousSignInMessage>>()
    val anonymousSignInMessage: LiveData<Event<AnonymousSignInMessage>> = _anonymousSignInMessage

    fun signInWithGoogle(token: String) = viewModelScope.launch {
        _googleSignInMessage.value = Event(GoogleSignInMessage.LOADING)
        try {
            Repository.signInWithGoogle(token)
            _googleSignInMessage.value = Event(
                if (Repository.isUserFullyRegistered()) GoogleSignInMessage.SUCCESS
                else GoogleSignInMessage.REGISTRATION_INCOMPLETE
            )
        } catch (e: Exception) {
            logw("signInWithGoogle failed", e)
            _googleSignInMessage.value = Event(GoogleSignInMessage.FAILURE)
        }
    }

    fun signInAnonymously() = viewModelScope.launch {
        _anonymousSignInMessage.value = Event(AnonymousSignInMessage.LOADING)
        try {
            Repository.signInAnonymously()
            _anonymousSignInMessage.value = Event(AnonymousSignInMessage.SUCCESS)
        } catch (e: Exception) {
            logw("signInAnonymously failed", e)
            _anonymousSignInMessage.value = Event(AnonymousSignInMessage.FAILURE)
        }
    }

    enum class GoogleSignInMessage {
        FAILURE, LOADING, REGISTRATION_INCOMPLETE, SUCCESS
    }

    enum class AnonymousSignInMessage {
        FAILURE, LOADING, SUCCESS
    }
}
