package com.sistempakar.gouramydoctor.userUI

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.R

class DetailRiwayat : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var currentUser: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_riwayat)

        database = FirebaseDatabase.getInstance().reference
        currentUser = FirebaseAuth.getInstance()

        val cvPenyakitMendekati = findViewById<CardView>(R.id.cvPenyakitMendekati)
        val tvDeskripsi = findViewById<TextView>(R.id.tvRiwayatDeskripsi)
        val tvSolusi = findViewById<TextView>(R.id.tvRiwayatSolusi)
        val tvGejala = findViewById<TextView>(R.id.tvRiwayatGejala)
        val tvNamaPenyakit = findViewById<TextView>(R.id.tvRiwayatDiagnosa)
        val tvPenyakitMendekati = findViewById<TextView>(R.id.tvPenyakitMendekati)
        val tvCF = findViewById<TextView>(R.id.tvRiwayatPersen)
        val tvWaktu = findViewById<TextView>(R.id.tvRiwayatWaktu)
        val tvAkun = findViewById<TextView>(R.id.tvRiwayatAkun)
        val cvDeskripsi: CardView = findViewById(R.id.cvDeskripsi)
        val cvSaran: CardView = findViewById(R.id.cvSaran)

        // Mengambil semua data dari intent
        val waktu = intent.getStringExtra("waktu")
        val uid = intent.getStringExtra("uid")
        val deskripsi = intent.getStringExtra("deskripsi")
        val solusi = intent.getStringExtra("solusi")
        val hasilCF = intent.getStringExtra("hasilCF")
        val namaPenyakit = intent.getStringExtra("namaPenyakit")
        val hasilGejala = intent.getStringExtra("hasilGejala")
        val penyakitMendekati = intent.getStringExtra("penyakitMendekati")

        // Menampilkan data di TextView
        tvNamaPenyakit.text = namaPenyakit
        tvWaktu.text = waktu
        tvCF.text = hasilCF
        tvDeskripsi.text = deskripsi
        tvGejala.text = hasilGejala
        tvSolusi.text = solusi

        // Periksa apakah namaPenyakit berisi "Penyakit tidak ditemukan"
        if (namaPenyakit == "Penyakit tidak ditemukan") {
            cvDeskripsi.visibility = View.GONE
            cvSaran.visibility = View.GONE
            // Periksa apakah penyakitMendekati tidak kosong dan tidak null
            if (!penyakitMendekati.isNullOrEmpty()) {
                // Jika penyakitMendekati tidak kosong, tampilkan CardView dan teksnya
                cvPenyakitMendekati.visibility = View.VISIBLE
                tvPenyakitMendekati.text = penyakitMendekati
            } else {
                // Jika penyakitMendekati kosong atau berisi, sembunyikan CardView
                cvPenyakitMendekati.visibility = View.GONE
            }
        } else {
            // Jika namaPenyakit tidak berisi "Penyakit tidak ditemukan", sembunyikan CardView
            cvPenyakitMendekati.visibility = View.GONE
        }

        // Menampilkan username akun dari database Firebase
        uid?.let { database.child("users").child(it) }
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val username = dataSnapshot.child("username").value as? String
                    tvAkun.text = username
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
    }
}