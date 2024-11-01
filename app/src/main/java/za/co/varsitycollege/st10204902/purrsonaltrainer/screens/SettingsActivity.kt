package za.co.varsitycollege.st10204902.purrsonaltrainer.screens

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivitySettingsBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.frontend_logic.SoundManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.settings_activities.AccountDetailsActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.settings_activities.BreakdownActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.settings_activities.CatSettingsActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var soundManager: SoundManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        soundManager = SoundManager(this, R.raw.custom_tap_sound)

        applyFloatUpAnimation(binding.settingsTitle)
        applyFloatUpAnimation(binding.accountButton)
        applyFloatUpAnimation(binding.catSettingsButton)
        applyFloatUpAnimation(binding.statisticsButton)

        binding.accountButton.setOnClickListener() {
            soundManager.playSound()
            val originalBackgroundLogin = binding.accountButton.background
            binding.accountButton.setBackgroundResource(R.drawable.svg_orange_bblbtn_clicked)

            // Revert the background after 2 seconds (2000 milliseconds)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.accountButton.background = originalBackgroundLogin
            }, 400)
            navigateTo(this, AccountDetailsActivity::class.java, null)
        }

        binding.catSettingsButton.setOnClickListener() {
            soundManager.playSound()
            val originalBackgroundCatSettings = binding.catSettingsButton.background
            binding.catSettingsButton.setBackgroundResource(R.drawable.svg_green_bblbtn_clicked)

            // Revert the background after 2 seconds (2000 milliseconds)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.catSettingsButton.background = originalBackgroundCatSettings
            }, 400)
            navigateTo(this, CatSettingsActivity::class.java, null)
        }

        binding.statisticsButton.setOnClickListener() {
            soundManager.playSound()
            val originalBackgroundStatistics = binding.statisticsButton.background
            binding.statisticsButton.setBackgroundResource(R.drawable.svg_purple_bblbtn_clicked)

            // Revert the background after 2 seconds (2000 milliseconds)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.statisticsButton.background = originalBackgroundStatistics
            }, 400)

            navigateTo(this, BreakdownActivity::class.java, null)
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        navigateTo(this, HomeActivity::class.java, null)
        finish()
    }

    private fun applyFloatUpAnimation(view: View?) {
        view?.let {
            val animation = AnimationUtils.loadAnimation(this, R.anim.float_up)
            it.startAnimation(animation)
        }
    }
}