package za.co.varsitycollege.st10204902.purrsonaltrainer.services

import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserWorkout
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutExercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.stores.ItemsStore

/**
 * Class responsible for calculating XP for workouts.
 */
class WorkoutXPCalculator {
    val globalItems = ItemsStore.globalItems

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
            return 0
        }
        val currentUser = UserManager.user!!
        var bonusXP = 0
        //switch statement
        when (currentUser.equipedItem) {
            "0" -> {
                for ((_, exercise) in userWorkout.workoutExercises) {
                    if (exercise.exerciseName.contains("deadlift",true) || exercise.exerciseName.contains("squat",true)  || exercise.exerciseName.contains("Bench Press",true) ) {
                        bonusXP += getXPPerSet(exercise)
                    }
                }
            }
            "1" -> {
                for ((_, exercise) in userWorkout.workoutExercises) {
                    if (exercise.category == "lats" || exercise.category == "traps" || exercise.category == "forearms") {
                        bonusXP += (getXPPerSet(exercise) * 0.2).toInt()
                    }
                }
            }
            "2" -> {
                var bonusMilkCoins = 1
                for ((_, exercise) in userWorkout.workoutExercises) {
                        bonusXP += getXPPerSet(exercise)
                        bonusMilkCoins++
                }
                UserManager.updateMilkCoins(currentUser.milkCoins + bonusMilkCoins)
                bonusXP /= 2
                bonusXP *= -1
            }
            "3" -> {
                // check if the workout happened between 12am-8am
                if (userWorkout.date.time % 86400000 in 0..28800000) {
                    for ((_, exercise) in userWorkout.workoutExercises) {
                            bonusXP += getXPPerSet(exercise)
                    }
                   val temp = bonusXP
                    bonusXP = (temp / 10).toInt()
                }

            }
            "4" -> {
                for ((_, exercise) in userWorkout.workoutExercises) {
                    bonusXP += getXPPerSet(exercise)
                }
                bonusXP = (bonusXP * 1.4).toInt()
            }
        }
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
            totalXP += getXPPerSet(exercise)
        }
        val bonusXP = calculateItemBonusXP(userWorkout)
        totalXP += bonusXP
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
        for (set in exercise.sets.values) {
            if (set.weight != null && set.reps != null) {
                setXP += (set.weight!! * set.reps!! * muscleWeighting).toInt()
            }
        }
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
        }
        println(xpRequirements)
        return xpRequirements
    }

    /**
     * Updates the user's level and remaining XP after earning XP from a workout.
     *
     * @param totalXP The total XP earned from the workout.
     */
    fun updateUserLevelAndXP(totalXP: Int) {
        val user = UserManager.user!!
        val xpRequirements = calculateXPRequirements()
        var currentXP = user.experiencePoints + totalXP
        var currentLevel = user.level
        var levelGainMilkCoins = 0
        // user gets 1 coin just for submitting a workout
        levelGainMilkCoins++

        // Level up while current XP exceeds XP required for the next level
        while (currentLevel < totalLevels && currentXP >= xpRequirements.getOrNull(currentLevel - 1) ?: Int.MAX_VALUE) {
            currentXP -= xpRequirements.getOrNull(currentLevel - 1) ?: 0
            currentLevel++
            levelGainMilkCoins++
        }

        // Cap at max level
        if (currentLevel >= totalLevels) {
            currentLevel = totalLevels
            currentXP = 0 // No more XP needed after max level
        }

        UserManager.updateExperiencePoints(currentXP)
        UserManager.updateLevel(currentLevel)
        UserManager.updateMilkCoins(user.milkCoins + levelGainMilkCoins)
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

        // Handle max level scenario
        if (currentLevel >= totalLevels) {
            return 1.0
        }

        val xpNeededForNextLevel = xpRequirements.getOrNull(currentLevel - 1) ?: Int.MAX_VALUE

        // Calculate progress percentage
        val progressPercentage = if (xpNeededForNextLevel > 0) {
            println("currentXP: $currentXP, xpNeededForNextLevel: $xpNeededForNextLevel")
            currentXP.toDouble() / xpNeededForNextLevel.toDouble()
        } else {
            0.0
        }
println("progressPercentage: $progressPercentage")
        // Cap the progress percentage at 1.0
        return minOf(progressPercentage, 1.0)
    }
}

