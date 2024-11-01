package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.shop

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityItemShopBinding

class ItemShopActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityItemShopBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityItemShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}