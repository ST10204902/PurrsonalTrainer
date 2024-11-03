package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.shop

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityItemShopBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ComponentItemPopupBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Item
import za.co.varsitycollege.st10204902.purrsonaltrainer.stores.ItemsStore
import android.util.Log
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager

class ItemShopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemShopBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityItemShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupShopItems()
        setupClickListeners()
        setupCoinsDisplay()
    }

    private fun setupShopItems() {
        // Map the global items to the shop item views
        val items = ItemsStore.globalItems

        // Setup of Items with data from ItemsStore
        setupShopItem(binding.shopItem1, items.getOrNull(0),
            R.drawable.item_nick, R.color.item_nick_start, R.color.item_nick_end)

        setupShopItem(binding.shopItem2, items.getOrNull(1),
            R.drawable.michael_item, R.color.item_michael_start, R.color.item_michael_end)

        setupShopItem(binding.shopItem3, items.getOrNull(2),
            R.drawable.item_harvey, R.color.item_harvey_start, R.color.item_harvey_end)

        setupShopItem(binding.shopItem4, items.getOrNull(3),
            R.drawable.item_jasper, R.color.item_jasper_start, R.color.item_jasper_end)

        setupShopItem(binding.shopItem5, items.getOrNull(4),
            R.drawable.item_anneme, R.color.item_anneme_start, R.color.item_anneme_end)
    }

    private fun setupShopItem(
        shopItemBinding: za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ComponentShopItemBinding,
        item: Item?,
        imageResource: Int,
        startColorRes: Int,
        endColorRes: Int
    ) {
        item?.let {
            shopItemBinding.apply {
                shopItemName.text = it.name
                shopItemImage.setImageResource(imageResource)
                milkcoinsComponent.milkcoinsAmount.reInitialiseComponent(startColorRes, endColorRes)
                root.setOnClickListener { _ ->
                    showItemDetailsDialog(it)
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Add any additional click listeners here if needed
    }

    private fun setupCoinsDisplay() {
        binding.backgroundShopCoins.milkcoinsAmount.reInitialiseComponent(
            R.color.background_balance_start,
            R.color.background_balance_end
        )
    }

    private fun showItemDetailsDialog(item: Item) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = ComponentItemPopupBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        // Set dialog window size
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        with(dialogBinding) {
            itemName.text = item.name
            itemDescription.text = item.description
            tvPrice.text = item.cost.toString()

            // Load item image
            val resourceId = resources.getIdentifier(
                item.itemURI,
                "drawable",
                packageName
            )
            itemImage.setImageResource(resourceId)

            btnPurchase.setOnClickListener {
                val currentUser = UserManager.user!!
                // Implement purchase logic here
                if (currentUser.milkCoins < item.cost) {
                    // Show toast
                    Log.e("ItemShopActivity", "Not enough milk coins ADD USER FEEDBACK")
                    return@setOnClickListener
                }
                else {
                    UserManager.updateUserInventory(item)
                    UserManager.updateEquipedItem(item.itemID)
                    var tempCoins = currentUser.milkCoins
                    tempCoins -= item.cost
                    UserManager.updateMilkCoins(tempCoins)
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}