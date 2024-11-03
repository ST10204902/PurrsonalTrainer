package za.co.varsitycollege.st10204902.purrsonaltrainer.services

import android.content.Context
import android.util.Log
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Item
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.User
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserWorkout
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutExercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutSet
import java.util.Calendar
import kotlin.math.pow


class GamifiedStatsManager(private val context: Context) {

    companion object {
        private const val TAG = "GamifiedStatsManager"
    }

    // Maximum level achievable
    val maxLevel = 69

    // Base XP required for level 1
    private val baseXPRequirement = 7000

    // XP scaling factor per level (adjust this to change leveling curve)
    private val xpScalingFactor = 1.1

    // Base XP per set (adjustable)
    private val baseXPPerSet = 10

    // Default muscle weighting
    private val defaultWeighting = 1.0

    // Muscle group weightings (localized)
    private val muscleGroupWeightings by lazy {
        mapOf(
            context.getString(R.string.muscle_group_glutes) to 1.0,
            context.getString(R.string.muscle_group_middle_back) to 1.0,
            context.getString(R.string.muscle_group_lower_back) to 1.0,
            context.getString(R.string.muscle_group_quads) to 1.1,
            context.getString(R.string.muscle_group_hamstrings) to 1.2,
            context.getString(R.string.muscle_group_chest) to 1.3,
            context.getString(R.string.muscle_group_lats) to 1.5,
            context.getString(R.string.muscle_group_traps) to 1.5,
            context.getString(R.string.muscle_group_calves) to 1.5,
            context.getString(R.string.muscle_group_adductors) to 1.7,
            context.getString(R.string.muscle_group_abductors) to 1.7,
            context.getString(R.string.muscle_group_shoulders) to 1.7,
            context.getString(R.string.muscle_group_triceps) to 1.7,
            context.getString(R.string.muscle_group_biceps) to 1.9,
            context.getString(R.string.muscle_group_abs) to 2.0,
            context.getString(R.string.muscle_group_forearms) to 2.0,
            context.getString(R.string.muscle_group_neck) to 2.0
        )
    }

    /**
     * Calculates the total XP required to reach a specific level.
     *
     * @param level The target level.
     * @return The total XP required to reach the target level.
     */
    fun getTotalXPForLevel(level: Int): Int {
        var totalXP = 0.0
        for (i in 1 until level) {
            totalXP += baseXPRequirement * xpScalingFactor.pow((i - 1).toDouble())
        }
        return totalXP.toInt()
    }

    /**
     * Calculates the XP earned for a single set.
     *
     * @param set The workout set.
     * @param muscleGroup The muscle group targeted.
     * @param itemEffect The effect of the equipped item on XP.
     * @return The XP earned for the set.
     */
    fun calculateXPForSet(set: WorkoutSet, muscleGroup: String, itemEffect: Double): Int {
        val muscleWeighting = muscleGroupWeightings.getOrDefault(muscleGroup, defaultWeighting)
        var xp = 0
        if (set.weight != null && set.reps != null && set.completed) {
            xp = (set.weight!! * set.reps!! * muscleWeighting * baseXPPerSet * itemEffect).toInt()
            Log.d(TAG, "Set ID: ${set.workoutSetID}, weight: ${set.weight}, reps: ${set.reps}, XP for set: $xp")
        } else {
            Log.d(TAG, "Set ID: ${set.workoutSetID} has null weight/reps or is not completed. Skipping.")
        }
        return xp
    }

    /**
     * Calculates the total XP earned for a workout.
     *
     * @param workout The user's workout.
     * @param equippedItem The item equipped by the user.
     * @param user The current user.
     * @return The total XP earned for the workout.
     */
    fun calculateTotalXPForWorkout(workout: UserWorkout, equippedItem: Item?, user: User): Int {
        var totalXP = 0
        workout.workoutExercises.values.forEach { exercise ->
            val muscleGroup = exercise.category
            val itemEffect = getItemXPModifier(equippedItem, user, workout, exercise)
            exercise.sets.values.forEach { set ->
                totalXP += calculateXPForSet(set, muscleGroup, itemEffect)
            }
        }
        Log.d(TAG, "Total XP for workout: $totalXP")
        return totalXP
    }

