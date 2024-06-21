package com.sistempakar.gouramydoctor.adminUI.Penyakit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.Diseases
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Symptoms
import com.sistempakar.gouramydoctor.adminUI.Gejala.AdminGejala

class UploadPenyakit : AppCompatActivity() {
    private lateinit var etKodePenyakit: EditText
    private lateinit var etNamaPenyakit: EditText
    private lateinit var etDeskripsiPenyakit: EditText
    private lateinit var etSolusiPenyakit: EditText
    private lateinit var btnSimpan: Button
    private lateinit var DiseaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_penyakit)

        etKodePenyakit = findViewById(R.id.kodePenyakit)
        etNamaPenyakit = findViewById(R.id.namaPenyakit)
        etDeskripsiPenyakit = findViewById(R.id.deskripsiPenyakit)
        etSolusiPenyakit = findViewById(R.id.solusiPenyakit)
        btnSimpan = findViewById(R.id.buttonSimpan)

        etKodePenyakit.isEnabled = false


        val kodePenyakit = etKodePenyakit.text.toString()

        DiseaseRef = FirebaseDatabase.getInstance().reference.child("diseases").child(kodePenyakit)

        btnSimpan.setOnClickListener {
            simpanData()
        }
        generateKodePenyakitOtomatis()
    }

    private fun generateKodePenyakitOtomatis() {
        DiseaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val totalDisease = dataSnapshot.childrenCount
                val newDiseaseCode = String.format("P%03d", totalDisease + 1)
                etKodePenyakit.setText(newDiseaseCode)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors during Firebase access
            }
        })
    }

    private fun simpanData() {
        val kodePenyakit = etKodePenyakit.text.toString()
        val namaPenyakit = etNamaPenyakit.text.toString()
        val deskripsiPenyakit = etDeskripsiPenyakit.text.toString()
        val solusiPenyakit = etSolusiPenyakit.text.toString()

        // Buat objek data gejala baru
        val penyakitBaru = Diseases(kodePenyakit, namaPenyakit, deskripsiPenyakit, solusiPenyakit)

        // Simpan data ke Firebase
        DiseaseRef.child(kodePenyakit).setValue(penyakitBaru)

        val builder = AlertDialog.Builder(this@UploadPenyakit)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()


        // Clear EditText fields
        etKodePenyakit.text.clear()
        etNamaPenyakit.text.clear()
        etDeskripsiPenyakit.text.clear()
        etSolusiPenyakit.text.clear()

        finish()
        val intent = Intent(this, AdminPenyakit::class.java)
        startActivity(intent)
        dialog.dismiss()
    }
}