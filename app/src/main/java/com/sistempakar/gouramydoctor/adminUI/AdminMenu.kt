package com.sistempakar.gouramydoctor.adminUI

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.userUI.Login
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.adminUI.Aturan.AdminAturan
import com.sistempakar.gouramydoctor.adminUI.Gejala.AdminGejala
import com.sistempakar.gouramydoctor.adminUI.Penyakit.AdminPenyakit



class AdminMenu : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_menu)

        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val cvMoveGejala : CardView = findViewById(R.id.daftarGejala)
        val cvMovePenyakit : CardView = findViewById(R.id.daftarPenyakit)
        val cvMoveAturan : CardView = findViewById(R.id.daftarAturan)
        val logout : ImageButton = findViewById(R.id.logout)
        val tvUser : TextView = findViewById(R.id.tvUser)

        auth = FirebaseAuth.getInstance()

        cvMoveGejala.setOnClickListener {
            val intent = Intent(this, AdminGejala::class.java)
            startActivity(intent)
        }
        cvMovePenyakit.setOnClickListener {
            val intent = Intent(this, AdminPenyakit::class.java)
            startActivity(intent)
        }
        cvMoveAturan.setOnClickListener {
            val intent = Intent(this, AdminAturan::class.java)
            startActivity(intent)
        }

        logout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        val databaseReference = FirebaseDatabase.getInstance().reference

        // Mendapatkan UID pengguna saat ini
        val currentUserId = auth.currentUser?.uid

        // Menambahkan listener untuk mendapatkan data pengguna
        currentUserId?.let<String, DatabaseReference> { databaseReference.child("users").child(it) }
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Mendapatkan nilai nama pengguna dan email
                    val namaPengguna = dataSnapshot.child("username").getValue(String::class.java)

                    // Menampilkan nama pengguna dan email ke dalam TextView
                    val textUser = "$namaPengguna"
                    tvUser.text = textUser
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Penanganan kesalahan, jika diperlukan
                }
            })

    }
    override fun onBackPressed() {
        // Perform your desired back button behavior here
        // For example, show a confirmation dialog or navigate to a previous screen
        showExitConfirmationDialog()
    }

    private fun showExitConfirmationDialog() {
        // Implement your custom exit confirmation dialog logic here
        // For example, use an AlertDialog to ask the user for confirmation

        val alertDialogBuilder = AlertDialog.Builder(this)
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                // Exit the app or perform necessary actions
                auth.signOut()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}