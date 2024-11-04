package za.co.varsitycollege.st10204902.purrsonaltrainer.stores

import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Item

class ItemsStore {
    companion object {
        val globalItems = listOf(
            Item("0", "Nick's Creatine", "Adds a XP bonus for all powerlifting moves done during a workout", 14, "placeholder_creatine"),
            Item("1", "Michael's Chalk", "Adds increases the XP reward for all exercises that target the lats, traps or forearms", 12, "michael_item"),
            Item("2", "Harvey's Cookies", "Gives extra milk coins but halves your XP reward", 16, "placeholder_cookie"),
            Item("3", "Jasper's Hoodie", "Gives 10% more xp for workouts completed between midnight and 8am", 8, "placeholder_hoodie"),
            Item("4", "Anneme's Plumbob", "When equipped the plumbob multiplies all XP received by 2.4, you must complete at 50 workouts in order to purchase this item", 19, "placeholder_plumbob")
        )
    }

}