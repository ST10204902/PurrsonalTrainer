package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.shop

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityShopChoiceBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo

class ShopChoiceActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityShopChoiceBinding

    private val backgroundsList = listOf(
        R.drawable.shop_door_backgrounds,
        R.drawable.shop_door_items
    )

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityShopChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Shop swiping
        val swipeView = binding.shopChoiceSwipable
        swipeView.setItems(backgroundsList) {
            when (swipeView.getCurrentItemPosition())
            {
                0 ->
                {
                    navigateTo(this, BackgroundShopActivity::class.java, null)
                }
                1 ->
                {
                    navigateTo(this, ItemShopActivity::class.java, null)
                }
            }
        }
    }
}