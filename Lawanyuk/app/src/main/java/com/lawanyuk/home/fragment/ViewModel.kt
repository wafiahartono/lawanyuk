package com.lawanyuk.home.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.lawanyuk.auth.model.User
import com.lawanyuk.repository.Repository
import com.lawanyuk.util.logd
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val authStateListener = FirebaseAuth.AuthStateListener {
        viewModelScope.launch {
            val authState = Repository.getAuthState()
            logd("authStateListener FirebaseAuth.AuthStateListener authState: ${authState.name}")
            _user.value =
                if (authState == Repository.AuthState.AUTHENTICATED) Repository.getUser()
                else null
        }
    }

    init {
        Repository.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        Repository.removeAuthStateListener(authStateListener)
    }

    fun signOut(googleSignInClient: GoogleSignInClient) = viewModelScope.launch {
        Repository.signOut(googleSignInClient)
    }
}
