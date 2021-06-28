package com.lawanyuk.auth.signup.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.lawanyuk.auth.model.User
import com.lawanyuk.repository.Repository
import com.lawanyuk.util.lifecycle.Event
import com.lawanyuk.util.logw
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val _message = MutableLiveData<Event<Message>>()
    val message: LiveData<Event<Message>> = _message

    fun signUp(user: User, googleAccount: Boolean) = viewModelScope.launch {
        _message.value = Event(Message.LOADING)
        try {
            Repository.signUp(user, googleAccount)
            _message.value = Event(Message.SUCCESS)
        } catch (e: Exception) {
            logw("signUp failed", e)
            _message.value = Event(
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> Message.EMAIL_ADDRESS_MALFORMED
                    is FirebaseAuthUserCollisionException -> Message.EMAIL_ADDRESS_ALREADY_REGISTERED
                    else -> Message.FAILURE
                }
            )
        }
    }

    fun getGoogleAccountData(): Map<String, String>? {
        return try {
            Repository.getUserGoogleAccountData()
        } catch (e: Exception) {
            logw("getGoogleAccountData failed", e)
            null
        }
    }

    enum class Message {
        EMAIL_ADDRESS_ALREADY_REGISTERED, EMAIL_ADDRESS_MALFORMED, FAILURE, LOADING, SUCCESS
    }
}
