package za.co.varsitycollege.st10204902.purrsonaltrainer.backend

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Exercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Item
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.User
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserAchievement
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserBackground
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserRoutine
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserWorkout
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutExercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutSet
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineBuilder
import java.util.Date


object UserManager {

    //                          PROPERTIES                       //
    //-----------------------------------------------------------//

    /**
     * The path to the users in the Firebase Realtime Database
     */
    const val USERS_PATH = "users"

    /**
     * A MutableStateFlow that holds the current user
     */
    private val _userFlow = MutableStateFlow<User?>(null)


    /**
     * A read-only property that exposes the current user
     */
    val user: User?
        get() = _userFlow.value

    /**
     * a Job that is used to synchronize the user data with Firebase
     */
    private var userSyncJob: Job? = null

    /**
     * A CoroutineScope that is used to manage the user synchronization job
     */
    val userManagerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    /**
     * the types of sets that can be used in a workout
     */
    enum class SetType
    {
        NORMAL,
        WARMUP,
        FAILURE,
        DROP
    }

    val MEASUREMENT_TYPE = listOf("Reps & Weight", "Time & Distance", "Time")

    //-----------------------------------------------------------//
    //                          METHODS                          //
    //-----------------------------------------------------------//


    // Authentication Methods
    //-----------------------------------------------------------//

    /**
     * Sets up the user singleton with the provided user ID
     * @param userId The ID of the user to set up the singleton with
     * @return A Result object containing the User if successful, or an exception if unsuccessful
     */
    suspend fun setUpSingleton(userId: String): Result<User> {
        return try {
            val databaseRef = FirebaseDatabase.getInstance().getReference(USERS_PATH).child(userId)
            val dataSnapshot = databaseRef.get().await()
            val user = dataSnapshot.getValue(User::class.java)
                ?: throw Exception("User not found in Firebase")
            setUser(user)
            Log.d("UserManager", "User set with ID: ${user.userID}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("UserManager", "Failed to load user: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Logs out the current user
     */
    fun logoutUser() {
        clearUser()
        userSyncJob?.cancel()
        Log.i("UserManager", "User logged out")
    }

    /**
     * Checks if a user is logged in
     * @return True if the user is logged in, false if not
     */
    fun userIsLoggedIn(): Boolean {
        if (_userFlow.value == null) {
            Log.i("UserManager", "User is not logged in")
            return false
        }
        return _userFlow.value != null

    }

    //-----------------------------------------------------------//
    // User Property Updates
    //-----------------------------------------------------------//

    suspend fun deleteUserDataExceptID() {
        val currentUser = _userFlow.value ?: return
        val userId = currentUser.userID

        val updatedUser = User(
            userID = userId,
            name = "",
            catName = "",
            experiencePoints = 0,
            backgroundURI = "",
            level = 0,
            equippedItem = "",
            catURI = "",
            milkCoins = 0,
            userRoutines = emptyMap(),
            userWorkouts = emptyMap(),
            userExercises = emptyMap(),
            userAchievements = emptyMap(),
            userInventory = emptyMap(),
            userBackgrounds = emptyMap()
        )

        try {
            val databaseRef = FirebaseDatabase.getInstance().getReference(USERS_PATH).child(userId)
            databaseRef.setValue(updatedUser).await()
            setUser(updatedUser)
            Log.d("UserManager", "User data cleared except for userID")
        } catch (e: Exception) {
            Log.e("UserManager", "Failed to clear user data: ${e.message}")
        }
    }

    /**
     * Updates the user's name
     * @param newName The new name to update the user with
     */
    fun updateUserName(newName: String) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.copy(name = newName)
            }
            Log.d("UserManager", "User name updated to $newName")
        } else {
            Log.e("UserManager.updateUserName", "User is not logged in")
        }
    }

