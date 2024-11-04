package za.co.varsitycollege.st10204902.purrsonaltrainer.stores

import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserBackground

class BackgroundStore {
    companion object {
        val globalBackgrounds = listOf(
            UserBackground("0", "Baggies Bottom",10, "shop_background_1"),
            UserBackground("1", "Clear Skies",  14, "shop_background_2"),
            UserBackground("2", "City Skyline",  16, "shop_background_3"),
            UserBackground("3", "Alien World",  25, "shop_background_4")
        )
    }
}