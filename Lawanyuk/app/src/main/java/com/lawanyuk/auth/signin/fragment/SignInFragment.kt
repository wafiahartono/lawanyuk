package com.lawanyuk.auth.signin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.lawanyuk.R
import com.lawanyuk.auth.model.User
import com.lawanyuk.databinding.FragmentSignInBinding
import com.lawanyuk.util.lifecycle.EventObserver
import com.lawanyuk.util.requireInput

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.message.observe(viewLifecycleOwner, EventObserver {
            var isLoading = false
            var buttonSignInTextResId = R.string.sign_in
            when (it) {
                ViewModel.Message.FAILURE ->
                    Snackbar.make(
                        binding.root,
                        R.string.fragment_sign_in_wrong_credential_message,
                        Snackbar.LENGTH_LONG
                    ).show()
                ViewModel.Message.LOADING -> {
                    isLoading = true
                    buttonSignInTextResId = R.string.fragment_sign_in_signing_in_user_button_text
                }
                ViewModel.Message.SUCCESS ->
                    findNavController().navigate(SignInFragmentDirections.actionFinishAuthenticate())
            }
            binding.buttonSignIn.isEnabled = !isLoading
            binding.buttonSignIn.setText(buttonSignInTextResId)
        })

        binding.buttonSignIn.setOnClickListener { signIn() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun signIn() {
        val emailAddress =
            binding.editTextEmailAddress.requireInput(binding.textInputLayoutEmailAddress) ?: return
        val password =
            binding.editTextPassword.requireInput(binding.textInputLayoutPassword) ?: return

        authViewModel.signIn(
            User(
                emailAddress = emailAddress,
                password = password
            )
        )
    }
}
