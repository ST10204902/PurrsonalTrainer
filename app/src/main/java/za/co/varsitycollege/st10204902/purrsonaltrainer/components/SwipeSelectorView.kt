package za.co.varsitycollege.st10204902.purrsonaltrainer.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.SwipableComponentAdapter

class SwipeSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val viewPager: ViewPager2
    private var adapter: SwipableComponentAdapter? = null

    init {
        // Inflate the layout
        val view = LayoutInflater.from(context)
            .inflate(R.layout.component_swipable_selector, this, true)
        viewPager = view.findViewById(R.id.viewPager)
    }

    fun setItems(
        avatarList: List<Int>,
        imageMargin: Int = 0, // Default margin of 0
        onAvatarSelected: (Int) -> Unit,
    ) {
        adapter = SwipableComponentAdapter(avatarList, imageMargin, onAvatarSelected)
        viewPager.adapter = adapter
    }

    fun getCurrentItemPosition(): Int {
        return viewPager.currentItem
    }

    fun setCurrentItemPosition(position: Int, smoothScroll: Boolean = true) {
        viewPager.setCurrentItem(position, smoothScroll)
    }
}
