package za.co.varsitycollege.st10204902.purrsonaltrainer.services

import android.util.Log
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserWorkout
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutExercise


/**
 * Class responsible for calculating XP for workouts.
 */
class WorkoutXPCalculator {

    companion object {
        private const val TAG = "WorkoutXPCalculator"
    }

    // Weighting for each muscle group
    private val muscleGroupWeightings = mapOf(
        "glutes" to 1.0,
        "middle back" to 1.0,
        "lower back" to 1.0,
        "quads" to 1.1,
        "hamstrings" to 1.2,
        "chest" to 1.3,
        "lats" to 1.5,
        "traps" to 1.5,
        "calves" to 1.5,
        "adductors" to 1.7,
        "abductors" to 1.7,
        "shoulders" to 1.7,
        "triceps" to 1.7,
        "biceps" to 1.9,
        "abs" to 2.0,
        "forearms" to 2.0,
        "neck" to 2.0
    )

    private val defaultWeighting = 1.0

    // Scaling factors per milestone
    private val scalingFactors = listOf(1.0, 1.3, 1.5, 1.8, 2.0, 2.5, 3.0, 4.0)
    private val baseXP = 7000
    private val totalLevels = 69

    fun calculateItemBonusXP(userWorkout: UserWorkout): Int {
        if (UserManager.user == null) {
            Log.d(TAG, "UserManager.user is null. Returning 0 bonus XP.")
            return 0
        }
        val currentUser = UserManager.user!!
        var bonusXP = 0
        Log.d(TAG, "Calculating item bonus XP for equipped item: ${currentUser.equipedItem}")
        when (currentUser.equipedItem) {
            "0" -> {
                for ((_, exercise) in userWorkout.workoutExercises) {
                    if (exercise.exerciseName.contains("deadlift", true) || exercise.exerciseName.contains("squat", true) || exercise.exerciseName.contains("Bench Press", true)) {
                        val xpPerSet = getXPPerSet(exercise)
                        bonusXP += xpPerSet
                        Log.d(TAG, "Item 0: Added XP ${xpPerSet} for exercise ${exercise.exerciseName}")
                    }
                }
            }
            "1" -> {
                for ((_, exercise) in userWorkout.workoutExercises) {
                    if (exercise.category == "lats" || exercise.category == "traps" || exercise.category == "forearms") {
                        val xpPerSet = (getXPPerSet(exercise) * 0.2).toInt()
                        bonusXP += xpPerSet
                        Log.d(TAG, "Item 1: Added XP ${xpPerSet} for exercise ${exercise.exerciseName}")
                    }
                }
            }
            "2" -> {
                var bonusMilkCoins = 1
                for ((_, exercise) in userWorkout.workoutExercises) {
                    val xpPerSet = getXPPerSet(exercise)
                    bonusXP += xpPerSet
                    bonusMilkCoins++
                    Log.d(TAG, "Item 2: Added XP ${xpPerSet} and incremented milk coins to ${bonusMilkCoins} for exercise ${exercise.exerciseName}")
                }
                UserManager.updateMilkCoins(currentUser.milkCoins + bonusMilkCoins)
                Log.d(TAG, "Item 2: Updated user's milk coins to ${currentUser.milkCoins + bonusMilkCoins}")
                bonusXP /= 2
                bonusXP *= -1
                Log.d(TAG, "Item 2: Final bonus XP after division and negation: ${bonusXP}")
            }
            "3" -> {
                // check if the workout happened between 12am-8am
                val timeOfDay = userWorkout.date.time % 86400000 // milliseconds in a day
                if (timeOfDay in 0..28800000) {
                    for ((_, exercise) in userWorkout.workoutExercises) {
                        val xpPerSet = getXPPerSet(exercise)
                        bonusXP += xpPerSet
                        Log.d(TAG, "Item 3: Added XP ${xpPerSet} for exercise ${exercise.exerciseName}")
                    }
                    bonusXP = (bonusXP / 10).toInt()
                    Log.d(TAG, "Item 3: Final bonus XP after division: ${bonusXP}")
                } else {
                    Log.d(TAG, "Item 3: Workout not between 12am-8am. No bonus XP.")
                }
            }
            "4" -> {
                for ((_, exercise) in userWorkout.workoutExercises) {
                    val xpPerSet = getXPPerSet(exercise)
                    bonusXP += xpPerSet
                    Log.d(TAG, "Item 4: Added XP ${xpPerSet} for exercise ${exercise.exerciseName}")
                }
                bonusXP = (bonusXP * 1.4).toInt()
                Log.d(TAG, "Item 4: Final bonus XP after multiplication: ${bonusXP}")
            }
            else -> {
                Log.d(TAG, "No equipped item or unrecognized item ID.")
            }
        }
        Log.d(TAG, "Total bonus XP calculated: ${bonusXP}")
        return bonusXP
    }

    /**
     * Calculates the amount of XP a user earns from a workout.
     *
     * @param userWorkout The workout details of the user.
     * @return The calculated XP as an integer.
     */
    fun calculateXP(userWorkout: UserWorkout): Int {
        var totalXP = 0
        for ((_, exercise) in userWorkout.workoutExercises) {
            val xpPerSet = getXPPerSet(exercise)
            totalXP += xpPerSet
            Log.d(TAG, "Added XP ${xpPerSet} for exercise ${exercise.exerciseName}")
        }
        val bonusXP = calculateItemBonusXP(userWorkout)
        totalXP += bonusXP
        Log.d(TAG, "Total XP after adding bonus XP (${bonusXP}): ${totalXP}")
        return totalXP
    }

