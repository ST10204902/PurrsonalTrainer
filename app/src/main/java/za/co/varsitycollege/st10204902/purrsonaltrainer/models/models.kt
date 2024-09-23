package za.co.valsitycollege.st10204902.purrsonaltrainer.models

import com.google.firebase.database.IgnoreExtraProperties
// Item class for representing items in the shop
@IgnoreExtraProperties
data class Item(
    val itemID: String = "",
    val name: String = "",
    val description: String = "",
    val cost: Int = 0,
    val itemURI: String = ""
)

// User class representing a user and their related data
@IgnoreExtraProperties
data class User(
    val userID: String = "",
    val name: String = "",
    val catName: String = "",
    val milkCoins: String = "",
    val experiencePoints: String = "",
    val backgroundURI: String = "",
    val catURI: String = "",
    val userRoutines: Map<String, UserRoutine> = emptyMap(),
    val userWorkouts: Map<String, UserWorkout> = emptyMap(),
    val userExercises: Map<String, Exercise> = emptyMap(),
    val userAchievements: Map<String, UserAchievement> = emptyMap(),
    val userBackgrounds: Map<String, UserBackground> = emptyMap(),
    val userInventory: Map<String, Item> = emptyMap()
)

// WorkoutExercise class representing an exercise during a workout
@IgnoreExtraProperties
data class WorkoutExercise(
    val exerciseID: String = "",
    val exerciseName: String = "",
    val category: String = "",
    val weight: Int? = 0,
    val reps: Int? = 0,
    val distance: Int? = 0,
    val durationSeconds: Int? = 0,
    val notes: String = ""
)

// Exercise class for storing default and custom exercises
@IgnoreExtraProperties
data class Exercise(
    val exerciseID: String = "",
    val exerciseName: String = "",
    val category: String = "",
    val notes: String = ""
)

// UserBackground class for representing user backgrounds
@IgnoreExtraProperties
data class UserBackground(
    val backgroundID: String = "",
    val name: String = "",
    val backgroundURI: String = ""
)

// UserRoutine class representing a user's routine
@IgnoreExtraProperties
data class UserRoutine(
    val routineID: String = "",
    val name: String = "",
    val description: String = "",
    val exercises: Map<String, Exercise> = emptyMap()
)

// UserWorkout class representing a workout within a routine
@IgnoreExtraProperties
data class UserWorkout(
    val workoutID: String = "",
    val name: String = "",
    val workoutExercises: Map<String, WorkoutExercise> = emptyMap(),
    val durationSeconds: Int = 0
)

// UserAchievement class representing a user's achievement
@IgnoreExtraProperties
data class UserAchievement(
    val achievementID: String = "",
    val name: String = "",
    val description: String = "",
    val dateAchieved: String = ""
)