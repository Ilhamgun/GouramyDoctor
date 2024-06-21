package com.sistempakar.gouramydoctor.userUI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.Diseases
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Symptoms

class DaftarGejala : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DaftarGejala.DaftarGejalaAdapter
    private val diseaseList = mutableListOf<Symptoms>()

    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_gejala)

        database = FirebaseDatabase.getInstance().reference.child("symptoms")

        recyclerView = findViewById(R.id.rvDaftarGejala)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DaftarGejalaAdapter(diseaseList) { symptoms ->
            val intent = Intent(this, UserDetailGejala::class.java)
            intent.putExtra("symptomId", symptoms.symptom_id)
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
                    val symptomId = dataSnapshot.child("symptom_id").value as? String
                    val symptomName = dataSnapshot.child("symptom_name").value as? String
                    val image = dataSnapshot.child("image").value as? String
                    val video = dataSnapshot.child("video").value as? String
                    if (symptomId != null && symptomName != null) {
                        diseaseList.add(Symptoms(symptomId, symptomName, image, video))
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    class DaftarGejalaAdapter(
        private val symptomList: List<Symptoms>,
        private val onItemClick: (Symptoms) -> Unit
    ) : RecyclerView.Adapter<DaftarGejalaAdapter.DaftarPenyakitViewHolder>() {

        class DaftarPenyakitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvSymptomName: TextView = itemView.findViewById(R.id.namaGejala)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaftarPenyakitViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.daftar_gejala_item, parent, false)
            return DaftarPenyakitViewHolder(view)
        }

        override fun onBindViewHolder(holder: DaftarPenyakitViewHolder, position: Int) {
            val disease = symptomList[position]
            holder.tvSymptomName.text = disease.symptom_name
            holder.itemView.setOnClickListener { onItemClick(disease) }
        }

        override fun getItemCount(): Int {
            return symptomList.size
        }
    }
}