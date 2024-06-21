package com.sistempakar.gouramydoctor.userUI

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        // Inisialisasi FirebaseAuth dan DatabaseReference
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Mendapatkan referensi ke child yang berisi data role pengguna

        val tvMoveRegister = findViewById<TextView>(R.id.tvRedirectRegister)
        val loginEmail = findViewById<EditText>(R.id.loginEmail)
        val loginPass = findViewById<EditText>(R.id.loginPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val eyeIcon = findViewById<ImageView>(R.id.showPass)

        loginPass.transformationMethod = PasswordTransformationMethod.getInstance()

        eyeIcon.setOnClickListener {
            val cursorPosition = loginPass.selectionStart // Save the cursor position

            if (loginPass.transformationMethod == PasswordTransformationMethod.getInstance()) {
                // Password is currently hidden, show it
                loginPass.transformationMethod = null
                eyeIcon.setImageResource(R.drawable.eye) // Set your eye icon for visible state
            } else {
                // Password is currently visible, hide it
                loginPass.transformationMethod = PasswordTransformationMethod.getInstance()
                eyeIcon.setImageResource(R.drawable.hide_eye) // Set your eye icon for hidden state
            }

            // Restore the cursor position
            loginPass.setSelection(cursorPosition)

        }


        tvMoveRegister.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val builder = AlertDialog.Builder(this@Login)
            builder.setCancelable(false)
            builder.setView(R.layout.progress_layout)
            val dialog = builder.create()
            dialog.show()

            val email = loginEmail.text.toString()
            val pass = loginPass.text.toString()

            if (email.isEmpty() || pass.isEmpty()){
                if (email.isEmpty()){
                    loginEmail.error = "Email belum diisi"
                }
                if (pass.isEmpty()){
                    loginPass.error = "Password belum diisi"
                }
                Toast.makeText(this, "Masukan data yang tepat", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }  else{
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val currentUser = auth.currentUser

                            // Retrieve the user's role from Firebase Realtime Database
                            val uid = currentUser?.uid
                            uid?.let { it1 -> databaseReference.child("users").child(it1) }
                                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val role = dataSnapshot.child("role").value?.toString()

                                        // Check the user's role and navigate accordingly
                                        if (role == "admin") {
                                            // Navigate to the admin page
                                            val intent = Intent(this@Login, AdminMenu::class.java)
                                            startActivity(intent)
                                        } else {
                                            // Navigate to the user page
                                            val intent = Intent(this@Login, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // Handle any errors
                                        Toast.makeText(
                                            this@Login,
                                            "Error dalam mengambil role pengguna: " + databaseError.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        dialog.dismiss()
                                    }
                                })
                        } else {
                            // Login failed
                            Toast.makeText(
                                this,
                                "Autentikasi gagal. Tidak ada akun yang terdaftar dengan informasi yang Anda masukkan.",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        }
                    }
            }
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}