package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.settings_activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.components.SwipeSelectorView
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityCatSettingsBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.SettingsActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.GetCatAvatars
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo

/**
 * Activity for managing cat settings.
 */
class CatSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCatSettingsBinding
    private lateinit var swipableSelector: SwipeSelectorView

    /**
     * Called when the activity is starting.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCatSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPage()

        binding.catSettingsDoneButton.setOnClickListener {
            try
            {
                // Update fields and navigate back
                UserManager.updateCatName(binding.catSettingsCatName.text.toString())
                UserManager.updateCatURI(swipableSelector.getCurrentItemPosition().toString())
                navigateTo(this, SettingsActivity::class.java, null)
            }
            catch (e: Exception)
            {
                Log.e("CatSettingsScreen", "Failed to save user's changes", e)
            }
        }
    }

    private fun setupPage()
    {
        // CatAvatarSetup
        swipableSelector = binding.catSettingsCatSwipeSelector
        swipableSelector.setItems(GetCatAvatars()) {}

        // Get the fur pattern currently chosen by the user
        try
        {
            val position = UserManager.user!!.catURI.toInt()
            swipableSelector.setCurrentItemPosition(position)
        }
        catch (e: Exception)
        {
            Log.e("CatSettingsScreen", "Failed to set the user's cat avatar", e)
        }

        // Get the current cat name
        try
        {
            // cat name setup
            val catName = UserManager.user!!.catName
            binding.catSettingsCatName.setText(catName)
        }
        catch (e: Exception)
        {
            Log.e("CatSettingsScreen", "Failed to set the user's cat name", e)
        }
    }
}