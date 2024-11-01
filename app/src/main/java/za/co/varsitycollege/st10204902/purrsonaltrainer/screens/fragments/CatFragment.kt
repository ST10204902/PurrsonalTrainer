package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.SettingsActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.shop.ShopChoiceActivity
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
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cat, container, false)

        // Navigation for settings
        view.findViewById<Button>(R.id.cat_settings_button).setOnClickListener {
            navigateTo(requireContext(), SettingsActivity::class.java, null)
        }

        // Navigation for shop
        view.findViewById<Button>(R.id.cat_shop_button).setOnClickListener {
            navigateTo(requireContext(), ShopChoiceActivity::class.java, null)
        }

        return view
    }
}