package za.co.varsitycollege.st10204902.purrsonaltrainer.services

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
    private lateinit var userWorkout: UserWorkout

    /**
     * Calculates the amount of XP a user earns from a workout.
     *
     * @param userWorkout The workout details of the user.
     * @return The calculated XP as an integer.
     */
    fun calculateXP(userWorkout: UserWorkout): Int {
        this.userWorkout = userWorkout
        var totalXP = 0

        for ((_, exercise) in userWorkout.workoutExercises) {
            val setXP = getXPPerExercise(exercise)
            totalXP += setXP
        }

        return totalXP
    }

    /**
     * Calculates the XP for a single set of an exercise.
     *
     * @param exercise The exercise details.
     * @return The calculated XP for the set.
     */
    private fun getXPPerExercise(exercise: WorkoutExercise): Int {
        val muscleWeighting =
            muscleGroupWeightings.getOrDefault(exercise.category, defaultWeighting)
        var totalXP = 0
        for ((_, set) in exercise.sets) {

            if (set.weight == null || set.weight == 0)
            {
                set.weight = userWorkout.bodyWeight
            }
            if (set.completed) {
                totalXP += (set.weight!! * set.reps!! * muscleWeighting).toInt()
            }
            }
        return totalXP
        }
    }