    /**
     * Calculates the total XP contributed by exercises targeting a specific muscle group.
     *
     * @param workout The user's workout.
     * @param muscleGroup The muscle group to filter exercises by.
     * @param equippedItem The item equipped by the user.
     * @param user The current user.
     * @return The total XP contributed by the specified muscle group.
     */
    private fun calculateXPForMuscleGroup(
        workout: UserWorkout,
        muscleGroup: String,
        equippedItem: Item?,
        user: User
    ): Int {
        var totalXP = 0
        workout.workoutExercises.values
            .filter { exercise -> exercise.category == muscleGroup }
            .forEach { exercise ->
                val itemEffect = getItemXPModifier(equippedItem, user, workout, exercise)
                exercise.sets.values.forEach { set ->
                    totalXP += calculateXPForSet(set, muscleGroup, itemEffect)
                }
            }
        Log.d(TAG, "Total XP for muscle group $muscleGroup: $totalXP")
        return totalXP
    }

    /**
     * Gets the XP modifier based on the equipped item.
     *
     * @param item The equipped item.
     * @param user The current user.
     * @param workout The workout being calculated.
     * @param exercise The current exercise.
     * @return The XP modifier as a multiplier.
     */
    private fun getItemXPModifier(
        item: Item?,
        user: User,
        workout: UserWorkout,
        exercise: WorkoutExercise
    ): Double {
        // Default effect is 1.0 (no change)
        var effect = 1.0

        when (item?.itemID) {
            "0" -> {
                // Nick's Creatine: Bonus XP for powerlifting moves
                val powerliftingMoves = listOf("deadlift", "squat", "bench press")
                if (powerliftingMoves.any { move ->
                        exercise.exerciseName.contains(move, ignoreCase = true)
                    }) {
                    effect = 1.2
                }
            }
            "1" -> {
                // Michael's Chalk: Bonus XP for lats, traps, forearms
                val affectedGroups = listOf(
                    context.getString(R.string.muscle_group_lats),
                    context.getString(R.string.muscle_group_traps),
                    context.getString(R.string.muscle_group_forearms)
                )
                if (exercise.category in affectedGroups) {
                    effect = 1.420
                }
            }
            "2" -> {
                // Harvey's Cookies: Extra milk coins, half XP
                effect = 0.5
            }
            "3" -> {
                // Jasper's Hoodie: Extra XP during specific hours
                val calendar = Calendar.getInstance()
                calendar.time = workout.date
                val workoutHour = calendar.get(Calendar.HOUR_OF_DAY)
                if (workoutHour in 0..7) {
                    effect = 1.2
                }
            }
            "4" -> {
                // Anneme's Plumbob: Double XP
                effect = 2.0
            }
            else -> {
                Log.d(TAG, "No equipped item or unrecognized item ID.")
            }
        }
        Log.d(TAG, "Item effect for item ${item?.itemID} on exercise ${exercise.exerciseName}. the the item ${item?.description}: $effect")
        return effect
    }

