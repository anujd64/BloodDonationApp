package com.example.blooddonationapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.blooddonationapp.databinding.ActivityMainBinding
import com.example.blooddonationapp.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        Log.d("SplashActivity", "onCreate: $currentUser")

        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            if(intent.extras != null){
                val extra = intent.extras?.getString("userId")
                intent.putExtra("userId", extra)
            }
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
