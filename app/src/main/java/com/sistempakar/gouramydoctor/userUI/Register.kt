package com.sistempakar.gouramydoctor.userUI

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Users

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var tvMoveLogin: TextView
    private val emailpattern = "[a-zA-Z0-9._-]+@[a-z]+\\\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        val regisName : EditText = findViewById(R.id.registerUser)
        val regisEmail : EditText = findViewById(R.id.registerEmail)
        val regisPass : EditText = findViewById(R.id.registerPassword)
        val regisButton : Button = findViewById(R.id.btnRegister)
        val eyeIcon = findViewById<ImageView>(R.id.showPass)

        regisPass.transformationMethod = PasswordTransformationMethod.getInstance()

        eyeIcon.setOnClickListener {
            val cursorPosition = regisPass.selectionStart // Save the cursor position

            if (regisPass.transformationMethod == PasswordTransformationMethod.getInstance()) {
                // Password is currently hidden, show it
                regisPass.transformationMethod = null
                eyeIcon.setImageResource(R.drawable.eye) // Set your eye icon for visible state
            } else {
                // Password is currently visible, hide it
                regisPass.transformationMethod = PasswordTransformationMethod.getInstance()
                eyeIcon.setImageResource(R.drawable.hide_eye) // Set your eye icon for hidden state
            }

            // Restore the cursor position
            regisPass.setSelection(cursorPosition)
        }

        tvMoveLogin = findViewById<TextView>(R.id.tvRedirectLogin)

        tvMoveLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        //database & auth initialization
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        regisButton.setOnClickListener {
            val builder = AlertDialog.Builder(this@Register)
            builder.setCancelable(false)
            builder.setView(R.layout.progress_layout)
            val dialog = builder.create()
            dialog.show()

            val user = regisName.text.toString()
            val email = regisEmail.text.toString()
            val password = regisPass.text.toString()
            val role = "user"

            if (user.isEmpty() || email.isEmpty() || password.isEmpty()){
                if (user.isEmpty()){
                    regisName.error = "Username belum diisi"
                }
                if (email.isEmpty()){
                    regisEmail.error = "Email belum diisi"
                }
                if (password.isEmpty()){
                    regisPass.error = "Password belum diisi"
                }
                Toast.makeText(this, "Masukan data yang tepat", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }  else{
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                    if (it.isSuccessful){
                        val databaseRef = database.reference.child("users").child(auth.currentUser!!.uid)
                        val users = Users(auth.currentUser!!.uid, user, email, password, role)
                        dialog.dismiss()

                        databaseRef.setValue(users).addOnCompleteListener {
                            if (it.isSuccessful){
                                val intent = Intent(this, Login::class.java)
                                startActivity(intent)
                                finish()
                            }else{
                                Toast.makeText(
                                    this,
                                    "Terjadi kesalahan, mohon coba lagi",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            }
                        }
                    } else{
                        Toast.makeText(
                            this,
                            "Terjadi kesalahan, mohon coba lagi",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }
                }
            }
        }

    }
}