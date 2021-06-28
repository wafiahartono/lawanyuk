package com.lawanyuk.main

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.lawanyuk.NavigationAppDirections
import com.lawanyuk.R
import com.lawanyuk.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var flagShowLayout = true
    private var layoutVisible = false

    private lateinit var navController: NavController

    private val topDestinationIdList = listOf(
        R.id.home_fragment,
        R.id.news_fragment,
        R.id.reminder_fragment,
        R.id.profile_fragment
    )

    private val viewModel: ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.root.visibility = View.INVISIBLE
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host)

        viewModel.authState.observe(this, Observer {
            updateLayoutByAuthState(it.peekContent())
        })

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (flagShowLayout && !layoutVisible) {
                lifecycleScope.launch {
                    delay(1000)
                    TransitionManager.beginDelayedTransition(binding.root as ViewGroup, Fade())
                    binding.root.visibility = View.VISIBLE
                    layoutVisible = true
                }
            }
            binding.bottomNavigationView.visibility =
                if (topDestinationIdList.contains(destination.id)) View.VISIBLE
                else View.GONE
        }

        binding.bottomNavigationView.setupWithNavController(navController)
    }

    private fun updateLayoutByAuthState(authState: ViewModel.AuthState) {
        var isAnonymous = false
        when (authState) {
            ViewModel.AuthState.ANONYMOUS -> isAnonymous = true
            ViewModel.AuthState.REGISTRATION_INCOMPLETE -> {
                if (navController.currentDestination?.id != R.id.sign_up_fragment)
                    navController.navigate(NavigationAppDirections.actionRegisterUserData(true))
            }
            ViewModel.AuthState.UNAUTHENTICATED -> {
                if (navController.currentDestination?.id != R.id.onboarding_fragment)
                    navController.navigate(NavigationAppDirections.actionAuthenticate())
            }
            else -> {
            }
        }
        binding.bottomNavigationView.menu.forEach { menuItem ->
            if (menuItem.itemId != R.id.home_fragment) menuItem.isEnabled = !isAnonymous
        }
        flagShowLayout = true
    }
}
