package com.lawanyuk.auth.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.lawanyuk.R
import com.lawanyuk.databinding.FragmentAuthBinding
import com.lawanyuk.util.lifecycle.EventObserver
import com.lawanyuk.util.logw

class AuthFragment : Fragment() {
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewModel by activityViewModels()

    private lateinit var navController: NavController

    private lateinit var googleSignInIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        googleSignInIntent = GoogleSignIn.getClient(
            requireContext(),
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()
        ).signInIntent
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.googleSignInMessage.observe(viewLifecycleOwner, EventObserver {
            var isLoading = false
            var buttonSignInWithGoogleTextResId = R.string.fragment_auth_button_google_sign_in_text
            when (it) {
                ViewModel.GoogleSignInMessage.FAILURE ->
                    Snackbar.make(
                        binding.root,
                        R.string.fragment_auth_failure_message,
                        Snackbar.LENGTH_LONG
                    ).show()
                ViewModel.GoogleSignInMessage.LOADING -> {
                    isLoading = true
                    buttonSignInWithGoogleTextResId = R.string.fragment_auth_signing_in_user_message
                }
                ViewModel.GoogleSignInMessage.REGISTRATION_INCOMPLETE ->
                    navController.navigate(AuthFragmentDirections.actionRegisterUserData(true))
                ViewModel.GoogleSignInMessage.SUCCESS ->
                    finishAuthenticate()
            }
            updateLayoutByLoading(isLoading)
            binding.buttonSignInWithGoogle.setText(buttonSignInWithGoogleTextResId)
        })

        viewModel.anonymousSignInMessage.observe(viewLifecycleOwner, EventObserver {
            var isLoading = false
            var buttonSignInAnonymouslyTextResId =
                R.string.fragment_auth_button_anonymous_sign_in_text
            when (it) {
                ViewModel.AnonymousSignInMessage.FAILURE ->
                    Snackbar.make(
                        binding.root,
                        R.string.fragment_auth_failure_message,
                        Snackbar.LENGTH_LONG
                    ).show()
                ViewModel.AnonymousSignInMessage.LOADING -> {
                    isLoading = true
                    buttonSignInAnonymouslyTextResId =
                        R.string.fragment_auth_signing_in_user_message
                }
                ViewModel.AnonymousSignInMessage.SUCCESS ->
                    finishAuthenticate()
            }
            updateLayoutByLoading(isLoading)
            binding.buttonSignInAnonymously.setText(buttonSignInAnonymouslyTextResId)
        })

        binding.buttonSignUp.setOnClickListener {
            navController.navigate(AuthFragmentDirections.actionSignUp(false))
        }
        binding.buttonSignInWithGoogle.setOnClickListener {
            startActivityForResult(googleSignInIntent, REQUEST_CODE_GOOGLE_SIGN_IN)
        }
        binding.buttonSignInAnonymously.setOnClickListener {
            viewModel.signInAnonymously()
        }
        binding.buttonSignIn.setOnClickListener {
            navController.navigate(AuthFragmentDirections.actionSignIn())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN && resultCode == RESULT_OK && data != null) {
            try {
                GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)?.idToken?.let {
                    viewModel.signInWithGoogle(it)
                }
            } catch (e: Exception) {
                logw("", e)
            }
        }
    }

    private fun finishAuthenticate() {
        navController.navigate(AuthFragmentDirections.actionFinishAuthenticate())
    }

    private fun updateLayoutByLoading(isLoading: Boolean) {
        binding.buttonSignUp.isEnabled = !isLoading
        binding.buttonSignInWithGoogle.isEnabled = !isLoading
        binding.buttonSignInAnonymously.isEnabled = !isLoading
        binding.buttonSignIn.isEnabled = !isLoading
    }

    companion object {
        private const val REQUEST_CODE_GOOGLE_SIGN_IN = 0
    }
}
