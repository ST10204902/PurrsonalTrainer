package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments

import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.frontend_logic.GradientTextView
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.SettingsActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.shop.ShopChoiceActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.WorkoutXPCalculator
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo

/**
 * A simple [Fragment] subclass.
 * Use the [CatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CatFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    { // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cat, container, false)

        // Setup xp bar and level
        val xpBar = view.findViewById<LinearLayout>(R.id.xp_bar)
        val level = xpBar.findViewById<GradientTextView>(R.id.cat_level)
        var xpBarProgress = xpBar.findViewById<ImageView>(R.id.progressBarProgress)
        val progressbarContainer = xpBar.findViewById<FrameLayout>(R.id.progressBarContainer)
        val progressBarContent = xpBar.findViewById<ConstraintLayout>(R.id.progressBarContent)

        // Setting cat level
        if (UserManager.user != null)
        {
            level.text = UserManager.user!!.level.toString()

            progressbarContainer.post {
                val containerWidth = progressBarContent.width
                val endCapWidthPx = dpToPx(20f) // Convert 20dp to pixels
                val availableWidth = containerWidth - endCapWidthPx

                // Get progress percentage (value between 0 and 1)
                val calculator = WorkoutXPCalculator()
                val progressPercentage = calculator.getLevelPercentageComplete() // E.g., 0.75 for 75%

                val newWidth = (availableWidth * progressPercentage).toInt()

                // Update the width of progressBarProgress
                val params = xpBarProgress.layoutParams
                params.width = newWidth
                xpBarProgress.layoutParams = params
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

        return view
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }
}