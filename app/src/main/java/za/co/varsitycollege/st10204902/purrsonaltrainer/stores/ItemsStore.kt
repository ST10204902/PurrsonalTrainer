package za.co.varsitycollege.st10204902.purrsonaltrainer.stores

import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Item

class ItemsStore {
    companion object {
        val globalItems = listOf(
            Item("0", "Nick's Creatine", "Adds a XP bonus for all powerlifting moves done during a workout", 12, "placeholder_creatine"),
            Item("1", "Michael's Chalk", "Adds increases the XP reward for all exercises that target the lats, traps or forearms", 12, "michael_item"),
            Item("2", "Harvey's Cookies", "Adds a XP bonus for all powerlifting moves done during a workout", 12, "placeholder_cookie"),
            Item("3", "Jasper's Hoodie", "Adds a XP bonus for all powerlifting moves done during a workout", 12, "placeholder_hoodie"),
            Item("4", "Anneme's Plumbob", "permanently multiplies all XP received by 2.4", 12, "placeholder_plumbob")
        )
    }
}
