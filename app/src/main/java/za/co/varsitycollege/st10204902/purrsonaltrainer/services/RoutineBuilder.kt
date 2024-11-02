package za.co.varsitycollege.st10204902.purrsonaltrainer.services

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
object RoutineBuilder {
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

    //-----------------------------------------------------------//
    //                          METHODS                          //
    //-----------------------------------------------------------//

    /**
     * Sets the name of the routine being built
     * @param name The name of the routine
     */
    fun setRoutineName(name: String) {
        this.name = name
    }

    /**
     * Sets the color of the routine being built
     * @param color The color of the routine
     */
    fun setRoutineColor(color: String) {
        this.color = color
    }

    /**
     * Sets the description of the routine being built
     * @param description The description of the routine
     */
    fun setRoutineDescription(description: String) {
        this.description = description
    }

    /**
     * Checks if the routine has an exercise
     * @return Boolean - True if the routine has an exercise, false if not
     */
    fun hasAnExercise(): Boolean {
        return exercises.isNotEmpty()
    }

    /**
     * Adds an exercise to the routine being built
     * @param exercise The exercise to add
     */
    fun addExercise(exercise: Exercise) {
        // if the exercise is not already in the exercises list then add it
        if (!exercises.contains(exercise.exerciseID)) {
            val tempExerciseID = exercise.exerciseID
            val tempExerciseName = exercise.exerciseName
            val tempCategory = exercise.category
            var tempNotes = exercise.notes
            if (!exercise.isCustom)
            {
                tempNotes = ""
            }
            val tempMeasurementType = exercise.measurementType
            val tempSets = mapOf<String, WorkoutSet>()
            val workoutExercise = WorkoutExercise(tempExerciseID, tempExerciseName, tempCategory, tempSets , Date(), tempNotes, tempMeasurementType)
            exercises[tempExerciseID] = workoutExercise

            // Notifying subscribers that an exercise has been added
            notifyExerciseAdded()
        }
    }
    fun addWorkoutExercise(workoutExercise: WorkoutExercise)
    {
        val tempExerciseID = workoutExercise.exerciseID
        val newExercises = exercises.plus(tempExerciseID to workoutExercise)
        exercises = newExercises.toMutableMap()

        // Notifying subscribers that an exercise has been added
        notifyExerciseAdded()
    }

    /**
     * Removes an exercise from the routine being built
     * @param exercise The exercise to remove
     * @return Boolean - True if the exercise was removed, false if not
     */
    fun convertToWorkoutExercise(exercise: Exercise): WorkoutExercise {
        val tempExerciseID = exercise.exerciseID
        val tempExerciseName = exercise.exerciseName
        val tempCategory = exercise.category
        var tempNotes = exercise.notes
        if (!exercise.isCustom)
        {
            tempNotes = ""
        }
        val tempSets = mapOf<String, WorkoutSet>()
        return WorkoutExercise(tempExerciseID, tempExerciseName, tempCategory, tempSets , Date(), tempNotes)
    }

    /**
     * Builds the routine and resets the builder
     * @return UserRoutine - The routine that was built
     */
    fun buildRoutine(): UserRoutine {
        val newRoutine =  UserRoutine(routineID, name , color, description, exercises)
        clearRoutine()
        return newRoutine
    }

    fun clearRoutine()
    {
        routineID= CreateID.GenerateID()
        name = ""
        description = ""
        exercises = mutableMapOf()
        color = ""
    }


    // Exercise Added Methods
    //-----------------------------------------------------------//
    /**
     * Adds a listener to the exerciseAddedListeners
     */
    fun addExerciseAddedListener(listener: ExerciseAddedListener)
    {
        exerciseAddedListeners.clear()
        exerciseAddedListeners.add(listener)
    }

    /**
     * Notifies subscribers that an exercise has been added
     */
    private fun notifyExerciseAdded()
    {
        for (listener in exerciseAddedListeners)
        {
            listener.onExerciseAdded()
        }
    }
}
//------------------------***EOF***-----------------------------//