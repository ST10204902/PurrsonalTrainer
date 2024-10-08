package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.login_register

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.AuthManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityHomeLoginRegisterBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.frontend_logic.SoundManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.HomeActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo

/**
 * Activity for handling home login and registration.
 */
class HomeLoginRegisterActivity : AppCompatActivity() {

    // THIS IS THE FIRST PAGE IN UI FLOW
    private lateinit var binding: ActivityHomeLoginRegisterBinding
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val TAG = "GoogleSignIn"
    private val REQ_ONE_TAP = 2
    private lateinit var auth: FirebaseAuth
    private lateinit var soundManager: SoundManager

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-created from a previous saved state, this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeLoginRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        auth = Firebase.auth

        oneTapSetup()

        // Apply login fragment before hand
        populateLoginFragment()

        soundManager = SoundManager(this, R.raw.custom_tap_sound)

        applyFloatUpAnimation(binding.appLogo)
        applyFloatUpAnimation(binding.loginButtonFrame)
        applyFloatUpAnimation(binding.registerButtonFrame)
        applyFloatUpAnimation(binding.googleSignInButton)

        val originalBackgroundRegister = binding.registerButton.background //getting the original background of the button
        binding.registerButton.setOnClickListener {
            soundManager.playSound()
            binding.registerButton.setBackgroundResource(R.drawable.svg_purple_bblbtn_clicked) // Set the background to the clicked background
            Handler(Looper.getMainLooper()).postDelayed({
                binding.registerButton.background =
                    originalBackgroundRegister // Restore the original background after the delay
            }, 200) // Delay in milliseconds
            navigateTo(this, RegisterActivity::class.java, null)
        }

        val originalBackgroundLogin = binding.loginButton.background
        // Binding show and dismiss popup for login
        binding.loginButton.setOnClickListener {
            soundManager.playSound()
            showLoginPopup()
            binding.loginButton.setBackgroundResource(R.drawable.svg_orange_bblbtn_clicked) // Set the background to the clicked background
            Handler(Looper.getMainLooper()).postDelayed({
                binding.loginButton.background =
                    originalBackgroundLogin // Restore the original background after the delay
            }, 200) // Delay in milliseconds
        }
        
        val originalBackgroundGoogle = binding.googleSignInButton.background
        binding.googleSignInButton.setOnClickListener() {
            soundManager.playSound()
            binding.googleSignInButton.setBackgroundResource(R.drawable.svg_grey_bblbtn_clicked) // Set the background to the clicked background
            Handler(Looper.getMainLooper()).postDelayed({
                binding.googleSignInButton.background =
                    originalBackgroundGoogle // Restore the original background after the delay
            }, 200) // Delay in milliseconds
            signInWithGoogle()
        }
        binding.loginDismissArea.setOnClickListener { dismissLoginPopup() }
    }

    /**
     * Sets up the One Tap sign-in client and request.
     */
    private fun oneTapSetup() {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.firebase_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()
    }

    /**
     * Shows the login popup with an animation.
     */
    private fun showLoginPopup() {
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        binding.loginFragmentContainer.startAnimation(slideUp)
        binding.loginFragmentContainer.visibility = View.VISIBLE
        binding.loginDismissArea.visibility = View.VISIBLE
    }

    /**
     * Dismisses the login popup with an animation.
     */
    private fun dismissLoginPopup() {
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        binding.loginFragmentContainer.startAnimation(slideDown)
        slideDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                binding.loginFragmentContainer.visibility = View.GONE
                binding.loginDismissArea.visibility = View.GONE
                // Reset the login fragment
                populateLoginFragment()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }

    /**
     * Initiates the Google sign-in process.
     */
    private fun signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                Log.e(TAG, "Google Sign-In failed", e)
                Toast.makeText(this, "Google Sign-In failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Handles the result from the Google sign-in intent.
     * @param requestCode The request code passed to startIntentSenderForResult.
     * @param resultCode The result code returned by the child activity.
     * @param data An Intent that carries the result data.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ONE_TAP) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken

                if (idToken != null) {
                    Log.d(TAG, "Got ID token: $idToken")
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success
                                Log.d(TAG, "signInWithCredential:success")


                                UserManager.userManagerScope.launch {
                                    val user = auth.currentUser
                                    val authManager = AuthManager()
                                    async {authManager.createUserInRealtimeDatabase(user!!.uid)}.await()
                                    if (user != null) {
                                        UserManager.setUpSingleton(user.uid)
                                    }
                                }.invokeOnCompletion {
                                    // Navigate to the next screen
                                    navigateTo(this, HomeActivity::class.java, null)
                                }

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.e(TAG, "signInWithCredential:failure", task.exception)
                            }
                        }
                } else {
                    Log.e(TAG, "No ID token!")
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Sign-in failed with error code: ${e.statusCode}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Populates the login fragment.
     */
    private fun populateLoginFragment() {
        this.supportFragmentManager.beginTransaction().apply {
            replace(binding.loginFragmentContainer.id, LoginFragment())
            addToBackStack(null)
            commit()
        }
    }

    private fun applyFloatUpAnimation(view: View) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.float_up)
        view.startAnimation(animation)
    }

    /**
     * Called when the activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
//------------------------***EOF***-----------------------------//