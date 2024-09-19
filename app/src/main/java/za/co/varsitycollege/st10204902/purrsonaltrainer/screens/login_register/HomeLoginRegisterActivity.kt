package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.login_register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityHomeBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.frontend_logic.BubbleButton

class HomeLoginRegisterActivity : AppCompatActivity() {
    // THIS IS THE FIRST PAGE IN UI FLOW
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_login_register)

        val loginButton: BubbleButton = findViewById(R.id.loginButton)
        val registerButton: BubbleButton = findViewById(R.id.registerButton)
        val ssoButton: BubbleButton = findViewById(R.id.ssoButton)


        // Customize each button with different properties
        customizeButton(loginButton, R.color.login_yellow_gradient, R.color.login_orange, R.string.login)
        customizeButton(registerButton, R.color.register_purple_gradient, R.color.register_purple, R.string.register)
        customizeButton(ssoButton, R.color.sso_gradient_start, R.color.sso_gradient_end, R.string.google_sso)
    }

    /**
     * Customizes the appearance and properties of a BubbleButton.
     *
     * @param button The BubbleButton to be customized.
     * @param startColorRes The resource ID of the start color for the gradient.
     * @param endColorRes The resource ID of the end color for the gradient.
     * @param textRes The resource ID of the text to be set on the button.
     */
    private fun customizeButton(button: BubbleButton, startColorRes: Int, endColorRes: Int, textRes: Int) {
        button.setButtonProperties(startColorRes, endColorRes, textRes)
    }
}
