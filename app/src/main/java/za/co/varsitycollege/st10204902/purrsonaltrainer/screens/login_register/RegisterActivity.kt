package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.login_register

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.Validator
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.AuthManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.frontend_logic.SoundManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.HomeActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo

/**
 * Activity for handling user registration.
 */
class RegisterActivity : AppCompatActivity() {
    private lateinit var soundManager: SoundManager

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-created from a previous saved state, this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) //navigating to the register view
        enableEdgeToEdge()
        soundManager = SoundManager(this, R.raw.custom_tap_sound)

        val registerButton: AppCompatButton =
            findViewById(R.id.registerButton) //getting the register button
        val originalBackground =
            registerButton.background //getting the original background of the button

        // Set the layout for the activity
        var email = findViewById<EditText>(R.id.emailInput)
        var password = findViewById<EditText>(R.id.passwordInput)
        var confirmPassword = findViewById<EditText>(R.id.passwordConfirmInput)
        var appLogo = findViewById<View>(R.id.appLogo)
        var registerCard = findViewById<View>(R.id.registerCard)
        Log.d("RegisterActivity", "OnCreate happened")

        applyFloatUpAnimation(registerButton)
        applyFloatUpAnimation(email)
        applyFloatUpAnimation(password)
        applyFloatUpAnimation(confirmPassword)
        applyFloatUpAnimation(appLogo)
        applyFloatUpAnimation(registerCard)

        // Set the on click listener for the register button
        registerButton.setOnClickListener {
            soundManager.playSound()

            Log.d("RegisterActivity", "Register button clicked")
            registerButton.setBackgroundResource(R.drawable.svg_purple_bblbtn_clicked) // Set the background to the clicked background

            // Revert the background after 2 seconds (2000 milliseconds)
            Handler(Looper.getMainLooper()).postDelayed({
                registerButton.background = originalBackground
            }, 400)

            val validator = Validator()
            val emailText = email.text.toString()
            val passwordText = password.text.toString()
            val confirmPasswordText = confirmPassword.text.toString()

            // Validate email
            if (!validator.validateEmail(emailText)) {
                email.error = "Invalid email"
                Log.d("RegisterActivity", "Invalid email")
                return@setOnClickListener
            }

            // Check if passwords match
            if (passwordText != confirmPasswordText) {
                confirmPassword.error = "Passwords do not match"
                Log.d("RegisterActivity", "Passwords do not match")
                return@setOnClickListener
            }

            // Validate password complexity
            if (!validator.validatePasswordComplexity(passwordText)) {
                password.error = "Password not valid"
                Log.d("RegisterActivity", "Password not valid")
                return@setOnClickListener
            }

            // Register the user
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("RegisterActivity", "The coroutine is running")
                val authManager = AuthManager()

                // Register the user and handle the result
                val result: Result<String> = try {
                    authManager.registerUser(emailText, passwordText)
                } catch (e: Exception) {
                    Result.failure(e) // Handle failure case
                }

                val userID = result.getOrNull()!!

                // Handle the result in the main thread
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        try {
                            UserManager.userManagerScope.launch {
                                UserManager.setUpSingleton(userID)
                            }.invokeOnCompletion {
                                // Navigate to the next screen
                                navigateTo(this@RegisterActivity, ProfileSetupActivity::class.java, null)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@RegisterActivity,
                                "Error setting up user",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Unable to complete registration. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("RegisterActivity", "Error: ${result.exceptionOrNull()}")
                    }
                }
            }
        }
    }

    private fun applyFloatUpAnimation(view: View) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.float_up)
        view.startAnimation(animation)
    }

}