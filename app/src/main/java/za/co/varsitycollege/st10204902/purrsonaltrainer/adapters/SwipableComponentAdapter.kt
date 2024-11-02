package za.co.varsitycollege.st10204902.purrsonaltrainer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.st10204902.purrsonaltrainer.R

class SwipableComponentAdapter(
    private val itemList: List<Int>, // Assuming using drawable resource IDs
    private val onItemPressed: (Int) -> Unit // Callback for when an avatar is selected
) : RecyclerView.Adapter<SwipableComponentAdapter.AvatarViewHolder>() {

    inner class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.swipable_item_image)

        fun bind(avatarResId: Int) {
            avatarImageView.setImageResource(avatarResId)
            itemView.setOnClickListener {
                onItemPressed(avatarResId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_swipable_component, parent, false)
        return AvatarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size
}
