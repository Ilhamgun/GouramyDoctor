package com.sistempakar.gouramydoctor.adminUI.Penyakit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.R

class DetailPenyakit : AppCompatActivity() {
    private lateinit var tvDiseaseId: TextView
    private lateinit var tvDiseaseName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvSolution: TextView
    private lateinit var fab: FloatingActionButton
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_penyakit)

        // Inisialisasi TextView
        tvDiseaseId = findViewById(R.id.kodePenyakit)
        tvDiseaseName = findViewById(R.id.namaPenyakit)
        tvDescription = findViewById(R.id.deskripsiPenyakit)
        tvSolution = findViewById(R.id.solusiPenyakit)
        fab = findViewById(R.id.fab)

        // Ambil data dari Intent
        val diseaseId = intent.getStringExtra("diseaseId")

        fab.setOnClickListener {
            val intent = Intent(this, EditPenyakit::class.java)
            intent.putExtra("diseaseId", diseaseId)
            startActivity(intent)
        }

        // Inisialisasi Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference.child("diseases")

        // Ambil data penyakit berdasarkan diseaseId
        diseaseId?.let { database.child(it) }
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val diseaseName = snapshot.child("disease_name").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val solution = snapshot.child("solution").value.toString()

                    // Set nilai ke TextView
                    tvDiseaseId.text = diseaseId
                    tvDiseaseName.text = diseaseName
                    tvDescription.text = description
                    tvSolution.text = solution
                }

                override fun onCancelled(error: DatabaseError) {
                    // Error handling jika gagal mengambil data dari Firebase
                }
            })
    }

}