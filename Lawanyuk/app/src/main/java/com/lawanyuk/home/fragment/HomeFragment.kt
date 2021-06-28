package com.lawanyuk.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import coil.api.load
import coil.transform.CircleCropTransformation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lawanyuk.R
import com.lawanyuk.auth.model.User
import com.lawanyuk.databinding.FragmentHomeBinding
import com.lawanyuk.util.logd
import kotlin.random.Random

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewModel by activityViewModels()

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user.observe(viewLifecycleOwner, Observer { updateLayoutByUser(it) })

        binding.layoutHeader.imageViewUserProfilePicture.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.home_fragment_sign_out_dialog_message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    viewModel.signOut(getGoogleSignInClient())
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }

        binding.layoutMenuReport.textViewMenuText.setText(
            R.string.fragment_home_menu_report_text
        )
        binding.layoutMenuReport.root.setOnClickListener {
            navController.navigate(HomeFragmentDirections.actionOpenReportMenu())
        }

        binding.layoutMenuConsultation.textViewMenuText.setText(
            R.string.fragment_home_menu_consultation_text
        )
        binding.layoutMenuConsultation.root.setOnClickListener {
            navController.navigate(HomeFragmentDirections.actionOpenConsultationMenu())
        }

        binding.layoutMenuMedicalFacility.textViewMenuText.setText(
            R.string.fragment_home_menu_medical_facility_text
        )
        binding.layoutMenuMedicalFacility.root.setOnClickListener {
            navController.navigate(HomeFragmentDirections.actionOpenMedicalFacilityMenu())
        }

        binding.layoutMenuMachineLearning.textViewMenuText.setText(
            R.string.fragment_home_menu_machine_learning_text
        )
        binding.layoutMenuMachineLearning.root.setOnClickListener {
            navController.navigate(HomeFragmentDirections.actionOpenMachineLearningMenu())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateLayoutByUser(user: User?) {
        logd("updateLayoutByUser user: $user")
        if (user == null) {
            binding.layoutHeader.imageViewUserProfilePicture.load(R.drawable.shape_guest_profile_picture)
            { transformations(CircleCropTransformation()) }

            binding.layoutHeader.textViewUserFullName.setText(R.string.fragment_home_guest_name_placeholder)
            binding.layoutHeader.textViewUserPosition.visibility = View.GONE
            binding.layoutHeader.textViewUserHomeAddress.visibility = View.GONE
        } else {
            if (Random.nextBoolean())
                binding.layoutHeader.textViewAppFlavor.setText(R.string.app_name_pro)

            binding.layoutHeader.imageViewUserProfilePicture.load(user.profilePictureUrl)
            { transformations(CircleCropTransformation()) }

            binding.layoutHeader.textViewUserFullName.text = user.fullName
            binding.layoutHeader.textViewUserPosition.text = user.position
            binding.layoutHeader.textViewUserHomeAddress.text = user.homeAddress
        }
    }

    private fun getGoogleSignInClient() = GoogleSignIn.getClient(
        requireContext(),
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
    )
}
