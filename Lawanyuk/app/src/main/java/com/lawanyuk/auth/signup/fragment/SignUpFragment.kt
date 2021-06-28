package com.lawanyuk.auth.signup.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.lawanyuk.R
import com.lawanyuk.auth.model.User
import com.lawanyuk.databinding.FragmentSignUpBinding
import com.lawanyuk.util.lifecycle.EventObserver
import com.lawanyuk.util.requireInput
import kotlin.random.Random

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewModel by activityViewModels()

    private val args: SignUpFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.message.observe(viewLifecycleOwner, EventObserver {
            var isLoading = false
            var buttonSignUpTextResId = R.string.sign_up
            when (it) {
                ViewModel.Message.EMAIL_ADDRESS_ALREADY_REGISTERED ->
                    showSnackbar(R.string.fragment_sign_up_email_address_already_registered_message)
                ViewModel.Message.EMAIL_ADDRESS_MALFORMED ->
                    showSnackbar(R.string.fragment_sign_up_email_address_malformed_message)
                ViewModel.Message.FAILURE ->
                    showSnackbar(R.string.fragment_sign_up_failure_message)
                ViewModel.Message.LOADING -> {
                    isLoading = true
                    buttonSignUpTextResId = R.string.fragment_sign_up_signing_up_user_button_text
                }
                ViewModel.Message.SUCCESS ->
                    findNavController().navigate(SignUpFragmentDirections.actionFinishAuthenticate())
            }
            binding.buttonSignUp.isEnabled = !isLoading
            binding.buttonSignUp.setText(buttonSignUpTextResId)
        })

        if (args.googleAccount) viewModel.getGoogleAccountData()?.let {
            binding.editTextFullName.setText(it["displayName"])
            binding.editTextEmailAddress.setText(it["emailAddress"])
            binding.editTextEmailAddress.isEnabled = false
            binding.editTextPassword.setText(R.string.password_placeholder)
            binding.editTextPassword.isEnabled = false
            binding.editTextPasswordConfirmation.setText(R.string.password_placeholder)
            binding.editTextPasswordConfirmation.isEnabled = false
        }
        else {
            binding.editTextPassword.doAfterTextChanged {
                binding.textInputLayoutPassword.error =
                    if (it.toString()
                            .trim().length < 8
                    ) getString(R.string.fragment_sign_up_weak_password_message)
                    else null
            }
            binding.editTextPasswordConfirmation.doAfterTextChanged {
                binding.textInputLayoutPasswordConfirmation.error =
                    if (it.toString().trim() == binding.editTextPassword.text.toString()
                            .trim()
                    ) null
                    else getString(R.string.fragment_sign_up_confirmation_password_doesnt_match)
            }
        }

        binding.buttonSignUp.setOnClickListener { signUp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showSnackbar(@StringRes resId: Int) {
        Snackbar.make(binding.root, resId, Snackbar.LENGTH_LONG).show()
    }

    private fun signUp() {
        val nik =
            binding.editTextNik.requireInput(binding.textInputLayoutNik) ?: return
        val fullName =
            binding.editTextFullName.requireInput(binding.textInputLayoutFullName) ?: return
        val position =
            binding.editTextPosition.requireInput(binding.textInputLayoutPosition) ?: return
        val homeAddress =
            binding.editTextHomeAddress.requireInput(binding.textInputLayoutHomeAddress) ?: return
        val phoneNumber =
            binding.editTextPhoneNumber.requireInput(binding.textInputLayoutPhoneNumber) ?: return
        val emailAddress = if (args.googleAccount) null
        else binding.editTextEmailAddress.requireInput(binding.textInputLayoutEmailAddress)
            ?: return
        val password = if (args.googleAccount) null
        else {
            if (binding.textInputLayoutPassword.error == null)
                binding.editTextPassword.requireInput(binding.textInputLayoutPassword) ?: return
            else return
        }
        if (binding.textInputLayoutPasswordConfirmation.error == null)
            binding.editTextPasswordConfirmation.requireInput(binding.textInputLayoutPasswordConfirmation)
                ?: return
        else return
        if (!binding.checkBoxTermsAndConditions.isChecked) {
            showSnackbar(R.string.fragment_sign_up_check_box_tac_not_checked_message)
            return
        }

        viewModel.signUp(
            User(
                emailAddress = emailAddress,
                fullName = fullName,
                homeAddress = homeAddress,
                nik = nik,
                password = password,
                phoneNumber = phoneNumber,
                position = position,
                profilePictureUrl = "https://i.pravatar.cc/250?img=${Random.nextInt(1, 70 + 1)}"
            ),
            args.googleAccount
        )
    }
}
