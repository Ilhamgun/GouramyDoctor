package com.sistempakar.gouramydoctor.userUI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.Diseases
import com.sistempakar.gouramydoctor.R

class DaftarPenyakit : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DaftarPenyakitAdapter
    private val diseaseList = mutableListOf<Diseases>()

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_penyakit)

        database = FirebaseDatabase.getInstance().reference.child("diseases")

        recyclerView = findViewById(R.id.rvDaftarPenyakit)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DaftarPenyakitAdapter(diseaseList) { diseases ->
            val intent = Intent(this, UserDetailPenyakit::class.java)
            intent.putExtra("diseaseId", diseases.disease_id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadDataFromFirebase()
    }

    private fun loadDataFromFirebase() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                diseaseList.clear()
                for (dataSnapshot in snapshot.children) {
                    val diseaseId = dataSnapshot.child("disease_id").value as? String
                    val diseaseName = dataSnapshot.child("disease_name").value as? String
                    if (diseaseId != null && diseaseName != null) {
                        diseaseList.add(Diseases(diseaseId, diseaseName))
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    class DaftarPenyakitAdapter(
        private val diseaseList: List<Diseases>,
        private val onItemClick: (Diseases) -> Unit
    ) : RecyclerView.Adapter<DaftarPenyakitAdapter.DaftarPenyakitViewHolder>() {

        class DaftarPenyakitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvDiseaseName: TextView = itemView.findViewById(R.id.namaPenyakit)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaftarPenyakitViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.daftar_penyakit_item, parent, false)
            return DaftarPenyakitViewHolder(view)
        }

        override fun onBindViewHolder(holder: DaftarPenyakitViewHolder, position: Int) {
            val disease = diseaseList[position]
            holder.tvDiseaseName.text = disease.disease_name
            holder.itemView.setOnClickListener { onItemClick(disease) }
        }

        override fun getItemCount(): Int {
            return diseaseList.size
        }
    }
}