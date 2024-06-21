package com.sistempakar.gouramydoctor.adminUI.Penyakit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.Diseases
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.adminUI.AdminMenu

class AdminPenyakit : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var diseaseAdapter: DiseaseAdapter
    private lateinit var diseaseList: MutableList<Diseases>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_penyakit)

        val fabMoveUpload : FloatingActionButton = findViewById(R.id.fab)
        fabMoveUpload.setOnClickListener{
            val intent = Intent(this, UploadPenyakit::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recycleViewPenyakit)
        recyclerView.layoutManager = LinearLayoutManager(this)

        diseaseList = mutableListOf()
        diseaseAdapter = DiseaseAdapter(diseaseList, this::onEditDisease, this::onDeleteDisease, this::onItemClick)
        recyclerView.adapter = diseaseAdapter

        database = FirebaseDatabase.getInstance().getReference("diseases")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                diseaseList.clear()
                for (diseaseSnapshot in snapshot.children) {
                    val diseaseId = diseaseSnapshot.key.toString()
                    val diseaseName = diseaseSnapshot.child("disease_name").value.toString()
                    val disease = Diseases(diseaseId, diseaseName)
                    diseaseList.add(disease)
                }
                diseaseAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun onItemClick(disease: Diseases) {
        // Implementasi logika untuk menampilkan detail data penyakit
        val intent = Intent(this, DetailPenyakit::class.java)
        intent.putExtra("diseaseId", disease.disease_id)
        startActivity(intent)
    }

    private fun onEditDisease(disease: Diseases) {
        // Implement logic to edit disease
        val intent = Intent(this, EditPenyakit::class.java)
        intent.putExtra("diseaseId", disease.disease_id)
        startActivity(intent)
    }

    private fun onDeleteDisease(disease: Diseases) {
        // Implement logic to delete disease
        val diseaseId = disease.disease_id
        val diseaseRef = diseaseId?.let { database.child(it) }

        // Menghapus data penyakit dari Firebase Realtime Database
        diseaseRef?.removeValue()?.addOnSuccessListener {
            // Data berhasil dihapus
            // Lakukan tindakan yang diperlukan, misalnya menampilkan pesan sukses
            Toast.makeText(this, "Data deleted successfully", Toast.LENGTH_SHORT).show()
        }?.addOnFailureListener { error ->
            // Gagal menghapus data
            // Lakukan penanganan kesalahan, misalnya menampilkan pesan kesalahan
            Toast.makeText(this, "Failed to delete data: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        // Pindah ke halaman AdminMenu
        val intent = Intent(this, AdminMenu::class.java)
        startActivity(intent)
        finish() // Menutup aktivitas saat ini (Opsional, tergantung pada kebutuhan Anda)
    }
}
