package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.shop

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityShopChoiceBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo

class ShopChoiceActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityShopChoiceBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityShopChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation to ItemShopActivity
        binding.itemsShopButton.setOnClickListener {
            navigateTo(this, ItemShopActivity::class.java, null)
        }

        // Navigating to BackgroundShopActivity
        binding.backgroundShopButton.setOnClickListener {
            navigateTo(this, BackgroundShopActivity::class.java, null)
        }
    }
}