package za.co.varsitycollege.st10204902.purrsonaltrainer.screens

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityHomeBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments.HomeFragment
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments.RoutinesFragment
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo

/**
 * Object holding useful fragment manipulation functions
 */
object FragmentUtils
{
    //-----------------------------------------------------------//
    //                          PROPERTIES                       //
    //-----------------------------------------------------------//

    lateinit var supportFragmentManager: FragmentManager


    //-----------------------------------------------------------//
    //                          METHODS                          //
    //-----------------------------------------------------------//

    /**
     * Changes the fragment shown to the one given.
     * How to use: 'FragmentUtils.navigateToFragment(DashboardFragment())' where 'DashboardFragment()' is the method
     * used to create the fragment you want to navigate to.
     * NOTE THIS WILL NOT WORK IF THE LANDING ACTIVITY HAS NEVER BEEN NAVIGATED TO
     * @param fragment: the fragment you want to navigate to. E.g. DashboardFragment()
     */
    fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            addToBackStack(null)
            commit()
        }
    }
}

class HomeActivity : AppCompatActivity() {
    //THIS IS THE FRAGMENT MANAGER PAGE
    //TABS: HOME, ROUTINES, CAT

    //-----------------------------------------------------------//
    //                          PROPERTIES                       //
    //-----------------------------------------------------------//

    private lateinit var binding: ActivityHomeBinding

    //-----------------------------------------------------------//
    //                          METHODS                          //
    //-----------------------------------------------------------//

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation Code:
        FragmentUtils.supportFragmentManager = this.supportFragmentManager

        // Custom navbar setup
        setupNavBar()
    }



    override fun onResume(){
        super.onResume()
        onHomeSelected(binding.customNavBar.homeIcon, binding.customNavBar.routinesIcon, binding.customNavBar.settingsIcon)
    }

    // Custom navbar methods
    //-----------------------------------------------------------//

    private fun setupNavBar()
    {
        // Initial fragment shown
        FragmentUtils.navigateToFragment(HomeFragment())

        // ImageViews
        val homeIcon = binding.customNavBar.homeIcon
        val routinesIcon = binding.customNavBar.routinesIcon
        val settingsIcon = binding.customNavBar.settingsIcon

        // onClicks
        homeIcon.setOnClickListener { onHomeSelected(homeIcon, routinesIcon, settingsIcon) }
        routinesIcon.setOnClickListener { onRoutinesSelected(homeIcon, routinesIcon, settingsIcon) }
        settingsIcon.setOnClickListener { onSettingsSelected(homeIcon, routinesIcon, settingsIcon) }
    }

    private fun onHomeSelected(homeIcon: ImageView, routinesIcon: ImageView, settingsIcon: ImageView)
    {
        // Icon
        homeIcon.setImageResource(R.drawable.home_selected)
        routinesIcon.setImageResource(R.drawable.routines_deselected)
        settingsIcon.setImageResource(R.drawable.settings_deselected)
        // Width
        homeIcon.layoutParams.width = 300
        routinesIcon.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        settingsIcon.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        // Height
        homeIcon.layoutParams.height = 121
        routinesIcon.layoutParams.height = 90
        settingsIcon.layoutParams.height = 90
        // Navigation
        FragmentUtils.navigateToFragment(HomeFragment())
    }

    private fun onRoutinesSelected(homeIcon: ImageView, routinesIcon: ImageView, settingsIcon: ImageView)
    {
        // Icon
        routinesIcon.setImageResource(R.drawable.routines_selected)
        homeIcon.setImageResource(R.drawable.home_deselected)
        settingsIcon.setImageResource(R.drawable.settings_deselected)
        // Width
        homeIcon.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        routinesIcon.layoutParams.width = 400
        settingsIcon.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        // Height
        homeIcon.layoutParams.height = 90
        routinesIcon.layoutParams.height = 121
        settingsIcon.layoutParams.height = 90
        // Navigation
        FragmentUtils.navigateToFragment(RoutinesFragment())
    }

    private fun onSettingsSelected(homeIcon: ImageView, routinesIcon: ImageView, settingsIcon: ImageView)
    {
        // Icon
        settingsIcon.setImageResource(R.drawable.settings_selected)
        homeIcon.setImageResource(R.drawable.home_deselected)
        routinesIcon.setImageResource(R.drawable.routines_deselected)
        // Width
        homeIcon.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        routinesIcon.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        settingsIcon.layoutParams.width = 380
        // Height
        homeIcon.layoutParams.height = 90
        routinesIcon.layoutParams.height = 90
        settingsIcon.layoutParams.height = 121
        // Navigation
        navigateTo(this, SettingsActivity::class.java, null)
    }
}
//------------------------***EOF***-----------------------------//