package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.shop

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityBackgroundShopBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ComponentItemPopupBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ComponentShopItemBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserBackground
import za.co.varsitycollege.st10204902.purrsonaltrainer.stores.BackgroundStore

class BackgroundShopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBackgroundShopBinding
    private val TAG = "BackgroundShopActivity"
    private val currentUser get() = UserManager.user

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBackgroundShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupShopBackgrounds()
        setupClickListeners()
        setupCoinsDisplay()
    }

    private fun setupShopBackgrounds() {
        Log.d(TAG, "setupShopBackgrounds: Setting up shop backgrounds")
        val backgrounds = BackgroundStore.globalBackgrounds

        setupShopBackground(binding.shopItemBackground1, backgrounds.getOrNull(0),
            R.drawable.shop_background_1)

        setupShopBackground(binding.shopItemBackground2, backgrounds.getOrNull(1),
            R.drawable.shop_background_2)

        setupShopBackground(binding.shopItemBackground3, backgrounds.getOrNull(2),
            R.drawable.shop_background_3)

        setupShopBackground(binding.shopItemBackground4, backgrounds.getOrNull(3),
            R.drawable.shop_background_4)
    }

    private fun setupShopBackground(
        shopBackgroundBinding: ComponentShopItemBinding,
        background: UserBackground?,
        imageResource: Int
    ) {
        Log.d(TAG, "setupShopBackground: Setting up background ${background?.name}")
        background?.let {
            shopBackgroundBinding.apply {
                shopItemName.text = it.name
                shopItemImage.setImageResource(imageResource)
                milkcoinsComponent.milkcoinsAmount.reInitialiseComponent(
                    R.color.background_price_start,
                    R.color.background_price_end
                )
                milkcoinsComponent.milkcoinsImage.setImageResource(R.drawable.milkcoin)
                milkcoinsComponent.milkcoinsAmount.text = it.cost.toString()

                val user = currentUser
                if (user != null) {
                    if (user.userBackgrounds.containsKey(it.backgroundID)) {
                        milkcoinsComponent.milkcoinsAmount.text = ""
                        milkcoinsComponent.milkcoinsImage.isVisible = false
                    }
                }

                root.setOnClickListener {
                    showBackgroundDetailsDialog(background)
                }
            }
        }
    }

    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up click listeners")
        // Add any additional click listeners here if needed
    }

    private fun setupCoinsDisplay() {
        Log.d(TAG, "setupCoinsDisplay: Displaying user coins")
        binding.backgroundShopCoins.milkcoinsAmount.text = currentUser?.milkCoins.toString()
        binding.backgroundShopCoins.milkcoinsAmount.reInitialiseComponent(
            R.color.background_balance_start,
            R.color.background_balance_end
        )
    }

    private fun showBackgroundDetailsDialog(background: UserBackground) {
        Log.d(TAG, "showBackgroundDetailsDialog: Showing details for background ${background.name}")
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = ComponentItemPopupBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        with(dialogBinding) {
            itemName.text = background.name
            tvPrice.text = background.cost.toString()
            val user = currentUser
            if (user != null) {
                if (user.userBackgrounds.containsKey(background.backgroundID)) {
                    btnPurchase.text = getString(R.string.owned)
                    ivCoin.isVisible = false
                    tvPrice.text = ""
                    btnPurchase.background = resources.getDrawable(R.drawable.svg_purple_bblbtn)
                    if (user.backgroundURI == background.backgroundURI) {
                        btnPurchase.text = getString(R.string.equipped)
                        btnPurchase.background =
                            resources.getDrawable(R.drawable.svg_purple_bblbtn_clicked)
                        Log.d(TAG, "showBackgroundDetailsDialog: Background ${background.name} is equipped")
                    } else {
                        btnPurchase.text = getString(R.string.equip)
                        Log.d(TAG, "showBackgroundDetailsDialog: Background ${background.name} can be equipped")
                    }
                } else {
                    btnPurchase.text = getString(R.string.buy)
                    if (user.milkCoins < background.cost) {
                        btnPurchase.isEnabled = false
                        btnPurchase.background = resources.getDrawable(R.drawable.svg_grey_bblbtn)
                        Log.d(TAG, "showBackgroundDetailsDialog: Not enough coins to buy ${background.name}")
                    }
                }
            }

            btnPurchase.setOnClickListener {
                Log.d(TAG, "showBackgroundDetailsDialog: btnPurchase clicked for ${background.name}")
                onPurchaseBackground(background, dialog, btnPurchase)
            }
        }

        dialog.show()
    }

    private fun onPurchaseBackground(background: UserBackground, dialog: Dialog, btnPurchase: Button) {
        Log.d(TAG, "onPurchaseBackground: Attempting to purchase/equip background ${background.name}")
        val user = currentUser
        if (user != null) {
            if (user.userBackgrounds.containsKey(background.backgroundID)) {
                UserManager.updateBackgroundURI(background.backgroundURI)
                btnPurchase.text = getString(R.string.equipped)
                Log.d(TAG, "onPurchaseBackground: Background ${background.name} equipped")
                setupShopBackgrounds()
                dialog.dismiss()
            } else if (user.milkCoins >= background.cost) {
                val newBalance = user.milkCoins - background.cost
                UserManager.updateMilkCoins(newBalance)
                UserManager.addUserBackground(background)
                Log.d(TAG, "onPurchaseBackground: Background ${background.name} purchased, new balance: $newBalance")
                setupCoinsDisplay()
                setupShopBackgrounds()
                dialog.dismiss()
            } else {
                Log.d(TAG, "onPurchaseBackground: Not enough coins to purchase ${background.name}")
            }
        }
    }
}
