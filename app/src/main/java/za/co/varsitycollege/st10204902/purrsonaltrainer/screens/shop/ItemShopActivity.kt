package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.shop

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.ShopItemAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityItemShopBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ComponentItemPopupBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Item
import za.co.varsitycollege.st10204902.purrsonaltrainer.stores.ItemsStore
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

class ItemShopActivity : AppCompatActivity(), ShopItemAdapter.OnItemClickListener {
    private lateinit var binding: ActivityItemShopBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvItems.apply {
            layoutManager = GridLayoutManager(this@ItemShopActivity, 2)
            adapter = ShopItemAdapter(ItemsStore.globalItems, this@ItemShopActivity)
        }
    }

    override fun onItemClick(item: Item) {
        showItemDetailsDialog(item)
    }

    //I did the god-forbidden thing and did different dialog logic... sorry! will explain it though
    private fun showItemDetailsDialog(item: Item) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = ComponentItemPopupBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        // Set dialog window size to be the same as the pop up
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // Optional: remove any dim/shadow behind the dialog
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        with(dialogBinding) {
            // link the item details stuff to the dialog
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

            // Button click listeners
            btnPurchase.setOnClickListener {
                // Implement purchase logic here
                dialog.dismiss()
            }

        }

        dialog.show()
    }
}
