package za.co.varsitycollege.st10204902.purrsonaltrainer.services

import android.util.Log
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.CreateID
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Exercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserRoutine
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutExercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutSet
import java.util.Date

interface ExerciseAddedListener {
    fun onExerciseAdded()
}

/**
 * Builder class for creating a routine
 */
class RoutineBuilder {
    //-----------------------------------------------------------//
    //                          PROPERTIES                       //
    //-----------------------------------------------------------//

    /**
     * The routineID of the routine being built
     */
    var routineID: String = CreateID.GenerateID()

    /**
     * The name of the routine being built
     */
    var name: String = ""

    /**
     * The description of the routine being built
     */
    var description: String = ""

    /**
     * The exercises in the routine being built
     */
    var exercises: MutableMap<String, WorkoutExercise> = mutableMapOf()

    /**
     * The color of the routine being built
     */
    var color: String = ""

    /**
     * List of subscribers to be notified when an exercise is added
     */
    private val exerciseAddedListeners = mutableListOf<ExerciseAddedListener>()

    /**
     * Tag for logging
     */
    private val TAG = "RoutineBuilder"

    //-----------------------------------------------------------//
    //                          METHODS                          //
    //-----------------------------------------------------------//

    /**
     * Sets the name of the routine being built
     * @param name The name of the routine
     */
    fun setRoutineName(name: String) {
        this.name = name
        Log.d(TAG, "Routine name set to: $name")
    }

    /**
     * Sets the color of the routine being built
     * @param color The color of the routine
     */
    fun setRoutineColor(color: String) {
        this.color = color
        Log.d(TAG, "Routine color set to: $color")
    }

    /**
     * Sets the description of the routine being built
     * @param description The description of the routine
     */
    fun setRoutineDescription(description: String) {
        this.description = description
        Log.d(TAG, "Routine description set to: $description")
    }

    /**
     * Checks if the routine has an exercise
     * @return Boolean - True if the routine has an exercise, false if not
     */
    fun hasAnExercise(): Boolean {
        val hasExercise = exercises.isNotEmpty()
        Log.d(TAG, "Checking if routine has exercises: $hasExercise")
        return hasExercise
    }

    /**
     * Adds an exercise to the routine being built
     * @param exercise The exercise to add
     */
    fun addExercise(exercise: Exercise) {
        try {
            if (!exercises.containsKey(exercise.exerciseID)) {
                val tempExerciseID = exercise.exerciseID
                val tempExerciseName = exercise.exerciseName
                val tempCategory = exercise.category
                var tempNotes = exercise.notes
                if (!exercise.isCustom) {
                    tempNotes = ""
                }
                val tempMeasurementType = exercise.measurementType
                val tempSets = mapOf<String, WorkoutSet>()
                val workoutExercise = WorkoutExercise(
                    tempExerciseID,
                    tempExerciseName,
                    tempCategory,
                    tempSets,
                    Date(),
                    tempNotes,
                    tempMeasurementType
                )
                exercises[tempExerciseID] = workoutExercise
                Log.d(TAG, "Added exercise: $tempExerciseName (ID: $tempExerciseID)")

                // Notifying subscribers that an exercise has been added
                notifyExerciseAdded()
            } else {
                Log.w(TAG, "Exercise already exists in routine: ${exercise.exerciseID}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding exercise: ${e.message}", e)
        }
    }

    /**
     * Adds a workout exercise to the routine
     * @param workoutExercise The workout exercise to add
     */
    fun addWorkoutExercise(workoutExercise: WorkoutExercise) {
        try {
            val tempExerciseID = workoutExercise.exerciseID
            exercises[tempExerciseID] = workoutExercise
            Log.d(TAG, "Added workout exercise: ${workoutExercise.exerciseName} (ID: $tempExerciseID)")

            // Notifying subscribers that an exercise has been added
            notifyExerciseAdded()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding workout exercise: ${e.message}", e)
        }
    }

    /**
     * Converts an Exercise to a WorkoutExercise
     * @param exercise The exercise to convert
     * @return WorkoutExercise The converted workout exercise
     */
    fun convertToWorkoutExercise(exercise: Exercise): WorkoutExercise {
        return try {
            val tempExerciseID = exercise.exerciseID
            val tempExerciseName = exercise.exerciseName
            val tempCategory = exercise.category
            var tempNotes = exercise.notes
            if (!exercise.isCustom) {
                tempNotes = ""
            }
            val tempSets = mapOf<String, WorkoutSet>()
            val workoutExercise = WorkoutExercise(
                tempExerciseID,
                tempExerciseName,
                tempCategory,
                tempSets,
                Date(),
                tempNotes,
                exercise.measurementType
            )
            Log.d(TAG, "Converted exercise to workout exercise: $tempExerciseName (ID: $tempExerciseID)")
            workoutExercise
        } catch (e: Exception) {
            Log.e(TAG, "Error converting exercise: ${e.message}", e)
            throw e
        }
    }

    /**
     * Builds the routine and resets the builder
     * @return UserRoutine - The routine that was built
     */
    fun buildRoutine(): UserRoutine {
        return try {
            if (name.isBlank()) {
                Log.w(TAG, "Routine name is blank")
            }
            if (color.isBlank()) {
                Log.w(TAG, "Routine color is not set")
            }
            if (exercises.isEmpty()) {
                Log.w(TAG, "Routine has no exercises")
            }
            val newRoutine = UserRoutine(routineID, name, color, description, exercises)
            Log.d(TAG, "Built routine: $name (ID: $routineID) with ${exercises.size} exercises")
            clearRoutine()
            newRoutine
        } catch (e: Exception) {
            Log.e(TAG, "Error building routine: ${e.message}", e)
            throw e
        }
    }

    /**
     * Clears the current routine data
     */
    fun clearRoutine() {
        routineID = CreateID.GenerateID()
        name = ""
        description = ""
        exercises = mutableMapOf()
        color = ""
        Log.d(TAG, "Routine cleared and reset")
    }

    // Exercise Added Methods
    //-----------------------------------------------------------//

    /**
     * Adds a listener to the exerciseAddedListeners
     * @param listener The listener to add
     */
    fun addExerciseAddedListener(listener: ExerciseAddedListener) {
        exerciseAddedListeners.clear()
        exerciseAddedListeners.add(listener)
        Log.d(TAG, "ExerciseAddedListener added and previous listeners cleared")
    }

    /**
     * Notifies subscribers that an exercise has been added
     */
    private fun notifyExerciseAdded() {
        try {
            for (listener in exerciseAddedListeners) {
                listener.onExerciseAdded()
            }
            Log.d(TAG, "Notified ${exerciseAddedListeners.size} ExerciseAddedListeners")
        } catch (e: Exception) {
            Log.e(TAG, "Error notifying listeners: ${e.message}", e)
        }
    }
}
