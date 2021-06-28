package com.lawanyuk.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.lawanyuk.repository.Repository
import com.lawanyuk.util.lifecycle.Event
import com.lawanyuk.util.logw
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val _authState = MutableLiveData<Event<AuthState>>()
    val authState: LiveData<Event<AuthState>> = _authState

    private val authStateListener = FirebaseAuth.AuthStateListener {
        viewModelScope.launch {
            try {
                _authState.value = Event(
                    when (Repository.getAuthState()) {
                        Repository.AuthState.ANONYMOUS -> AuthState.ANONYMOUS
                        Repository.AuthState.AUTHENTICATED -> {
                            if (Repository.isUserFullyRegistered()) AuthState.AUTHENTICATED
                            else AuthState.REGISTRATION_INCOMPLETE
                        }
                        Repository.AuthState.UNAUTHENTICATED -> AuthState.UNAUTHENTICATED
                    }
                )
            } catch (e: Exception) {
                logw("authStateListener viewModelScope.launch failed", e)
            }
        }
    }

    init {
        Repository.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        Repository.removeAuthStateListener(authStateListener)
    }

    enum class AuthState {
        ANONYMOUS, AUTHENTICATED, REGISTRATION_INCOMPLETE, UNAUTHENTICATED
    }
}
