package com.sistempakar.gouramydoctor.userUI

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.Gejala
import com.sistempakar.gouramydoctor.R

class Diagnosa : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var diagnoseButton: Button
    private lateinit var gejalaList: MutableList<Gejala>
    private lateinit var adapter: AdapterDiagnosa
    private lateinit var searchView: androidx.appcompat.widget.SearchView

    private lateinit var originalGejalaList: MutableList<Gejala>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_penyakit)

        recyclerView = findViewById(R.id.rvDiagnosa)
        diagnoseButton = findViewById(R.id.btnDiagnosa)

        gejalaList = mutableListOf()
        databaseReference = FirebaseDatabase.getInstance().reference.child("symptoms")

        adapter = AdapterDiagnosa(this, gejalaList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        originalGejalaList = mutableListOf()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                originalGejalaList.clear() // Clear the original list
                gejalaList.clear() // Clear the filtered list

                for (dataSnapshot in snapshot.children) {
                    val gejala = dataSnapshot.getValue(Gejala::class.java)
                    gejala?.let {
                        originalGejalaList.add(it) // Add data to the original list
                        gejalaList.add(it) // Add data to the filtered list
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })

        // Initialize the SearchView and handle search events
        searchView = findViewById(R.id.searchView)
        searchView.queryHint = "Cari gejala penyakit..."
        searchView.setOnClickListener{
            searchView.isIconified = false
        }
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterGejalaList(newText)
                return true
            }
        })

        diagnoseButton.setOnClickListener {
            val builder = AlertDialog.Builder(this@Diagnosa)
            builder.setCancelable(false)
            builder.setView(R.layout.progress_layout)
            val dialog = builder.create()
            dialog.show()

            val selectedGejala = getSelectedGejala()

            if (selectedGejala.isEmpty()) {
                // Tidak ada gejala yang dipilih, berikan tindakan yang sesuai, misalnya tampilkan pesan kesalahan.
                Toast.makeText(this, "Silakan pilih setidaknya satu gejala.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                val intent = Intent(this, HasilDiagnosa::class.java)
                intent.putParcelableArrayListExtra("selectedGejala", ArrayList(selectedGejala))
                startActivity(intent)
                dialog.dismiss()
                finish()
            }
        }
    }

    private fun filterGejalaList(query: String) {
        val filteredList = mutableListOf<Gejala>()

        for (gejala in originalGejalaList) { // Filter the original list
            if (gejala.symptom_name.contains(query, ignoreCase = true)) {
                filteredList.add(gejala)
            }
        }

        adapter.updateData(filteredList)
    }

    private fun getKeyakinanValue(keyakinan: String): Double {
        return when (keyakinan) {
            "Tidak" -> 0.0
            "Tidak Tahu" -> 0.2
            "Sedikit Yakin" -> 0.4
            "Cukup Yakin" -> 0.6
            "Yakin" -> 0.8
            "Sangat Yakin" -> 1.0
            else -> 0.0
        }
    }

    private fun getSelectedGejala(): List<Gejala> {
        return adapter.getGejalaList().filterIndexed { index, gejala ->
            gejala.isChecked || adapter.getCheckedStatus(index)
        }.mapIndexed { index, gejala ->
            val spinnerPosition = adapter.getSpinnerSelection(index)
            val keyakinan = resources.getStringArray(R.array.keyakinan_array)[spinnerPosition]
            val keyakinanValue = getKeyakinanValue(keyakinan)
            Gejala(gejala.symptom_id, gejala.symptom_name, keyakinanValue)
        }
    }

}
