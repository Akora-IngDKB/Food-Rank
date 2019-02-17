package org.akoraingdkb.foodorder

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({ checkSignIn() }, 750)
    }

    private fun checkSignIn () {
        val mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser == null) {
            // No user signed in. Launch the login activity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            // User has signed in. Launch the main activity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
