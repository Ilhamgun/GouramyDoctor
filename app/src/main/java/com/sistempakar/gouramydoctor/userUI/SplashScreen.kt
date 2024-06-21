package com.sistempakar.gouramydoctor.userUI

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.adminUI.AdminMenu

@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {
    private val SPLASH_DELAY: Long = 3000 // Durasi SplashScreen (dalam milidetik)
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("users")

        // Handler untuk menunda navigasi ke halaman berikutnya
        Handler().postDelayed({
            // Mengecek apakah pengguna sudah login sebelumnya
            if (firebaseAuth.currentUser != null) {
                val userId = firebaseAuth.currentUser!!.uid
                // Mendapatkan data pengguna dari database berdasarkan UID
                databaseReference.child(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val userRole = dataSnapshot.child("role").value as String
                            if (userRole == "user") {
                                // Mengarahkan ke halaman user
                                startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                            } else {
                                // Mengarahkan ke halaman admin atau halaman sesuai peran lainnya
                                startActivity(Intent(this@SplashScreen, AdminMenu::class.java))
                            }
                            finish()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle database error
                        }
                    })
            } else {
                // Pengguna belum login, arahkan ke halaman login
                startActivity(Intent(this@SplashScreen, Onboarding::class.java))
                finish()
            }
        }, SPLASH_DELAY)
    }

    /*// This is used to hide the status bar and make
        // the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // we used the postDelayed(Runnable, time) method
        // to send a message with a delayed time.
        //Normal Handler is deprecated , so we have to change the code little bit

        // Handler().postDelayed({
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Onboarding::class.java)
            startActivity(intent)
            finish()
        }, 3000) // 3000 is the delayed time in milliseconds.
    }*/
}