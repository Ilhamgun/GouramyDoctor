package com.sistempakar.gouramydoctor.adminUI.Gejala

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Symptoms
import com.sistempakar.gouramydoctor.adminUI.AdminMenu

class AdminGejala : AppCompatActivity(), SymptomAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var symptomAdapter: SymptomAdapter
    private lateinit var database: DatabaseReference
    private lateinit var symptomList: MutableList<Symptoms>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gejala)

        val fabMoveUpload : FloatingActionButton = findViewById(R.id.fab)
        fabMoveUpload.setOnClickListener{
            val intent = Intent(this, UploadGejala::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recycleViewGejala)
        recyclerView.layoutManager = LinearLayoutManager(this)

        symptomList = mutableListOf()
        symptomAdapter = SymptomAdapter(symptomList, this)
        recyclerView.adapter = symptomAdapter

        database = FirebaseDatabase.getInstance().getReference("symptoms")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                symptomList.clear()
                for (symptomSnapshot in snapshot.children) {
                    val symptom = symptomSnapshot.getValue(Symptoms::class.java)
                    symptom?.let { symptomList.add(it) }
                }
                symptomAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    override fun onEditClick(position: Int) {
        // Handle edit button click
        val symptomId = symptomList[position].symptom_id

        // Pindah ke halaman edit dengan mengirim symptoms_id
        val intent = Intent(this, EditGejala::class.java)
        intent.putExtra("symptomId", symptomId)
        startActivity(intent)
    }

    override fun onDeleteClick(position: Int) {
        // Handle delete button click
        val symptomId = symptomList[position].symptom_id

        // Hapus data dari Firebase Realtime Database berdasarkan symptomId
        if (symptomId != null) {
            database.child(symptomId).removeValue()
                .addOnSuccessListener {
                    // Data berhasil dihapus
                    Toast.makeText(
                        this,
                        "Data berhasil dihapus",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { error ->
                    // Gagal menghapus data
                    Toast.makeText(
                        this,
                        "Terjadi kesalahan, gagal menghapus data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onBackPressed() {
        // Pindah ke halaman AdminMenu
        val intent = Intent(this, AdminMenu::class.java)
        startActivity(intent)
        finish() // Menutup aktivitas saat ini (Opsional, tergantung pada kebutuhan Anda)
    }
}