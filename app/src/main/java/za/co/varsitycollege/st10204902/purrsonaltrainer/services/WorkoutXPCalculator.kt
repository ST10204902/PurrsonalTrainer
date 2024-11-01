package za.co.varsitycollege.st10204902.purrsonaltrainer.services

import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.User
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserWorkout
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutExercise

/**
 * Class responsible for calculating XP for workouts.
 */
class WorkoutXPCalculator {

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

    private var currentLevel = 1

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
            if (set.weight != null || set.reps != null) {
                setXP += (set.weight!! * set.reps!! * muscleWeighting).toInt()
            }
        }
        return setXP
    }

    /**
     * Calculates the XP requirements for each level.
     *
     * @return A list of XP requirements for each level.
     */
    private fun calculateXPRequirements(): List<Int> {
        val xpRequirements = mutableListOf(0) // Level 1 starts at 0 XP
        for (level in 2..totalLevels) {
            val milestoneIndex = (level - 1) / 10
            val scalingFactor = scalingFactors[minOf(milestoneIndex, scalingFactors.size - 1)]
            val previousXP = xpRequirements.last()
            val newXP = previousXP + (baseXP * scalingFactor).toInt()
            xpRequirements.add(newXP)
        }
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
        var accumulatedXP = user.experiencePoints + totalXP
        UserManager.updateExperiencePoints(accumulatedXP)
        // Determine the new level and remaining XP
        while (currentLevel < totalLevels && accumulatedXP >= xpRequirements[currentLevel]) {
            accumulatedXP -= xpRequirements[currentLevel]
            currentLevel++
        }
        UserManager.updateLevel(currentLevel)
    }

    fun getLevelPercentageComplete(): Double {
        val user = UserManager.user!!
        val xpRequirements = calculateXPRequirements()
        val currentXP = user.experiencePoints
        val nextLevelXP = xpRequirements[currentLevel]
        val previousLevelXP = xpRequirements[currentLevel - 1]
        val levelXPRange = nextLevelXP - previousLevelXP
        val currentLevelXP = currentXP - previousLevelXP
        return (currentLevelXP.toDouble() / levelXPRange)
    }
}