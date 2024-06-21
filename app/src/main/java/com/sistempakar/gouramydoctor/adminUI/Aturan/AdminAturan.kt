package com.sistempakar.gouramydoctor.adminUI.Aturan

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
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Rules
import com.sistempakar.gouramydoctor.adminUI.AdminMenu
import com.sistempakar.gouramydoctor.adminUI.Penyakit.EditPenyakit

class AdminAturan : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var rulesAdapter: RulesAdapter
    private lateinit var ruleList: MutableList<Rules>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_aturan)

        val fabMoveUpload : FloatingActionButton = findViewById(R.id.fab)
        fabMoveUpload.setOnClickListener{
            val intent = Intent(this, UploadAturan::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recycleViewAturan)
        recyclerView.layoutManager = LinearLayoutManager(this)

        ruleList = mutableListOf()
        rulesAdapter = RulesAdapter(ruleList, this::onEditRule, this::onDeleteRule)
        recyclerView.adapter = rulesAdapter

        database = FirebaseDatabase.getInstance().getReference("rules")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ruleList.clear()
                for (diseaseSnapshot in snapshot.children) {
                    val rulesId = diseaseSnapshot.key.toString()
                    val diseaseId = diseaseSnapshot.child("disease_id").value.toString()
                    val diseaseName = diseaseSnapshot.child("disease_name").value.toString()

                    val rule = Rules(rulesId, diseaseId, diseaseName)
                    ruleList.add(rule)
                }
                rulesAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })

    }

    private fun onEditRule(rules: Rules) {
        // Implement logic to edit disease
        val intent = Intent(this, EditAturan::class.java)
        intent.putExtra("rulesId", rules.rule_id)
        startActivity(intent)
    }

    private fun onDeleteRule(rules: Rules) {
        // Implement logic to delete disease
        val ruleId = rules.rule_id
        val ruleRef = ruleId?.let { database.child(it) }

        // Menghapus data penyakit dari Firebase Realtime Database
        ruleRef?.removeValue()?.addOnSuccessListener {
            // Data berhasil dihapus
            // Lakukan tindakan yang diperlukan, misalnya menampilkan pesan sukses
            Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
        }?.addOnFailureListener { error ->
            // Gagal menghapus data
            // Lakukan penanganan kesalahan, misalnya menampilkan pesan kesalahan
            Toast.makeText(this, "Gagal hapus data: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        // Pindah ke halaman AdminMenu
        val intent = Intent(this, AdminMenu::class.java)
        startActivity(intent)
        finish() // Menutup aktivitas saat ini (Opsional, tergantung pada kebutuhan Anda)
    }
}