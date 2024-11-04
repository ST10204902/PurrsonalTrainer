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
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityItemShopBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ComponentItemPopupBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Item
import za.co.varsitycollege.st10204902.purrsonaltrainer.stores.ItemsStore

class ItemShopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemShopBinding
    private val TAG = "ItemShopActivity"
    private val currentUser get() = UserManager.user  // Updated here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting ItemShopActivity")
        enableEdgeToEdge()
        binding = ActivityItemShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupShopItems()
        setupClickListeners()
        setupCoinsDisplay()
    }

    private fun setupShopItems() {
        Log.d(TAG, "setupShopItems: Setting up shop items")
        val items = ItemsStore.globalItems

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
        Log.d(TAG, "setupShopItem: Setting up item ${item?.name}")
        item?.let {
            shopItemBinding.apply {
                shopItemName.text = it.name
                shopItemImage.setImageResource(imageResource)
                milkcoinsComponent.milkcoinsAmount.reInitialiseComponent(startColorRes, endColorRes)
                milkcoinsComponent.milkcoinsAmount.text = it.cost.toString()
                root.setOnClickListener { _ ->
                    Log.d(TAG, "setupShopItem: Item ${it.name} clicked")
                    showItemDetailsDialog(it)
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

    private fun showItemDetailsDialog(item: Item) {
        Log.d(TAG, "showItemDetailsDialog: Showing details for item ${item.name}")
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
            itemName.text = item.name
            itemDescription.text = item.description
            val user = currentUser  // Get the latest user data
            if (user != null) {
                if (user.userInventory.contains(item)) {
                    btnPurchase.text = getString(R.string.owned)
                    // set the button colour to show that the item is owned
                    btnPurchase.background = resources.getDrawable(R.drawable.svg_purple_bblbtn)
                    if (user.equippedItem == item.itemID) {
                        btnPurchase.text = getString(R.string.equipped)
                        btnPurchase.background = resources.getDrawable(R.drawable.svg_purple_bblbtn_clicked)
                        Log.d(TAG, "showItemDetailsDialog: Item ${item.name} is equipped")
                    } else {
                        btnPurchase.text = getString(R.string.equip)
                        Log.d(TAG, "showItemDetailsDialog: Item ${item.name} can be equipped")
                    }
                } else {
                    btnPurchase.text = getString(R.string.buy)
                    if (user.milkCoins < item.cost) {
                        btnPurchase.isEnabled = false
                        Log.d(TAG, "showItemDetailsDialog: Not enough coins to buy ${item.name}")
                    }
                }
            }
            val resourceId = resources.getIdentifier(item.itemURI, "drawable", packageName)
            itemImage.setImageResource(resourceId)

            btnPurchase.setOnClickListener {
                Log.d(TAG, "showItemDetailsDialog: btnPurchase clicked for ${item.name}")
                onPurchaseItem(item, dialog, btnPurchase)
            }
        }

        dialog.show()
    }

    private fun onPurchaseItem(item: Item, dialog: Dialog, btnPurchase: Button) {
        Log.d(TAG, "onPurchaseItem: Attempting to purchase/equip item ${item.name}")
        if (currentUser != null) {
            if (currentUser!!.userInventory.contains(item)) {
                UserManager.updateEquipedItem(item.itemID)
                btnPurchase.text = getString(R.string.equipped)
                Log.d(TAG, "onPurchaseItem: Item ${item.name} equipped")
                setupShopItems()
                dialog.dismiss()
            } else if (currentUser!!.milkCoins >= item.cost) {
                val oldBalance = currentUser!!.milkCoins
                val newBalance = oldBalance - item.cost
                UserManager.updateMilkCoins(newBalance)
                UserManager.updateUserInventory(item)
                Log.d(TAG, "onPurchaseItem: Item ${item.name} purchased, new balance: $newBalance")
                setupCoinsDisplay()
                setupShopItems()
                dialog.dismiss()
            } else {
                Log.d(TAG, "onPurchaseItem: Not enough coins to purchase ${item.name}")
            }
        }
    }

}
