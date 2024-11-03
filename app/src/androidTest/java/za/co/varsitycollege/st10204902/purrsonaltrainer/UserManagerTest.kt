package za.co.varsitycollege.st10204902.purrsonaltrainer

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.AuthManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.CreateID
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.*
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineBuilder

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class UserManagerTest {

    companion object {
        private lateinit var authManager: AuthManager
        private val auth: FirebaseAuth = FirebaseAuth.getInstance()
        private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        private lateinit var currentUserID: String


        private val testUser = User(
            userID = "",
            name = "Test User",
            catName = "Test Cat",
            milkCoins = "0",
            experiencePoints = 0,
            backgroundURI = "test background uri",
            level = 1,
            catURI = "test cat uri",
            userRoutines = emptyMap(),
            userWorkouts = emptyMap(),
            userExercises = emptyMap(),
            userAchievements = emptyMap(),
            userBackgrounds = emptyMap(),
            userInventory = emptyMap()
        )

        @BeforeClass
        @JvmStatic
        fun oneTimeSingletonSetup() {
            runBlocking {
                authManager = AuthManager(auth)
                // Await the registration process
                authManager.registerUser(CreateID.GenerateID() + "@gmail.com", "password")

                currentUserID = auth.currentUser?.uid ?: throw Exception("User registration failed")

                delay(1000)
                // Initialize UserManager with the correct currentUserID
                UserManager.setUpSingleton(currentUserID)

                // Ensure the test user is written under the currentUserID
                val updatedTestUser = testUser.copy(userID = currentUserID)
                database.getReference("users").child(currentUserID).setValue(updatedTestUser).await()

                // Optionally, add a small delay to ensure data is written
                delay(1000)
            }
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            runBlocking {
                val user = auth.currentUser
                user?.delete()?.await()
                database.getReference("users").child(currentUserID).removeValue().await()
            }
        }
    }

    @Before
    fun setUp() {

    }

    @Test
    fun updateUserName_withValidName_updatesName() = runBlocking {
        val newName = "Updated Test User"
        UserManager.updateUserName(newName)

        Thread.sleep(500)
        val updatedUser = UserManager.user
        assertNotNull("User should not be null", updatedUser)
        assertEquals("User name should be updated", newName, updatedUser?.name)
    }

    @Test
    fun updateCatName_withValidName_updatesCatName() = runBlocking {
        val newCatName = "Updated Test Cat"
        UserManager.updateCatName(newCatName)
        val updatedUser = UserManager.user
        assertNotNull("User should not be null", updatedUser)
        assertEquals("Cat name should be updated", newCatName, updatedUser?.catName)
    }

    @Test
    fun updateExperiencePoints_withValidPoints_updatesExperiencePoints() = runBlocking {
        val newExperiencePoints = 100
        UserManager.updateExperiencePoints(newExperiencePoints)
        val updatedUser = UserManager.user
        assertNotNull("User should not be null", updatedUser)
        assertEquals(
            "Experience points should be updated",
            newExperiencePoints,
            updatedUser?.experiencePoints
        )
    }

    @Test
    fun updateCatURI_withValidURI_updatesCatURI() = runBlocking {
        val newCatURI = "Updated Cat URI"
        UserManager.updateCatURI(newCatURI)
        val updatedUser = UserManager.user
        assertNotNull("User should not be null", updatedUser)
        assertEquals("Cat URI should be updated", newCatURI, updatedUser?.catURI)
    }

    @Test
    fun updateBackgroundURI_withValidURI_updatesBackgroundURI() = runBlocking {
        val newBackgroundURI = "Updated Background URI"
        UserManager.updateBackgroundURI(newBackgroundURI)
        val updatedUser = UserManager.user
        assertNotNull("User should not be null", updatedUser)
        assertEquals(
            "Background URI should be updated",
            newBackgroundURI,
            updatedUser?.backgroundURI
        )
    }

    @Test
    fun updateMilkCoins_withValidCoins_updatesMilkCoins() = runBlocking {
        val newMilkCoins = "100"
        UserManager.updateMilkCoins(newMilkCoins)
        val updatedUser = UserManager.user
        assertNotNull("User should not be null", updatedUser)
        assertEquals("Milk coins should be updated", newMilkCoins, updatedUser?.milkCoins)
    }

    @Test
    fun addUserRoutine_withValidRoutine_addsRoutine() = runBlocking {
        val routineBuilder = RoutineBuilder()
        val routineName = "routineName"
        val routineDescription = "routineDescription"
        val routineExercises = mutableMapOf<String, WorkoutExercise>()
        routineBuilder.name = routineName
        routineBuilder.description = routineDescription
        routineBuilder.exercises = routineExercises
        routineBuilder.color = "color"
        val userRoutine = routineBuilder.buildRoutine()
        UserManager.addUserRoutine(userRoutine)
        val updatedUser = UserManager.user
        assertNotNull("User should not be null", updatedUser)
        assertTrue(
            "Routine should be added",
            updatedUser?.userRoutines?.containsKey(userRoutine.routineID) == true
        )
    }

    @Test
    fun addUserExercise_withValidExercise_addsExercise() = runBlocking {
        val exerciseID = CreateID.GenerateID()
        val exerciseName = "exerciseName"
        val category = "category"
        val notes = "notes"
        val exercise = Exercise(
            exerciseID,
            exerciseName,
            category,
            notes
        )

        UserManager.addUserExercise(exercise)
        val updatedUser = UserManager.user

        assertNotNull("User should not be null", updatedUser)
        assertTrue(
            "Exercise should be added",
            updatedUser?.userExercises?.containsKey(exerciseID) == true
        )
    }

    @Test
    fun addUserAchievement_withValidAchievement_addsAchievement() = runBlocking {
        val achievementID = CreateID.GenerateID()
        val achievementName = "achievementName"
        val achievementDescription = "achievementDescription"
        val achievementURI = "achievementURI"
        val userAchievement = UserAchievement(
            achievementID,
            achievementName,
            achievementDescription,
            achievementURI
        )

        UserManager.addUserAchievement(userAchievement)
        val updatedUser = UserManager.user

        assertNotNull("User should not be null", updatedUser)
        assertTrue(
            "Achievement should be added",
            updatedUser?.userAchievements?.containsKey(achievementID) == true
        )
    }

    @After
    fun tearDown() {
        runBlocking {

        }
    }
}