    /**
     * Updates the user's level
     * @param newLevel The new level to update the user with
     */
    fun updateUserInventory(newItem: Item) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.let {
                    val updatedInventory = user.userInventory + (newItem.itemID to newItem)
                    it.copy(userInventory = updatedInventory)
                }
            }
        } else {
            Log.e("UserManager.updateUserInventory", "User is not logged in")
        }
    }

    /**
     * Updates the user's cat name
     * @param newCatName The new cat name to update the user with
     */
    fun updateCatName(newCatName: String) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.copy(catName = newCatName)
            }
        } else {
            Log.e("UserManager.updateCatName", "User is not logged in")
        }
    }

    /**
     * Updates the user's equiped item
     * @param newItem The new item to update the user with
     */
    fun updateEquipedItem(newItem: String) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.copy(equippedItem = newItem)
            }
        } else {
            Log.e("UserManager.updateEquipedItem", "User is not logged in")
        }
    }

    /**
     * Updates the user's experience points
     * @param newPoints The new experience points to update the user with
     */
    fun updateExperiencePoints(newPoints: Int) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.copy(experiencePoints = newPoints)
            }
        } else {
            Log.e("UserManager.updateExperiencePoints", "User is not logged in")
        }
    }



    fun updateLevel(newLevel: Int) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.copy(level = newLevel)
            }
        } else {
            Log.e("UserManager.updateLevel", "User is not logged in")
        }
    }

    /**
     * Updates the user's background URI
     * @param newURI The new background URI to update the user with
     */
    fun updateBackgroundURI(newURI: String) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.copy(backgroundURI = newURI)
            }
        } else {
            Log.e("UserManager.updateBackgroundURI", "User is not logged in")
        }
    }

    /**
     * Updates the user's cat URI
     * @param newURI The new cat URI to update the user with
     */
    fun updateCatURI(newURI: String) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.copy(catURI = newURI)
            }
        } else {
            Log.e("UserManager.updateCatURI", "User is not logged in")
        }
    }

    /**
     * Updates the user's milk coins
     * @param newCoins The new milk coins to update the user with
     */
    fun updateMilkCoins(newCoins: Int) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.copy(milkCoins = newCoins)
            }
        } else {
            Log.e("UserManager.updateMilkCoins", "User is not logged in")
        }
    }

    /**
     * Updates the user's workout in progress
     * @param workoutID The ID of the workout in progress
     */
    fun updateWorkoutInProgress(workoutID: String) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.copy(workoutInProgress = workoutID)
            }
        } else {
            Log.e("UserManager.updateWorkoutInProgress", "User is not logged in")
        }
    }

    /**
     * resets the user's workout in progress
     */
    fun resetWorkoutInProgress() {
        if (!userIsLoggedIn()) {
            Log.e("UserManager.resetWorkoutInProgress", "User is not logged in")
        } else {
            _userFlow.update { user ->
                user?.copy(workoutInProgress = "")
            }
        }
    }

    /**
     * Sets the user singleton to the provided user
     * @param user The user to set the singleton to
     */
    fun getWorkoutInProgress(): UserWorkout? {
        if (!userIsLoggedIn()) {
            Log.e("UserManager.getWorkoutInProgress", "User is not logged in")
            return null
        }
        if (user?.workoutInProgress.isNullOrEmpty()) {
            Log.w("UserManager.getWorkoutInProgress", "No workout is happening rn")
            return null
        }
        return user?.userWorkouts?.get(user?.workoutInProgress)
    }

    //-----------------------------------------------------------//
    // UserRoutine Management
    //-----------------------------------------------------------//

    /**
     * Adds a new user routine to the user
     * @param newRoutine The new routine to add to the user
     */
    fun addUserRoutine(newRoutine: UserRoutine) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.copy(userRoutines = user.userRoutines + (newRoutine.routineID to newRoutine))
            }
        } else {
            Log.e("UserManager.addUserRoutine", "User is not logged in")

        }
    }

    /**
     * Removes a user routine from the user
     * @param routineID The ID of the routine to remove
     */
    fun removeUserRoutine(routineID: String) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                if (user?.userRoutines.isNullOrEmpty()) {
                    Log.w("UserManager", "User routines are empty or null")
                    return@update user // Return the user unchanged
                }
                user!!.copy(userRoutines = user.userRoutines - (routineID))
            }
        } else {
            Log.e("UserManager.removeUserRoutine", "User is not logged in")
        }
    }

    /**
     * Updates a user routine
     * @param routineID The ID of the routine to update
     * @param updatedRoutine The updated routine to replace the old one
     */
    fun updateUserRoutine(routineID: String, updatedRoutine: UserRoutine) {
        if (userIsLoggedIn()) {
            val newRoutine = updatedRoutine.copy(routineID = routineID)
            removeUserRoutine(routineID)
            addUserRoutine(newRoutine)
        } else {
            Log.e("UserManager.updateUserRoutine", "User is not logged in")
        }
    }
    //-----------------------------------------------------------//
    // UserWorkout Management
    //-----------------------------------------------------------//

    /**
     * Adds a new user workout to the user
     * @param newWorkout The new workout to add to the user
     */
    fun addUserWorkout(newWorkout: UserWorkout) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.copy(userWorkouts = user.userWorkouts + (newWorkout.workoutID to newWorkout))
            }
        } else {
            Log.e("UserManager.addUserWorkout", "User is not logged in")
        }
    }

    /**
     * Removes a user workout from the user
     * @param workoutID The ID of the workout to remove
     */
    fun removeUserWorkout(workoutID: String) {

        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                if (user?.userWorkouts.isNullOrEmpty()) {
                    Log.w("UserManager", "User Workouts are empty or null")
                    return@update user // Return the user unchanged
                }
                user?.copy(userWorkouts = user.userWorkouts - (workoutID))
            }
        } else {
            Log.e("UserManager.removeUserWorkout", "User is not logged in")
        }
    }

    /**
     * Updates a user workout
     * @param workoutID The ID of the workout to update
     * @param updatedWorkout The updated workout to replace the old one
     */
    fun updateUserWorkout(workoutID: String, updatedWorkout: UserWorkout) {
        if (userIsLoggedIn()) {
            val newWorkout = updatedWorkout.copy(workoutID = workoutID)
            removeUserWorkout(workoutID)
            addUserWorkout(newWorkout)
        } else {
            Log.e("UserManager.updateUserWorkout", "User is not logged in")
        }
    }

    //-----------------------------------------------------------//
    // WorkoutExercise Management
    //-----------------------------------------------------------//

    /**
     * Adds an exercise to a workout
     * @param workoutID The ID of the workout to add the exercise to
     * @param exercise The exercise to add to the workout
     */
    fun addExerciseToWorkout(workoutID: String, exercise: WorkoutExercise) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.let {
                    val updatedWorkout = it.userWorkouts[workoutID]?.workoutExercises?.plus(
                        exercise.exerciseID to exercise
                    )
                    updatedWorkout?.let { exercises ->
                        it.copy(
                            userWorkouts = it.userWorkouts + (workoutID to it.userWorkouts[workoutID]!!.copy(
                                workoutExercises = exercises
                            ))
                        )
                    } ?: it // Return the original user if no update occurred
                }
            }
        } else {
            Log.e("UserManager.addExerciseToWorkout", "User is not logged in")
        }
    }

    /**
     * Removes an exercise from a workout
     * @param workoutID The ID of the workout to remove the exercise from
     * @param exerciseID The ID of the exercise to remove
     */
    fun removeExerciseFromWorkout(workoutID: String, exerciseID: String) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                val exercises = user?.userWorkouts?.get(workoutID)?.workoutExercises
                if (exercises.isNullOrEmpty() || !exercises.containsKey(exerciseID)) {
                    Log.w(
                        "UserManager",
                        "the workout or doesn't exist or itn doesn't contain the exercise"
                    )
                    return@update user // Return the user unchanged
                }

                user.let {
                    val updatedWorkout =
                        it.userWorkouts[workoutID]?.workoutExercises?.minus(exerciseID)
                    updatedWorkout?.let { exercises ->
                        it.copy(
                            userWorkouts = it.userWorkouts + (workoutID to it.userWorkouts[workoutID]!!.copy(
                                workoutExercises = exercises
                            ))
                        )
                    } ?: it // Return the original user if no update occurred
                }
            }
        } else {
            Log.e("UserManager.removeExerciseFromWorkout", "User is not logged in")
        }
    }

    /**
     * Updates an exercise in a workout
     * @param workoutID The ID of the workout containing the exercise
     * @param exerciseID The ID of the exercise to update
     * @param updatedExercise The updated exercise to replace the old one
     */
    fun updateExerciseInWorkout(
        workoutID: String, exerciseID: String, updatedExercise: WorkoutExercise
    ) {
        if (userIsLoggedIn()) {
            val newExercise = updatedExercise.copy(exerciseID = exerciseID)
            removeExerciseFromWorkout(workoutID, exerciseID)
            addExerciseToWorkout(workoutID, newExercise)
        } else {
            Log.e("UserManager.updateExerciseInWorkout", "User is not logged in")
        }
    }

    //-----------------------------------------------------------//
    //  WorkoutSet Management
    //-----------------------------------------------------------//

    /**
     * Adds a set to an exercise
     * @param workoutID The ID of the workout containing the exercise
     * @param workoutExerciseID The ID of the exercise to add the set to
     * @param set The set to add to the exercise
     */
    fun addWorkoutSetToWorkoutExercise(workoutID: String, workoutExerciseID: String, set: WorkoutSet) {
        if (userIsLoggedIn())
        {
            _userFlow.update { user ->
                user?.let {
                    val updatedWorkout = it.userWorkouts[workoutID]?.workoutExercises?.get(workoutExerciseID)?.sets?.plus(set.workoutSetID to set)
                    updatedWorkout?.let { sets ->
                        it.copy(
                            userWorkouts = it.userWorkouts + (workoutID to it.userWorkouts[workoutID]!!.copy(
                                workoutExercises = it.userWorkouts[workoutID]!!.workoutExercises + (workoutExerciseID to it.userWorkouts[workoutID]!!.workoutExercises[workoutExerciseID]!!.copy(
                                    sets = sets
                                ))
                            ))
                        )
                    } ?: it // Return the original user if no update occurred
                }
            }
        }
        else
        {
            Log.e("UserManager.addWorkoutSetToWorkoutExercise", "User is not logged in")
        }
    }

    fun addWorkoutSetToWorkoutExerciseInRoutine(routineID: String, exerciseID:String, newSet: WorkoutSet)
    {
        if (userIsLoggedIn())
        {
            user.let {
                val updatedRoutine = it?.userRoutines?.get(routineID)?.exercises?.get(exerciseID)?.sets?.plus(newSet.workoutSetID to newSet)
                updatedRoutine?.let { sets ->
                    it?.let {
                        val updatedRoutineExercises = it.userRoutines[routineID]?.exercises?.get(exerciseID)?.copy(sets = sets)
                        it.copy(
                            userRoutines = it.userRoutines + (routineID to it.userRoutines[routineID]!!.copy(
                                exercises = it.userRoutines[routineID]!!.exercises + (exerciseID to updatedRoutineExercises!!)
                            ))
                        )
                    }
                }
            }
        }
        else
        {
            Log.e("UserManager.addWorkoutSetToWorkoutExerciseInRoutine", "User is not logged in")
        }
    }

    /**
     * Removes a set from an exercise
     * @param workoutID The ID of the workout containing the exercise
     * @param exerciseID The ID of the exercise to remove the set from
     * @param setID The ID of the set to remove
     */
    fun removeWorkoutSetFromWorkoutExercise(workoutID: String, exerciseID: String, setID:String )
    {
        if (userIsLoggedIn())
        {
            _userFlow.update { user ->
                val sets = user?.userWorkouts?.get(workoutID)?.workoutExercises?.get(exerciseID)?.sets
                if (sets.isNullOrEmpty() || !sets.containsKey(setID))
                {
                    Log.w("UserManager", "the workout or doesn't exist or itn doesn't contain the exercise")
                    return@update user // Return the user unchanged
                }

                user.let {
                    val updatedWorkout = it.userWorkouts[workoutID]?.workoutExercises?.get(exerciseID)?.sets?.minus(setID)
                    updatedWorkout?.let { sets ->
                        it.copy(
                            userWorkouts = it.userWorkouts + (workoutID to it.userWorkouts[workoutID]!!.copy(
                                workoutExercises = it.userWorkouts[workoutID]!!.workoutExercises + (exerciseID to it.userWorkouts[workoutID]!!.workoutExercises[exerciseID]!!.copy(
                                    sets = sets
                                ))
                            ))
                        )
                    } ?: it // Return the original user if no update occurred
                }
            }

        }
        else
        {
            Log.e("UserManager.removeWorkoutSetFromWorkoutExercise", "User is not logged in")
        }
    }


    //-----------------------------------------------------------//
    // UserExercise Management
    //-----------------------------------------------------------//

    /**
     * Adds a new exercise to the user
     * @param newExercise The new exercise to add to the user
     */
    fun addUserExercise(newExercise: Exercise) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.let {
                    val updatedExercises =
                        user.userExercises + (newExercise.exerciseID to newExercise)
                    it.copy(userExercises = updatedExercises)
                }
            }
        } else {
            Log.e("UserManager.addUserExercise", "User is not logged in")
        }
    }

    /**
     * Removes an exercise from the user
     * @param exerciseID The ID of the exercise to remove
     */
    fun removeUserExercise(exerciseID: String) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                val exercises = user?.userExercises
                if (exercises.isNullOrEmpty() || !exercises.containsKey(exerciseID)) {
                    Log.w(
                        "UserManager",
                        "user exercises are either empty or the exercise doesn't exist"
                    )
                    return@update user // Return the user unchanged
                }
                user.let {
                    val updatedExercises = user.userExercises - (exerciseID)
                    it.copy(userExercises = updatedExercises)
                }
            }
        } else {
            Log.e("UserManager.removeUserExercise", "User is not logged in")
        }
    }

    /**
     * Updates an exercise in the user
     * @param exerciseID The ID of the exercise to update
     * @param updatedExercise The updated exercise to replace the old one
     */
    fun updateUserExercise(exerciseID: String, updatedExercise: Exercise) {
        if (userIsLoggedIn()) {
            val newExercise = updatedExercise.copy(exerciseID = exerciseID)
            removeUserExercise(exerciseID)
            addUserExercise(newExercise)
        } else {
            Log.e("UserManager.updateUserExercise", "User is not logged in")
        }
    }

    //-----------------------------------------------------------//
    // UserAchievement Management
    //-----------------------------------------------------------//

    /**
     * Adds a new achievement to the user
     * @param newAchievement The new achievement to add to the user
     */
    fun addUserAchievement(newAchievement: UserAchievement) {
        if (userIsLoggedIn()) {

            _userFlow.update { user ->
                user?.let {
                    val updatedAchievements =
                        user.userAchievements + (newAchievement.achievementID to newAchievement)
                    it.copy(userAchievements = updatedAchievements)
                }
            }
        } else {
            Log.e("UserManager.addUserAchievement", "User is not logged in")
        }
    }

    /**
     * Removes an achievement from the user
     * @param achievementID The ID of the achievement to remove
     */
    fun removeUserAchievement(achievementID: String) {

        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                val achievements = user?.userAchievements
                if (achievements.isNullOrEmpty() || !achievements.containsKey(achievementID)) {
                    Log.w(
                        "UserManager",
                        "User Achievement are empty or null or the achievement doesn't exist"
                    )
                    return@update user // Return the user unchanged
                }
                user.let {
                    val updatedAchievements =
                        user.userAchievements + (achievementID to user.userAchievements[achievementID]!!)
                    it.copy(userAchievements = updatedAchievements)
                }
            }
        } else {
            Log.e("UserManager.removeUserAchievement", "User is not logged in")
        }
    }


    /**
     * Updates an achievement in the user
     * @param achievementID The ID of the achievement to update
     * @param updatedAchievement The updated achievement to replace the old one
     */
    fun updateUserAchievement(achievementID: String, updatedAchievement: UserAchievement) {
        if (userIsLoggedIn()) {
            val newAchievement = updatedAchievement.copy(achievementID = achievementID)
            removeUserAchievement(achievementID)
            addUserAchievement(newAchievement)
        } else {
            Log.e("UserManager.updateUserAchievement", "User is not logged in")
        }
    }

    //-----------------------------------------------------------//
    // UserInventory Management
    //-----------------------------------------------------------//

    /**
     * Adds an item to the user's inventory
     * @param newItem The new item to add to the user's inventory
     */
    fun addItemToInventory(newItem: Item) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.let {
                    val updatedInventory = user.userInventory + (newItem.itemID to newItem)
                    it.copy(userInventory = updatedInventory)
                }
            }
        } else {
            Log.e("UserManager.addItemToInventory", "User is not logged in")
        }
    }

    /**
     * Removes an item from the user's inventory
     * @param itemID The ID of the item to remove
     */
    fun removeItemFromInventory(itemID: String) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                val inventory = user?.userInventory
                if (inventory.isNullOrEmpty() || !inventory.containsKey(itemID)) {
                    Log.w("UserManager", "User Inventory is empty or the item doesn't exist")
                    return@update user // Return the user unchanged
                }
                user.let {
                    val updatedInventory = user.userInventory - (itemID)
                    it.copy(userInventory = updatedInventory)
                }
            }
        } else {
            Log.e("UserManager.removeItemFromInventory", "User is not logged in")
        }
    }

    /**
     * Updates an item in the user's inventory
     * @param itemID The ID of the item to update
     * @param updatedItem The updated item to replace the old one
     */
    fun updateUserItem(itemID: String, updatedItem: Item) {
        if (userIsLoggedIn()) {
            val newItem = updatedItem.copy(itemID = itemID)
            removeItemFromInventory(itemID)
            addItemToInventory(newItem)
        } else {
            Log.e("UserManager.updateUserItem", "User is not logged in")
        }
    }

    //-----------------------------------------------------------//
    // UserBackground Management
    //-----------------------------------------------------------//

    /**
     * Adds a new background to the user
     * @param newBackground The new background to add to the user
     */
    fun addUserBackground(newBackground: UserBackground) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                user?.let {
                    val updatedBackgrounds =
                        user.userBackgrounds + (newBackground.backgroundID to newBackground)
                    it.copy(userBackgrounds = updatedBackgrounds)
                }
            }
        } else {
            Log.e("UserManager.addUserBackground", "User is not logged in")
        }
    }

    /**
     * Removes a background from the user
     * @param backgroundID The ID of the background to remove
     */
    fun removeUserBackground(backgroundID: String) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                val backgrounds = user?.userBackgrounds
                if (backgrounds.isNullOrEmpty() || !backgrounds.containsKey(backgroundID)) {
                    Log.w(
                        "UserManager",
                        "User Backgrounds are empty or the background doesn't exist"
                    )
                    return@update user // Return the user unchanged
                }
                user.let {
                    val updatedBackgrounds = user.userBackgrounds - (backgroundID)
                    it.copy(userBackgrounds = updatedBackgrounds)
                }
            }
        } else {
            Log.e("UserManager.removeUserBackground", "User is not logged in")
        }
    }

    /**
     * Updates a background in the user
     * @param backgroundID The ID of the background to update
     * @param updatedBackground The updated background to replace the old one
     */
    fun updateUserBackground(backgroundID: String, updatedBackground: UserBackground) {
        if (userIsLoggedIn()) {
            val newBackground = updatedBackground.copy(backgroundID = backgroundID)
            removeUserBackground(backgroundID)
            addUserBackground(newBackground)
        } else {
            Log.e("UserManager.updateUserBackground", "User is not logged in")
        }
    }

    //-----------------------------------------------------------//
    // CustomCategories Management
    //-----------------------------------------------------------//

    /**
     * Adds a new custom category to the user
     * @param newCategory The new category to add to the user
     */
    fun addCustomCategory(newCategory: String) {
        if (userIsLoggedIn()) {
            if (user!!.customCategories.contains(newCategory)) {
                Log.w("UserManager", "Category already exists")
                return
            }
            _userFlow.update { user ->
                user?.let {
                    val updatedCategories = user.customCategories + newCategory
                    it.copy(customCategories = updatedCategories)
                }
            }
        } else {
            Log.e("UserManager.addCustomCategory", "User is not logged in")
        }
    }

    /**
     * Removes a custom category from the user
     * @param category The category to remove
     */
    fun removeCustomCategory(category: String) {
        if (userIsLoggedIn()) {
            _userFlow.update { user ->
                val categories = user?.customCategories
                if (categories.isNullOrEmpty() || !categories.contains(category)) {
                    Log.w(
                        "UserManager",
                        "User Custom Categories are empty or the category doesn't exist"
                    )
                    return@update user // Return the user unchanged
                }
                user.let {
                    val updatedCategories = user.customCategories - category
                    it.copy(customCategories = updatedCategories)
                }
            }
        } else {
            Log.e("UserManager.removeCustomCategory", "User is not logged in")
        }
    }

    /**
     * Populates the user with sample data including multiple workouts,
     * each with several exercises and sets with varying reps and weights.
     *
     * **Note**: This method is intended for testing purposes only.
     * Remove or disable it in production builds.
     */
    suspend fun populateAllFields() {
        withContext(Dispatchers.IO) {
            try {
                // Ensure the user is logged in
                val currentUser = user ?: throw Exception("User is not logged in")

                // Step 1: Create Sample Exercises
                val exerciseBenchPress = Exercise(
                    exerciseID = CreateID.GenerateID(),
                    exerciseName = "Bench Press",
                    category = "Chest",
                    notes = "Primary chest exercise",
                    measurementType = "Weight",
                    isCustom = false
                )
                val exerciseSquat = Exercise(
                    exerciseID = CreateID.GenerateID(),
                    exerciseName = "Squat",
                    category = "Legs",
                    notes = "Primary leg exercise",
                    measurementType = "Weight",
                    isCustom = false
                )
                val exerciseDeadlift = Exercise(
                    exerciseID = CreateID.GenerateID(),
                    exerciseName = "Deadlift",
                    category = "Back",
                    notes = "Primary back exercise",
                    measurementType = "Weight",
                    isCustom = false
                )

                // Add exercises to UserManager
                addUserExercise(exerciseBenchPress)
                addUserExercise(exerciseSquat)
                addUserExercise(exerciseDeadlift)

                // Step 2: Create Sample Workouts
                val workoutMonday = UserWorkout(
                    workoutID = CreateID.GenerateID(),
                    name = "Monday Workout",
                    workoutExercises = emptyMap(),
                    durationSeconds = 3600, // 1 hour
                    date = Date()
                )

                val workoutWednesday = UserWorkout(
                    workoutID = CreateID.GenerateID(),
                    name = "Wednesday Workout",
                    workoutExercises = emptyMap(),
                    durationSeconds = 3600, // 1 hour
                    date = Date()
                )

                // Add sets to Bench Press
                val bpSet1 = WorkoutSet(
                    workoutSetID = CreateID.GenerateID(),
                    weight = 100,
                    reps = 8,
                    setType = "Standard"
                )
                val bpSet2 = WorkoutSet(
                    workoutSetID = CreateID.GenerateID(),
                    weight = 105,
                    reps = 6,
                    setType = "Standard"
                )
                val bpSet3 = WorkoutSet(
                    workoutSetID = CreateID.GenerateID(),
                    weight = 110,
                    reps = 4,
                    setType = "Standard"
                )

                // Add workouts to UserManager
                addUserWorkout(workoutMonday)
                addUserWorkout(workoutWednesday)

                val routineBuilder = RoutineBuilder()


                addExerciseToWorkout(workoutMonday.workoutID, routineBuilder.convertToWorkoutExercise(exerciseBenchPress))
                addExerciseToWorkout(workoutMonday.workoutID, routineBuilder.convertToWorkoutExercise(exerciseSquat))
                addExerciseToWorkout(workoutWednesday.workoutID, routineBuilder.convertToWorkoutExercise(exerciseDeadlift))

                // Add sets to Bench Press exercise
                addWorkoutSetToWorkoutExercise(workoutMonday.workoutID, exerciseBenchPress.exerciseID, bpSet1)
                addWorkoutSetToWorkoutExercise(workoutMonday.workoutID, exerciseBenchPress.exerciseID, bpSet2)
                addWorkoutSetToWorkoutExercise(workoutMonday.workoutID, exerciseBenchPress.exerciseID, bpSet3)



                // Add sets to Squat
                val squatSet1 = WorkoutSet(
                    workoutSetID = CreateID.GenerateID(),
                    weight = 150,
                    reps = 10,
                    setType = "Standard"
                )
                val squatSet2 = WorkoutSet(
                    workoutSetID = CreateID.GenerateID(),
                    weight = 160,
                    reps = 8,
                    setType = "Standard"
                )
                val squatSet3 = WorkoutSet(
                    workoutSetID = CreateID.GenerateID(),
                    weight = 170,
                    reps = 6,
                    setType = "Standard"
                )

                // Add sets to Squat exercise
                addWorkoutSetToWorkoutExercise(workoutMonday.workoutID, exerciseSquat.exerciseID, squatSet1)
                addWorkoutSetToWorkoutExercise(workoutMonday.workoutID, exerciseSquat.exerciseID, squatSet2)
                addWorkoutSetToWorkoutExercise(workoutMonday.workoutID, exerciseSquat.exerciseID, squatSet3)

                // Add sets to Deadlift
                val dlSet1 = WorkoutSet(
                    workoutSetID = CreateID.GenerateID(),
                    weight = 180,
                    reps = 5,
                    setType = "Standard"
                )
                val dlSet2 = WorkoutSet(
                    workoutSetID = CreateID.GenerateID(),
                    weight = 190,
                    reps = 3,
                    setType = "Standard"
                )
                val dlSet3 = WorkoutSet(
                    workoutSetID = CreateID.GenerateID(),
                    weight = 200,
                    reps = 1,
                    setType = "Standard"
                )

                // Add sets to Deadlift exercise
                addWorkoutSetToWorkoutExercise(workoutWednesday.workoutID, exerciseDeadlift.exerciseID, dlSet1)
                addWorkoutSetToWorkoutExercise(workoutWednesday.workoutID, exerciseDeadlift.exerciseID, dlSet2)
                addWorkoutSetToWorkoutExercise(workoutWednesday.workoutID, exerciseDeadlift.exerciseID, dlSet3)



                // Add sets to Bench Press exercise
                val wbSet1 = WorkoutSet(
                    workoutSetID = CreateID.GenerateID(),
                    weight = 95,
                    reps = 10,
                    setType = "Standard"
                )
                val wbSet2 = WorkoutSet(
                    workoutSetID = CreateID.GenerateID(),
                    weight = 100,
                    reps = 8,
                    setType = "Standard"
                )

                // Add sets to Bench Press in Wednesday Workout
                addWorkoutSetToWorkoutExercise(workoutWednesday.workoutID, exerciseBenchPress.exerciseID, wbSet1)
                addWorkoutSetToWorkoutExercise(workoutWednesday.workoutID, exerciseBenchPress.exerciseID, wbSet2)

                Log.i("UserManager.populateAllFields", "Sample data populated successfully.")

            } catch (e: Exception) {
                Log.e("UserManager.populateAllFields", "Error populating sample data: ${e.message}")
            }
        }

    }



    //-----------------------------------------------------------//
    // Synchronization Methods
    //-----------------------------------------------------------//

    /**
     * Starts the user synchronization job
     * This job listens for changes to the user and synchronizes the user data with Firebase
     */
    private fun startUserSync() {
        userSyncJob?.cancel()
        userSyncJob = userManagerScope.launch {
            _userFlow.filterNotNull().debounce(300).collectLatest { user ->
                Log.d("UserManager", "Detected user change, synchronizing with Firebase")
                val result = synchronizeUserWithFirebase(user)
                result.onFailure { exception ->
                    Log.e("UserManager", "Failed to synchronize user data: ${exception.message}")
                }

            }
        }
    }

    /**
     * Synchronizes the user data with Firebase
     * @param user The user to synchronize
     * @return A Result object containing Unit if successful, or an exception if unsuccessful
     */
    private suspend fun synchronizeUserWithFirebase(user: User): Result<Unit> {
        return try {
            val userId = user.userID
            if (userId.isNotEmpty()) {
                val databaseRef =
                    FirebaseDatabase.getInstance().getReference(USERS_PATH).child(userId)
                databaseRef.setValue(user).await()
                Log.d("UserManager", "User data synchronized with Firebase")
                Result.success(Unit)
            } else {
                val exception = Exception("User ID is empty, cannot synchronize")
                Log.e("UserManager", exception.message ?: "")
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Log.e("UserManager", "Failed to synchronize user data: ${e.message}")
            Result.failure(e)
        }
    }

    //-----------------------------------------------------------//
    // Private helper methods
    //-----------------------------------------------------------//

    /**
     * Sets the user singleton
     */
    private fun setUser(user: User) {
        println(user.userID)
        _userFlow.value = user
        startUserSync()
    }

    /**
     * Clears the user singleton
     */
    private fun clearUser() {
        _userFlow.value = null
    }

    //-----------------------------------------------------------//
}
//------------------------***EOF***-----------------------------//