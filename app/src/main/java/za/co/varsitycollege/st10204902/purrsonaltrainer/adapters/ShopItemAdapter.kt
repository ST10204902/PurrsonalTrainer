package za.co.varsitycollege.st10204902.purrsonaltrainer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ItemItemshopitemBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Item

/**
 * Adapter class for displaying a list of items in a RecyclerView.
 *
 * @property items List of items to be displayed.
 * @property itemClickListener Listener for item click events.
 */
class ShopItemAdapter(
    private val items: List<Item>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ShopItemAdapter.ItemViewHolder>() {

    /**
     * Interface for handling item click events.
     */
    interface OnItemClickListener {
        /**
         * Called when an item is clicked.
         *
         * @param item The clicked item.
         */
        fun onItemClick(item: Item)
    }

    /**
     * Creates a new ViewHolder for an item.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The view type of the new View.
     * @return A new ItemViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemItemshopitemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    /**
     * Binds an item to a ViewHolder.
     *
     * @param holder The ViewHolder to bind the item to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    /**
     * Returns the total number of items in the list.
     *
     * @return The total number of items.
     */
    override fun getItemCount() = items.size

    /**
     * ViewHolder class for an item.
     *
     * @property binding The binding for the item view.
     */
    inner class ItemViewHolder(private val binding: ItemItemshopitemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds an item to the view.
         *
         * @param item The item to bind.
         */
        fun bind(item: Item) {
            with(binding) {
                // Load item image
                val resourceId = root.context.resources.getIdentifier(
                    item.itemURI,
                    "drawable",
                    root.context.packageName
                )
                itemImage.setImageResource(resourceId)

                // Set click listener
                root.setOnClickListener { itemClickListener.onItemClick(item) }
            }
        }
    }
}