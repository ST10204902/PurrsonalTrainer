package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.frontend_logic.GradientTextView
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.SettingsActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.shop.ShopChoiceActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.GetCatDrawableId
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.GamifiedStatsManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo

/**
 * A simple [Fragment] subclass.
 * Use the [CatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CatFragment : Fragment() {
    private val currentUser get() = UserManager.user  // Updated here
    private val TAG = "CatFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    { // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cat, container, false)
        // set the background image
        if (currentUser != null) {
            val backgroundURI = currentUser!!.backgroundURI
            if (backgroundURI != "") {
                try {
                    val layout = view.findViewById<LinearLayout>(R.id.cat_fragment)

                    val resourceId = resources.getIdentifier(
                        backgroundURI,
                        "drawable", requireContext().packageName
                    )
                    val drawable = ContextCompat.getDrawable(requireContext(), resourceId)

                    layout.background = drawable

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to set background", e)
                }
            } else {
                val layout = view.findViewById<LinearLayout>(R.id.cat_fragment)
                layout.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.cat_background_1)
            }
        }

        // Setup xp bar and level
        val xpBar = view.findViewById<LinearLayout>(R.id.xp_bar)
        val level = xpBar.findViewById<GradientTextView>(R.id.cat_level)
        val xpBarProgress = xpBar.findViewById<ImageView>(R.id.progressBarProgress)
        val progressbarContainer = xpBar.findViewById<FrameLayout>(R.id.progressBarContainer)
        val progressBarContent = xpBar.findViewById<ConstraintLayout>(R.id.progressBarContent)
        val background = view.background

        // Setting cat level
        if (UserManager.user != null)
        {
            val currentUser = UserManager.user!!
            level.text = currentUser.level.toString()

            progressbarContainer.post {
                val containerWidth = progressBarContent.width
                val endCapWidthPx = dpToPx(20f) // Convert 20dp to pixels
                val availableWidth = containerWidth - endCapWidthPx

                // Get progress percentage (value between 0 and 1)
                val calculator = GamifiedStatsManager(requireContext())
                val progressPercentage = calculator.getLevelPercentageComplete(currentUser) // E.g., 0.75 for 75%

                val newWidth = (availableWidth * progressPercentage).toInt()

                // Update the width of progressBarProgress
                val params = xpBarProgress.layoutParams
                if (newWidth > 0) {
                    params.width = newWidth
                    xpBarProgress.layoutParams = params
                }
                else {
                    params.width = 1
                    xpBarProgress.layoutParams = params
                }
            }
        }

        // Navigation for settings
        view.findViewById<ImageButton>(R.id.cat_settings_button).setOnClickListener {
            navigateTo(requireContext(), SettingsActivity::class.java, null)
        }

        // Navigation for shop
        view.findViewById<ImageButton>(R.id.cat_shop_button).setOnClickListener {
            navigateTo(requireContext(), ShopChoiceActivity::class.java, null)
        }

        // Cat Avatar setup
        val catAvatar = view.findViewById<ImageView>(R.id.cat_fragment_avatar)
        try
        {
            val drawable = ContextCompat.getDrawable(requireContext(), GetCatDrawableId())
            catAvatar.setImageDrawable(drawable)
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Failed to set cat avatar", e)
        }

        return view
    }

     fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }
}
