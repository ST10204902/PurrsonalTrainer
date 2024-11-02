package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.login_register

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityProfileSetupBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.HomeActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.GetCatAvatars
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo

class ProfileSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cat name entered by the user (for whoever needs this)
        val catName = binding.profileSetupCatName.text

        // CatAvatarSetup
        val swipableSelector = binding.profileSetupCatSwipeSelector
        swipableSelector.setItems(GetCatAvatars()) {}

        // Navigation from next button
        binding.profileSetupNext.setOnClickListener {
            // Add the cat information to Firebase
            UserManager.updateCatName(catName.toString())
            UserManager.updateCatURI(swipableSelector.getCurrentItemPosition().toString())
            Toast.makeText(this, swipableSelector.getCurrentItemPosition().toString(), Toast.LENGTH_SHORT).show()

            navigateTo(this, HomeActivity::class.java, null)
        }
    }
}