    /**
     * Updates the user's XP, level, and milk coins after completing a workout.
     *
     * @param user The user.
     * @param workout The workout completed.
     * @param equippedItem The item equipped by the user.
     * @return The updated user.
     */
    fun updateUserStatsAfterWorkout(user: User, workout: UserWorkout, equippedItem: Item?) {
        val totalXP = calculateTotalXPForWorkout(workout, equippedItem, user)
        val totalMilkCoins = calculateMilkCoinsForWorkout(workout, equippedItem, user.level)

        var newExperiencePoints = user.experiencePoints + totalXP
        var newLevel = user.level
        var levelUpMilkCoins = 0

        val xpRequirements = calculateXPRequirements()

        // Level up while current XP exceeds XP required for the next level
        while (newLevel < maxLevel && newExperiencePoints >= (xpRequirements.getOrNull(newLevel - 1)
                ?: Int.MAX_VALUE)
        ) {
            val xpNeeded = xpRequirements.getOrNull(newLevel - 1) ?: 0
            newExperiencePoints -= xpNeeded
            newLevel++
            levelUpMilkCoins++
            Log.d(TAG, "Leveled up! New level: $newLevel, Remaining XP: $newExperiencePoints, Milk coins gained: $levelUpMilkCoins")
        }

        // Cap at max level
        if (newLevel >= maxLevel) {
            newLevel = maxLevel
            newExperiencePoints = 0 // No more XP needed after max level
            Log.d(TAG, "Reached max level: $maxLevel. Current XP set to 0.")
        }

        val newMilkCoins = user.milkCoins + totalMilkCoins + levelUpMilkCoins

        // Update user stats
        UserManager.updateExperiencePoints(newExperiencePoints)
        UserManager.updateLevel(newLevel)
        UserManager.updateMilkCoins(newMilkCoins)


        Log.d(TAG, "Updated user XP: $newExperiencePoints, Level: $newLevel, Total Milk Coins: $newMilkCoins")


    }

    /**
     * Calculates the milk coins earned for a workout.
     *
     * @param workout The workout completed.
     * @param equippedItem The item equipped by the user.
     * @param userLevel The user's current level.
     * @return The milk coins earned.
     */
    private fun calculateMilkCoinsForWorkout(
        workout: UserWorkout,
        equippedItem: Item?,
        userLevel: Int
    ): Int {
        // Base milk coins per workout
        var milkCoins = 1 // User gets 1 coin just for submitting a workout

        // Scaling factor to ensure enough milk coins by a certain level
        val scalingFactor = (60.0 / (20 * averageWorkoutsPerLevel())).toInt()
        milkCoins *= scalingFactor

        // Modify based on item effects
        if (equippedItem?.itemID == "2") {
            // Harvey's Cookies: Extra milk coins
            milkCoins *= 2
            Log.d(TAG, "Equipped with Harvey's Cookies. Milk coins doubled to $milkCoins")
        }

        Log.d(TAG, "Total milk coins earned for workout: $milkCoins")
        return milkCoins
    }

    /**
     * Estimates the average number of workouts per level.
     * Adjust this value based on expected user behavior.
     */
    private fun averageWorkoutsPerLevel(): Int {
        return 3 // Assume 3 workouts per level on average
    }

    /**
     * Calculates the XP requirements for each level.
     *
     * @return A list of XP requirements for each level (incremental XP).
     */
    private fun calculateXPRequirements(): List<Int> {
        val xpRequirements = mutableListOf<Int>() // XP required to go from level N to N+1
        for (level in 1 until maxLevel) {
            val xpForLevel = (baseXPRequirement * Math.pow(xpScalingFactor, (level - 1).toDouble())).toInt()
            xpRequirements.add(xpForLevel)
            Log.d(TAG, "Level $level: XP required to level up: $xpForLevel")
        }
        return xpRequirements
    }

    /**
     * Calculates the percentage of XP completed towards the next level.
     *
     * @param user The user.
     * @return A value between 0.0 and 1.0 representing the progress to the next level.
     */
    fun getLevelPercentageComplete(user: User): Double {
        val xpRequirements = calculateXPRequirements()
        val currentXP = user.experiencePoints
        val currentLevel = user.level

        Log.d(TAG, "Calculating level percentage complete. User level: $currentLevel, current XP: $currentXP")

        // Handle max level scenario
        if (currentLevel >= maxLevel) {
            Log.d(TAG, "User is at max level. Returning 1.0")
            return 1.0
        }

        val xpNeededForNextLevel = xpRequirements.getOrNull(currentLevel - 1) ?: Int.MAX_VALUE
        Log.d(TAG, "XP needed for next level: $xpNeededForNextLevel")

        // Calculate progress percentage
        val progressPercentage = if (xpNeededForNextLevel > 0) {
            currentXP.toDouble() / xpNeededForNextLevel.toDouble()
        } else {
            0.0
        }

        Log.d(TAG, "Progress percentage: $progressPercentage")
        // Cap the progress percentage at 1.0
        return minOf(progressPercentage, 1.0)
    }
}
