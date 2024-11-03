package za.co.varsitycollege.st10204902.purrsonaltrainer.models

import com.google.firebase.database.IgnoreExtraProperties
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.CreateID
import java.util.Date

/**
 * Data class representing an item in the shop.
 *
 * @property itemID The unique identifier for the item.
 * @property name The name of the item.
 * @property description A brief description of the item.
 * @property cost The cost of the item in the shop.
 * @property itemURI The URI for the item's image or resource.
 */
@IgnoreExtraProperties
data class Item(
    val itemID: String = "",
    val name: String = "",
    val description: String = "",
    val cost: Int = 0,
    val itemURI: String = ""
)

/**
 * Data class representing a user and their related data.
 *
 * @property userID The unique identifier for the user.
 * @property name The name of the user.
 * @property catName The name of the user's cat.
 * @property milkCoins The amount of milk coins the user has.
 * @property experiencePoints The experience points the user has earned.
 * @property backgroundURI The URI for the user's background image.
 * @property catURI The URI for the user's cat image.
 * @property userRoutines A map of the user's routines.
 * @property userWorkouts A map of the user's workouts.
 * @property userExercises A map of the user's exercises.
 * @property userAchievements A map of the user's achievements.
 * @property userBackgrounds A map of the user's backgrounds.
 * @property userInventory A map of the user's inventory items.
 * @property customCategories A list of custom categories created by the user.
 */
@IgnoreExtraProperties
data class User(
    val userID: String = "",
    val name: String = "",
    val catName: String = "",
    val milkCoins: Int = 0,
    val experiencePoints: Int = 0,
    val level:  Int = 1,
    val backgroundURI: String = "",
    val catURI: String = "",
    val workoutInProgress: String = "",
    val equippedItem: String = "",
    val userRoutines: Map<String, UserRoutine> = emptyMap(),
    val userWorkouts: Map<String, UserWorkout> = emptyMap(),
    val userExercises: Map<String, Exercise> = emptyMap(),
    val userAchievements: Map<String, UserAchievement> = emptyMap(),
    val userBackgrounds: Map<String, UserBackground> = emptyMap(),
    val userInventory: Map<String, Item> = emptyMap(),
    val customCategories: List<String> = emptyList()
)

/**
 * Data class representing a workout exercise.
 *
 * @property exerciseID The unique identifier for the exercise.
 * @property exerciseName The name of the exercise.
 * @property category The category of the exercise.
 * @property sets A map of sets within the workout exercise.
 * @property date The date the exercise was performed.
 * @property notes Additional notes about the exercise.
 * @property measurementType The type of measurement used for the exercise.
 */
@IgnoreExtraProperties
data class WorkoutExercise(
    val exerciseID: String = CreateID.GenerateID(),
    val exerciseName: String = "",
    val category: String = "",
    val sets: Map<String, WorkoutSet> = emptyMap(),
    val date: Date = Date(),
    val notes: String = "",
    val measurementType: String = "",
)

/**
 * Data class representing a set within a workout exercise.
 *
 * @property workoutSetID The unique identifier for the workout set.
 * @property weight The weight used in the set.
 * @property reps The number of repetitions performed in the set.
 * @property distance The distance covered in the set.
 * @property durationSeconds The duration of the set in seconds.
 * @property setType The type of the set.
 */
@IgnoreExtraProperties
data class WorkoutSet(
    var workoutSetID: String = CreateID.GenerateID(),
    var weight: Int? = 0,
    var reps: Int? = 0,
    var distance: Int? = 0,
    var durationSeconds: Int? = 0,
    var setType: String = "",
    var completed : Boolean = false
)

/**
 * Data class representing an exercise.
 *
 * @property exerciseID The unique identifier for the exercise.
 * @property exerciseName The name of the exercise.
 * @property category The category of the exercise.
 * @property notes Additional notes about the exercise.
 * @property measurementType The type of measurement used for the exercise.
 * @property isCustom Indicates if the exercise is custom created by the user.
 */
@IgnoreExtraProperties
data class Exercise(
    val exerciseID: String = CreateID.GenerateID(),
    val exerciseName: String = "",
    val category: String = "",
    val notes: String = "",
    val measurementType: String = "",
    val isCustom: Boolean = true
)

/**
 * Data class representing a user background.
 *
 * @property backgroundID The unique identifier for the background.
 * @property name The name of the background.
 * @property backgroundURI The URI for the background image.
 */
@IgnoreExtraProperties
data class UserBackground(
    val backgroundID: String = CreateID.GenerateID(),
    val name: String = "",
    val backgroundURI: String = ""
)

/**
 * Data class representing a user's routine.
 *
 * @property routineID The unique identifier for the routine.
 * @property name The name of the routine.
 * @property color The color associated with the routine.
 * @property description A brief description of the routine.
 * @property exercises A map of exercises within the routine.
 */
@IgnoreExtraProperties
data class UserRoutine(
    val routineID: String = CreateID.GenerateID(),
    val name: String = "",
    val color: String = "",
    val description: String = "",
    val exercises: Map<String, WorkoutExercise> = emptyMap()
)

// TODO: Add color field please Michael
// TODO: Add user body weight please Michael
/**
 * Data class representing a workout within a routine.
 *
 * @property workoutID The unique identifier for the workout.
 * @property name The name of the workout.
 * @property workoutExercises A map of exercises within the workout.
 * @property durationSeconds The duration of the workout in seconds.
 * @property date The date the workout was performed.
 */
@IgnoreExtraProperties
data class UserWorkout(
    val workoutID: String = CreateID.GenerateID(),
    val name: String = "",
    val workoutExercises: Map<String, WorkoutExercise> = emptyMap(),
    val durationSeconds: Int = 0,
    val date: Date = Date(),
    val bodyWeight: Int = 0,
    val color: String = "",
)


/**
 * Data class representing a user's achievement.
 *
 * @property achievementID The unique identifier for the achievement.
 * @property name The name of the achievement.
 * @property description A brief description of the achievement.
 * @property dateAchieved The date the achievement was earned.
 */
@IgnoreExtraProperties
data class UserAchievement(
    val achievementID: String = CreateID.GenerateID(),
    val name: String = "",
    val description: String = "",
    val dateAchieved: String = ""
)