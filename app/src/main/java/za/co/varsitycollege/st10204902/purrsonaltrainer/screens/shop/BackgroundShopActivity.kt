package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.shop

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityBackgroundShopBinding

class BackgroundShopActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityBackgroundShopBinding
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBackgroundShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup of Items
        val background1 = binding.shopItemBackground1
        background1.shopItemName.text = "*BG Name Here"
        background1.shopItemImage.setImageResource(R.drawable.shop_background_1)
        background1.milkcoinsComponent.milkcoinsAmount.reInitialiseComponent(R.color.background_price_start, R.color.background_price_end)

        val background2 = binding.shopItemBackground2
        background2.shopItemName.text = "*BG Name Here"
        background2.shopItemImage.setImageResource(R.drawable.shop_background_2)
        background2.milkcoinsComponent.milkcoinsAmount.reInitialiseComponent(R.color.background_price_start, R.color.background_price_end)

        val background3 = binding.shopItemBackground3
        background3.shopItemName.text = "*BG Name Here"
        background3.shopItemImage.setImageResource(R.drawable.shop_background_3)
        background3.milkcoinsComponent.milkcoinsAmount.reInitialiseComponent(R.color.background_price_start, R.color.background_price_end)

        val background4 = binding.shopItemBackground4
        background4.shopItemName.text = "*BG Name Here"
        background4.shopItemImage.setImageResource(R.drawable.shop_background_4)
        background4.milkcoinsComponent.milkcoinsAmount.reInitialiseComponent(R.color.background_price_start, R.color.background_price_end)

        // Setup Coins
        binding.backgroundShopCoins.milkcoinsAmount.reInitialiseComponent(R.color.background_balance_start, R.color.background_balance_end)
    }
}