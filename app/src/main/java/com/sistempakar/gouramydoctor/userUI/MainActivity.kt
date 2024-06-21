package com.sistempakar.gouramydoctor.userUI

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.R

private lateinit var auth: FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        auth = FirebaseAuth.getInstance()

        val databaseReference = FirebaseDatabase.getInstance().reference

        // Mendapatkan UID pengguna saat ini
        val currentUserId = auth.currentUser?.uid

        //initialization button
        val cvDiagnosa: Button = findViewById(R.id.diagnosaPenyakit)
        val cvRiwayat: CardView = findViewById(R.id.riwayatDiagnosa)
        val cvBantuan: CardView = findViewById(R.id.bantuan)
        val cvTentang: CardView = findViewById(R.id.tentangKami)
        val cvProfil: ImageButton = findViewById(R.id.profilPengguna)
        val cvPenyakit: CardView = findViewById(R.id.daftarPenyakit)
        val cvGejala: CardView = findViewById(R.id.daftarGejala)
        val logout : ImageButton = findViewById(R.id.logout)


        cvDiagnosa.setOnClickListener {
            val intent = Intent(this, Diagnosa::class.java)
            startActivity(intent)
        }
        cvProfil.setOnClickListener {
            val intent = Intent(this, Profil::class.java)
            startActivity(intent)
        }
        cvBantuan.setOnClickListener {
            val intent = Intent(this, Bantuan::class.java)
            startActivity(intent)
        }
        cvRiwayat.setOnClickListener {
            val intent = Intent(this, RiwayatDiagnosa::class.java)
            startActivity(intent)
        }
        cvTentang.setOnClickListener {
            val intent = Intent(this, TentangKami::class.java)
            startActivity(intent)
        }
        cvPenyakit.setOnClickListener{
            val intent = Intent(this, DaftarPenyakit::class.java)
            startActivity(intent)
        }
        cvGejala.setOnClickListener{
            val intent = Intent(this, DaftarGejala::class.java)
            startActivity(intent)
        }
        logout.setOnClickListener {
            showExitConfirmationDialog()
        }

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