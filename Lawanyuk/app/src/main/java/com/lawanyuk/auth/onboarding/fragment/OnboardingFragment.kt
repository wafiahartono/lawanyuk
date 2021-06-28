package com.lawanyuk.auth.onboarding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import coil.api.load
import com.lawanyuk.R
import com.lawanyuk.databinding.FragmentOnboardingBinding
import com.lawanyuk.util.logd

class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val totalPage = 3
    private var currentPage = 0

    private val imageResIds = listOf(
        R.drawable.svg_undraw_my_app_grf2,
        R.drawable.svg_undraw_doctor_kw5l,
        R.drawable.svg_undraw_medical_care_movn
    )

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedCallback = addOnBackPressedCallback()
    }

    private fun addOnBackPressedCallback() = requireActivity().onBackPressedDispatcher.addCallback {
        setPage(currentPage, --currentPage)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentPage = 1

        binding.buttonAction.setOnClickListener {
            if (currentPage == totalPage) {
                onBackPressedCallback.remove()
                findNavController().navigate(OnboardingFragmentDirections.actionShowAuthOptions())
            } else setPage(currentPage, ++currentPage)
        }

        setPage(currentPage, currentPage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setPage(previousPage: Int, nextPage: Int) {
        logd("setPage($previousPage, $nextPage)")
        if (nextPage == 1)
            onBackPressedCallback.remove()
        else if (previousPage == 1 && nextPage == 2)
            onBackPressedCallback = addOnBackPressedCallback()
        val i = nextPage - 1
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        binding.imageViewImage.load(imageResIds[i])
        binding.textViewTitle.text = resources.getStringArray(R.array.onboarding_titles)[i]
        binding.textViewSubtitle.text = resources.getStringArray(R.array.onboarding_subtitles)[i]
        binding.buttonAction.setText(if (nextPage == totalPage) R.string.start else R.string.next)
    }
}
