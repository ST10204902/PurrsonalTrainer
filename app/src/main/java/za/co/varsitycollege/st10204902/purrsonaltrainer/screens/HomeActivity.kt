package za.co.varsitycollege.st10204902.purrsonaltrainer.screens

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityHomeBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments.CatFragment
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
        onHomeSelected(binding.customNavBar.homeIcon, binding.customNavBar.routinesIcon, binding.customNavBar.catIcon)
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
        val catIcon = binding.customNavBar.catIcon

        // onClicks
        homeIcon.setOnClickListener { onHomeSelected(homeIcon, routinesIcon, catIcon) }
        routinesIcon.setOnClickListener { onRoutinesSelected(homeIcon, routinesIcon, catIcon) }
        catIcon.setOnClickListener { onCatSelected(homeIcon, routinesIcon, catIcon) }
    }

    private fun onHomeSelected(homeIcon: ImageView, routinesIcon: ImageView, catIcon: ImageView)
    {
        // Icon
        homeIcon.setImageResource(R.drawable.home_selected)
        routinesIcon.setImageResource(R.drawable.routines_deselected)
        catIcon.setImageResource(R.drawable.cat_deselected)
        // Width
        homeIcon.layoutParams.width = 300
        routinesIcon.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        catIcon.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        // Height
        homeIcon.layoutParams.height = 121
        routinesIcon.layoutParams.height = 90
        catIcon.layoutParams.height = 90
        // Navigation
        FragmentUtils.navigateToFragment(HomeFragment())
    }

    private fun onRoutinesSelected(homeIcon: ImageView, routinesIcon: ImageView, catIcon: ImageView)
    {
        // Icon
        routinesIcon.setImageResource(R.drawable.routines_selected)
        homeIcon.setImageResource(R.drawable.home_deselected)
        catIcon.setImageResource(R.drawable.cat_deselected)
        // Width
        homeIcon.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        routinesIcon.layoutParams.width = 400
        catIcon.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        // Height
        homeIcon.layoutParams.height = 90
        routinesIcon.layoutParams.height = 121
        catIcon.layoutParams.height = 90
        // Navigation
        FragmentUtils.navigateToFragment(RoutinesFragment())
    }

    private fun onCatSelected(homeIcon: ImageView, routinesIcon: ImageView, catIcon: ImageView)
    {
        // Icon
        catIcon.setImageResource(R.drawable.cat_selected)
        homeIcon.setImageResource(R.drawable.home_deselected)
        routinesIcon.setImageResource(R.drawable.routines_deselected)
        // Width
        homeIcon.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        routinesIcon.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        catIcon.layoutParams.width = 285
        // Height
        homeIcon.layoutParams.height = 90
        routinesIcon.layoutParams.height = 90
        catIcon.layoutParams.height = 121
        // Navigation
        FragmentUtils.navigateToFragment(CatFragment())
    }
}
//------------------------***EOF***-----------------------------//