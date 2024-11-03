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

        // Setup of Items
        val background1 = binding.shopItem1
        background1.shopItemName.text = "Nick's Creatine"
        background1.shopItemImage.setImageResource(R.drawable.item_nick)
        background1.milkcoinsComponent.milkcoinsAmount.reInitialiseComponent(R.color.item_nick_start, R.color.item_nick_end)

        val background2 = binding.shopItem2
        background2.shopItemName.text = "Michael's Earphones"
        background2.shopItemImage.setImageResource(R.drawable.item_michael)
        background2.milkcoinsComponent.milkcoinsAmount.reInitialiseComponent(R.color.item_michael_start, R.color.item_michael_end)

        val background3 = binding.shopItem3
        background3.shopItemName.text = "Harvey's Cookies"
        background3.shopItemImage.setImageResource(R.drawable.item_harvey)
        background3.milkcoinsComponent.milkcoinsAmount.reInitialiseComponent(R.color.item_harvey_start, R.color.item_harvey_end)

        val background4 = binding.shopItem4
        background4.shopItemName.text = "Jasper's Hoodie"
        background4.shopItemImage.setImageResource(R.drawable.item_jasper)
        background4.milkcoinsComponent.milkcoinsAmount.reInitialiseComponent(R.color.item_jasper_start, R.color.item_jasper_end)

        val background5 = binding.shopItem5
        background5.shopItemName.text = "Anneme's Plumbob"
        background5.shopItemImage.setImageResource(R.drawable.item_anneme)
        background5.milkcoinsComponent.milkcoinsAmount.reInitialiseComponent(R.color.item_anneme_start, R.color.item_anneme_end)

        // Setup Coins
        binding.backgroundShopCoins.milkcoinsAmount.reInitialiseComponent(R.color.background_balance_start, R.color.background_balance_end)
    }
}