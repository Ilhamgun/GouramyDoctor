package com.sistempakar.gouramydoctor.userUI

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.R

class Profil : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)
        supportActionBar?.hide()

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference

        val switchMode: Switch = findViewById(R.id.switchMode)
        val cvRiwayat: AppCompatButton = findViewById(R.id.riwayatDiagnosa)
        val cvBantuan: AppCompatButton = findViewById(R.id.bantuan)
        val cvTentang: AppCompatButton = findViewById(R.id.tentangKami)
        val logout: Button = findViewById(R.id.btnLogout)
        val tvUsername: TextView = findViewById(R.id.username)
        val tvEmail: TextView = findViewById(R.id.userEmail)
        val ivProfil: ImageView = findViewById(R.id.ivProfil)
        val btnHapusAkun: Button = findViewById(R.id.btnHapusAkun)
        val btnUbahAkun: Button = findViewById(R.id.btnUbahAkun)

        auth = FirebaseAuth.getInstance()
        val databaseReference = FirebaseDatabase.getInstance().reference

        // Mendapatkan UID pengguna saat ini
        val currentUserId = auth.currentUser?.uid

        // Menambahkan listener untuk mendapatkan data pengguna
        currentUserId?.let { databaseReference.child("users").child(it) }
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Mendapatkan nilai nama pengguna dan email
                    val namaPengguna = dataSnapshot.child("username").getValue(String::class.java)
                    val email = dataSnapshot.child("email").getValue(String::class.java)

                    // Menampilkan nama pengguna dan email ke dalam TextView
                    val textUser = "$namaPengguna"
                    val textEmail = "$email"

                    tvUsername.text = textUser
                    tvEmail.text = textEmail

                    val imageUrl = dataSnapshot.child("imageUrl").getValue(String::class.java)
                    if (imageUrl != null) {
                        // Load the image using Glide with 'this' as the context
                        Glide.with(this@Profil)
                            .load(imageUrl)
                            .into(findViewById(R.id.ivProfil))
                    } else {
                        // If the imageUrl is null or empty, you can display a default image or hide the ImageView
                        findViewById<ImageView>(R.id.ivProfil).setImageResource(R.drawable.user)
                        // Or: findViewById<ImageView>(R.id.profileImageView).visibility = View.GONE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Penanganan kesalahan, jika diperlukan
                }
            })

        switchMode.setOnCheckedChangeListener { _, isChecked ->
            // Set mode malam jika isChecked bernilai true, jika tidak set mode terang
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }


        btnHapusAkun.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus Akun")
                .setMessage("Apakah anda yakin ingin menghapus akun ini?")
                .setPositiveButton("Ya") { _, _ ->
                    // Exit the app or perform necessary actions
                    val currentUser: FirebaseUser? = auth.currentUser
                    if (currentUser != null) {
                        // Mendapatkan UID pengguna saat ini
                        val currentUid = currentUser.uid

                        // Hapus data dari Realtime Database
                        deleteDataFromDatabase(currentUid)

                        // Hapus akun pengguna dari Authentication
                        deleteAccountFromAuthentication(currentUser)
                    }
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
        btnUbahAkun.setOnClickListener {
            val intent = Intent(this, UbahAkun::class.java)
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
        //logout
        logout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun deleteDataFromDatabase(uid: String) {
        // Ubah sesuai dengan struktur data Anda di Firebase Realtime Database
        val dataRef: DatabaseReference = databaseReference.child("users").child(uid)
        dataRef.removeValue()
    }

    private fun deleteAccountFromAuthentication(user: FirebaseUser) {
        user.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Akun pengguna telah dihapus dari Authentication
                } else {
                    // Gagal menghapus akun pengguna dari Authentication
                }
            }
    }
}