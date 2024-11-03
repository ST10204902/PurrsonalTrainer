package za.co.varsitycollege.st10204902.purrsonaltrainer.backend

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

import kotlinx.coroutines.tasks.await
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.User

class AuthManager(val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    //                          PROPERTIES                       //
    //----------------------------------------------------------//

    /**
     * The Firebase Realtime Database instance
     */
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    //----------------------------------------------------------//
    //                          METHODS                          //
    //----------------------------------------------------------//

    /**
     * Registers a user with the provided email and password
     * USE authResult.user.uid to get the user's unique ID to setup the singleton in UserManger
     * @param email The email of the user
     * @param password The password of the user
     * @return A Result object containing the Users ID if successful, or an exception if unsuccessful
     */
    suspend fun registerUser(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userID = createUserInRealtimeDatabase(authResult.user!!.uid)
            Result.success(userID)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logs in a user with the provided email and password
     * @param email The email of the user
     * @param password The password of the user
     * @return A Result object containing the Users ID if successful, or an exception if unsuccessful
     */
    suspend fun loginUser(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user!!.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * creates the user in the realtime database when they register
     * @param userID The user's unique ID
     * @return The user's unique ID
     */
    suspend fun createUserInRealtimeDatabase(userID: String): String {
        val databaseReference = FirebaseDatabase.getInstance().getReference(UserManager.USERS_PATH).child(userID)

        // Suspend the coroutine until Firebase database operation completes
        return suspendCancellableCoroutine { continuation ->
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User ID exists, no need to create a new user
                        Log.d("AuthManager", "User already exists in the database.")
                        continuation.resume(userID) // Resume coroutine with userID
                    } else {
                        // User ID does not exist, create a new user
                        val userObject = User(
                            userID,
                            "",
                            "",
                            "",
                            0,
                            1,
                            "",
                            "",
                            "",
                            "",
                            emptyMap(),
                            emptyMap(),
                            emptyMap(),
                            emptyMap(),
                            emptyMap(),
                            emptyMap()
                        )

                        // Add the user to the database and wait for the result
                        databaseReference.setValue(userObject)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("AuthManager", "User added to Realtime Database successfully")
                                    continuation.resume(userID) // Resume coroutine with userID
                                } else {
                                    val exception = task.exception ?: Exception("Unknown error occurred while adding user")
                                    Log.e("AuthManager", "Failed to add user to Realtime Database: ${exception.message}")
                                    continuation.resumeWithException(exception) // Resume coroutine with exception
                                }
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("AuthManager", "Database error: ${databaseError.message}")
                    continuation.resumeWithException(databaseError.toException()) // Resume coroutine with exception
                }
            })
        }
    }

//--------------------------------------------------------------//
}
//------------------------***EOF***-----------------------------//