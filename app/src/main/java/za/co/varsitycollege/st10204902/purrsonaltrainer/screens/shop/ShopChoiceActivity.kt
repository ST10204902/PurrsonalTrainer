package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.shop

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityShopChoiceBinding

class ShopChoiceActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityShopChoiceBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityShopChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}