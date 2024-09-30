package za.co.varsitycollege.st10204902.purrsonaltrainer.backend

import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserWorkout

/**
 * A class that provides various methods to calculate totals for sets, reps, and weight
 * from a collection of user workouts.
 *
 * @property usersWorkouts A map of user workouts keyed by workout ID.
 */
class WorkoutWorker(
    val usersWorkouts: Map<String, UserWorkout>
) {

    /**
     * Gets the maximum number of reps performed for a specific exercise across all workouts.
     *
     * @param exerciseID The ID of the exercise.
     * @return The maximum number of reps.
     */
    fun getMaxRepsPerExercise(exerciseID: String): Int {
        var maxReps = 0
        for (workout in usersWorkouts) {
            for (exercise in workout.value.workoutExercises.values) {
                if (exercise.exerciseID == exerciseID) {
                    for (set in exercise.sets.values) {
                        if ((set.reps ?: 0) > maxReps) {
                            maxReps = set.reps ?: 0
                        }
                    }
                }
            }
        }
        return maxReps
    }

    /**
     * Calculates the one-rep max (1RM) for each rep range for a specific exercise.
     *
     * @param exerciseID The ID of the exercise.
     * @return A map where the key is the number of reps and the value is the weight lifted.
     */
    fun calculateRMsPerExercise(exerciseID: String): Map<Int, Int> {
        val exerciseRMs = mutableMapOf<Int, Int>()

        val maxReps = getMaxRepsPerExercise(exerciseID)
        for (i in 1..maxReps) {
            exerciseRMs[i] = 0
        }

        for (workout in usersWorkouts) {
            for (exercise in workout.value.workoutExercises.values) {
                if (exercise.exerciseID == exerciseID) {
                    for (set in exercise.sets.values) {
                        if (set.reps != null && set.weight != null) {
                            if (exerciseRMs.containsKey(set.reps)) {
                                if (exerciseRMs[set.reps]!! < set.weight!!) {
                                    exerciseRMs[set.reps!!] = set.weight!!
                                }
                            } else {
                                exerciseRMs[set.reps!!] = set.weight!!
                            }
                        }
                    }
                }
            }
        }
        return exerciseRMs
    }

    /**
     * Calculates the total number of sets across all workouts.
     *
     * @return The total number of sets.
     */
    fun getTotalSets(): Int {
        var totalSets = 0
        for (workout in usersWorkouts) {
            for (exercise in workout.value.workoutExercises.values) {
                totalSets += exercise.sets.count()
            }
        }
        return totalSets
    }

    /**
     * Calculates the total number of reps across all workouts.
     *
     * @return The total number of reps.
     */
    fun getTotalReps(): Int {
        var totalReps = 0
        for (workout in usersWorkouts) {
            for (exercise in workout.value.workoutExercises.values) {
                for (set in exercise.sets.values) {
                    totalReps += set.reps ?: 0
                }
            }
        }
        return totalReps
    }

    /**
     * Calculates the total weight lifted across all workouts.
     *
     * @return The total weight lifted.
     */
    fun getTotalWeight(): Int {
        var totalWeight = 0
        for (workout in usersWorkouts) {
            for (exercise in workout.value.workoutExercises.values) {
                for (set in exercise.sets.values) {
                    totalWeight += set.weight ?: 0
                }
            }
        }
        return totalWeight
    }

    /**
     * Calculates the total number of sets for a specific exercise across all workouts.
     *
     * @param exerciseID The ID of the exercise.
     * @return The total number of sets for the specified exercise.
     */
    fun getTotalSetsPerExercise(exerciseID: String): Int {
        var totalSets = 0
        for (workout in usersWorkouts) {
            for (exercise in workout.value.workoutExercises.values) {
                if (exercise.exerciseID == exerciseID) {
                    totalSets += exercise.sets.count()
                }
            }
        }
        return totalSets
    }

    /**
     * Calculates the total number of reps for a specific exercise across all workouts.
     *
     * @param exerciseID The ID of the exercise.
     * @return The total number of reps for the specified exercise.
     */
    fun getTotalRepsPerExercise(exerciseID: String): Int {
        var totalReps = 0
        for (workout in usersWorkouts) {
            for (exercise in workout.value.workoutExercises.values) {
                if (exercise.exerciseID == exerciseID) {
                    for (set in exercise.sets.values) {
                        totalReps += set.reps ?: 0
                    }
                }
            }
        }
        return totalReps
    }

    /**
     * Calculates the total weight lifted for a specific exercise across all workouts.
     *
     * @param exerciseID The ID of the exercise.
     * @return The total weight lifted for the specified exercise.
     */
    fun getTotalWeightPerExercise(exerciseID: String): Int {
        var totalWeight = 0
        for (workout in usersWorkouts) {
            for (exercise in workout.value.workoutExercises.values) {
                if (exercise.exerciseID == exerciseID) {
                    for (set in exercise.sets.values) {
                        totalWeight += set.weight ?: 0
                    }
                }
            }
        }
        return totalWeight
    }

    /**
     * Calculates the total number of sets for a specific workout.
     *
     * @param workoutID The ID of the workout.
     * @return The total number of sets for the specified workout.
     */
    fun getTotalSetsPerWorkout(workoutID: String): Int {
        var totalSets = 0
        for (workout in usersWorkouts) {
            if (workout.key == workoutID) {
                for (exercise in workout.value.workoutExercises.values) {
                    totalSets += exercise.sets.count()
                }
            }
        }
        return totalSets
    }

    /**
     * Calculates the total number of reps for a specific workout.
     *
     * @param workoutID The ID of the workout.
     * @return The total number of reps for the specified workout.
     */
    fun getTotalRepsPerWorkout(workoutID: String): Int {
        var totalReps = 0
        for (workout in usersWorkouts) {
            if (workout.key == workoutID) {
                for (exercise in workout.value.workoutExercises.values) {
                    for (set in exercise.sets.values) {
                        totalReps += set.reps ?: 0
                    }
                }
            }
        }
        return totalReps
    }

    /**
     * Calculates the total weight lifted for a specific workout.
     *
     * @param workoutID The ID of the workout.
     * @return The total weight lifted for the specified workout.
     */
    fun getTotalWeightPerWorkout(workoutID: String): Int {
        var totalWeight = 0
        for (workout in usersWorkouts) {
            if (workout.key == workoutID) {
                for (exercise in workout.value.workoutExercises.values) {
                    for (set in exercise.sets.values) {
                        totalWeight += set.weight ?: 0
                    }
                }
            }
        }
        return totalWeight
    }

    /**
     * Calculates the total number of sets for a specific exercise within a specific workout.
     *
     * @param workoutID The ID of the workout.
     * @param exerciseID The ID of the exercise.
     * @return The total number of sets for the specified exercise within the specified workout.
     */
    fun getTotalSetsPerWorkoutPerExercise(workoutID: String, exerciseID: String): Int {
        var totalSets = 0
        for (workout in usersWorkouts) {
            if (workout.key == workoutID) {
                for (exercise in workout.value.workoutExercises.values) {
                    if (exercise.exerciseID == exerciseID) {
                        totalSets += exercise.sets.count()
                    }
                }
            }
        }
        return totalSets
    }

    /**
     * Calculates the total number of reps for a specific exercise within a specific workout.
     *
     * @param workoutID The ID of the workout.
     * @param exerciseID The ID of the exercise.
     * @return The total number of reps for the specified exercise within the specified workout.
     */
    fun getTotalRepsPerWorkoutPerExercise(workoutID: String, exerciseID: String): Int {
        var totalReps = 0
        for (workout in usersWorkouts) {
            if (workout.key == workoutID) {
                for (exercise in workout.value.workoutExercises.values) {
                    if (exercise.exerciseID == exerciseID) {
                        for (set in exercise.sets.values) {
                            totalReps += set.reps ?: 0
                        }
                    }
                }
            }
        }
        return totalReps
    }

    /**
     * Calculates the total weight lifted for a specific exercise within a specific workout.
     *
     * @param workoutID The ID of the workout.
     * @param exerciseID The ID of the exercise.
     * @return The total weight lifted for the specified exercise within the specified workout.
     */
    fun getTotalWeightPerWorkoutPerExercise(workoutID: String, exerciseID: String): Int {
        var totalWeight = 0
        for (workout in usersWorkouts) {
            if (workout.key == workoutID) {
                for (exercise in workout.value.workoutExercises.values) {
                    if (exercise.exerciseID == exerciseID) {
                        for (set in exercise.sets.values) {
                            totalWeight += set.weight ?: 0
                        }
                    }
                }
            }
        }
        return totalWeight
    }
}