    /**
     * Calculates the XP for a single set of an exercise.
     *
     * @param exercise The exercise details.
     * @return The calculated XP for the set.
     */
    private fun getXPPerSet(exercise: WorkoutExercise): Int {
        val muscleWeighting = muscleGroupWeightings.getOrDefault(exercise.category, defaultWeighting)
        var setXP = 0
        Log.d(TAG, "Calculating XP for exercise: ${exercise.exerciseName}, category: ${exercise.category}, muscle weighting: ${muscleWeighting}")
        for (set in exercise.sets.values) {
            if (set.weight != null && set.reps != null) {
                val xpForSet = (set.weight!! * set.reps!! * muscleWeighting).toInt()
                setXP += xpForSet
                Log.d(TAG, "Set ID: ${set.workoutSetID}, weight: ${set.weight}, reps: ${set.reps}, XP for set: ${xpForSet}")
            } else {
                Log.d(TAG, "Set ID: ${set.workoutSetID} has null weight or reps. Skipping.")
            }
        }
        Log.d(TAG, "Total XP for exercise ${exercise.exerciseName}: ${setXP}")
        return setXP
    }

    /**
     * Calculates the XP requirements for each level.
     *
     * @return A list of XP requirements for each level (incremental XP).
     */
    private fun calculateXPRequirements(): List<Int> {
        val xpRequirements = mutableListOf<Int>() // XP required to go from level N to N+1
        for (level in 1 until totalLevels) {
            val milestoneIndex = (level - 1) / 10
            val scalingFactor = scalingFactors[minOf(milestoneIndex, scalingFactors.size - 1)]
            val levelXP = (baseXP * scalingFactor).toInt()
            xpRequirements.add(levelXP)
            Log.d(TAG, "Level $level: XP required to level up: $levelXP")
        }
        Log.d(TAG, "XP Requirements: $xpRequirements")
        return xpRequirements
    }

    /**
     * Updates the user's level and remaining XP after earning XP from a workout.
     *
     * @param totalXP The total XP earned from the workout.
     */
    fun updateUserLevelAndXP(totalXP: Int) {
        val user = UserManager.user!!
        Log.d(TAG, "Updating user level and XP. User initial XP: ${user.experiencePoints}, Level: ${user.level}, Total XP earned: $totalXP")
        val xpRequirements = calculateXPRequirements()
        var currentXP = user.experiencePoints + totalXP
        var currentLevel = user.level
        var levelGainMilkCoins = 1 // user gets 1 coin just for submitting a workout
        Log.d(TAG, "Initial currentXP: $currentXP, currentLevel: $currentLevel, levelGainMilkCoins: $levelGainMilkCoins")

        // Level up while current XP exceeds XP required for the next level
        while (currentLevel < totalLevels && currentXP >= xpRequirements.getOrNull(currentLevel - 1) ?: Int.MAX_VALUE) {
            val xpNeeded = xpRequirements.getOrNull(currentLevel - 1) ?: 0
            currentXP -= xpNeeded
            currentLevel++
            levelGainMilkCoins++
            Log.d(TAG, "Leveled up! New level: $currentLevel, Remaining XP: $currentXP, Milk coins gained: $levelGainMilkCoins")
        }

        // Cap at max level
        if (currentLevel >= totalLevels) {
            currentLevel = totalLevels
            currentXP = 0 // No more XP needed after max level
            Log.d(TAG, "Reached max level: $totalLevels. Current XP set to 0.")
        }

        UserManager.updateExperiencePoints(currentXP)
        UserManager.updateLevel(currentLevel)
        UserManager.updateMilkCoins(user.milkCoins + levelGainMilkCoins)
        Log.d(TAG, "Updated user XP: $currentXP, Level: $currentLevel, Total Milk Coins: ${user.milkCoins + levelGainMilkCoins}")
    }

    /**
     * Calculates the percentage of XP completed towards the next level.
     *
     * @return A value between 0.0 and 1.0 representing the progress to the next level.
     */
    fun getLevelPercentageComplete(): Double {
        val user = UserManager.user!!
        val xpRequirements = calculateXPRequirements()
        val currentXP = user.experiencePoints
        val currentLevel = user.level

        Log.d(TAG, "Calculating level percentage complete. User level: $currentLevel, current XP: $currentXP")

        // Handle max level scenario
        if (currentLevel >= totalLevels) {
            Log.d(TAG, "User is at max level. Returning 1.0")
            return 1.0
        }

        val xpNeededForNextLevel = xpRequirements.getOrNull(currentLevel - 1) ?: Int.MAX_VALUE
        Log.d(TAG, "XP needed for next level: $xpNeededForNextLevel")

        // Calculate progress percentage
        val progressPercentage = if (xpNeededForNextLevel > 0) {
            Log.d(TAG, "currentXP: $currentXP, xpNeededForNextLevel: $xpNeededForNextLevel")
            currentXP.toDouble() / xpNeededForNextLevel.toDouble()
        } else {
            0.0
        }
        Log.d(TAG, "progressPercentage: $progressPercentage")
        // Cap the progress percentage at 1.0
        return minOf(progressPercentage, 1.0)
    }
}
