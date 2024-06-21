package com.sistempakar.gouramydoctor.adminUI.Aturan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.adminUI.Gejala.AdminGejala

class UploadAturan : AppCompatActivity() {
    //initialize layout component
    private lateinit var kodeAturan: EditText
    private lateinit var spinnerContainer: LinearLayout
    private lateinit var tambahGejala: Button
    private lateinit var simpanButton: Button
    private lateinit var spinnerPenyakit: Spinner

    //initialize database
    private lateinit var database: DatabaseReference
    private lateinit var rulesRef: DatabaseReference
    private lateinit var symptomsRef: DatabaseReference

    private lateinit var diseaseList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_aturan)

        kodeAturan = findViewById(R.id.kodeAturan)
        spinnerContainer = findViewById(R.id.spinnerContainer)
        tambahGejala = findViewById(R.id.tambahGejala)
        simpanButton = findViewById(R.id.simpanButton)
        spinnerPenyakit = findViewById(R.id.spinnerPenyakit)

        // Inisialisasi list penyakit
        diseaseList = ArrayList()

        val kodeAturan = kodeAturan.text.toString()
        rulesRef = FirebaseDatabase.getInstance().reference.child("rules").child(kodeAturan)
        symptomsRef = FirebaseDatabase.getInstance().reference.child("symptoms")
        database = FirebaseDatabase.getInstance().reference


        // Mendapatkan referensi ke child 'disease' di Firebase Database
        val database = FirebaseDatabase.getInstance()
        val reference = database.reference.child("diseases")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Mengambil data dari Firebase Database dan menambahkannya ke list penyakit
                for (snapshot in dataSnapshot.children) {
                    val diseaseId = snapshot.key
                    val diseaseName = snapshot.child("disease_name").getValue(String::class.java)
                    val diseaseString = "$diseaseId - $diseaseName"
                    diseaseList.add(diseaseString)
                }

                // Mengatur adapter spinner dengan menggunakan list penyakit
                val adapter = ArrayAdapter(this@UploadAturan, android.R.layout.simple_spinner_item, diseaseList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPenyakit.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Failed to read value.", databaseError.toException())
            }
        })

        tambahGejala.setOnClickListener {
            addDynamicView()
        }

        simpanButton.setOnClickListener {
            saveDataToFirebase()
        }

        generateKodeAturanOtomatis()
    }

    private fun addDynamicView() {
        val dynamicView = layoutInflater.inflate(R.layout.spinner_aturan_item, null)
        spinnerContainer.addView(dynamicView)

        val spinner: Spinner = dynamicView.findViewById(R.id.aturanGejala)
        val cfEditText: EditText = dynamicView.findViewById(R.id.nilaiCF)
        val removeButton: ImageButton = dynamicView.findViewById(R.id.deleteButton)

        // Mengisi data Spinner dari Firebase Database
        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        symptomsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                spinnerAdapter.clear()
                for (snapshot in dataSnapshot.children) {
                    val symptomId = snapshot.key
                    val symptomName = snapshot.child("symptom_name").getValue(String::class.java)
                    val symptomString = "$symptomId - $symptomName"
                    spinnerAdapter.add(symptomString)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })

        // Menghapus dynamic view saat tombol hapus diklik
        removeButton.setOnClickListener {
            spinnerContainer.removeView(dynamicView)
        }
    }

    private fun saveDataToFirebase() {
        val kodeAturan = kodeAturan.text.toString()

        //input spinner penyakit
        val selectedDisease = spinnerPenyakit.selectedItem.toString()
        val diseaseId = selectedDisease.substringBefore(" - ")
        val diseaseName = selectedDisease.substringAfter(" - ")

        val diseaseValue = HashMap<String, Any>()
        diseaseValue["disease_id"] = diseaseId
        diseaseValue["disease_name"] = diseaseName

        //input spinner gejala
        val dataGejala: MutableList<Map<String, String>> = mutableListOf()
        for (i in 0 until spinnerContainer.childCount) {
            val dynamicView = spinnerContainer.getChildAt(i)
            val spinner: Spinner = dynamicView.findViewById(R.id.aturanGejala)
            val cfEditText: EditText = dynamicView.findViewById(R.id.nilaiCF)

            val selectedSymptom = spinner.selectedItem.toString()
            val cf = cfEditText.text.toString()

            val symptomId = selectedSymptom.substringBefore(" - ")
            val symptomName = selectedSymptom.substringAfter(" - ")

            val symptomValue = HashMap<String, Any>()
            symptomValue["symptom_id"] = symptomId
            symptomValue["symptom_name"] = symptomName
            symptomValue["cf_value"] = cf

            val itemData = hashMapOf(
                "symptom_id" to symptomId,
                "symptom_name" to symptomName,
                "cf_value" to cf
            )
            dataGejala.add(itemData)
        }

        val builder = AlertDialog.Builder(this@UploadAturan)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        // Menyimpan data rules_id, disease_id dan disease_name ke Firebase DB di child rules
        val rulesRef = database.child("rules").child(kodeAturan)
        rulesRef.setValue(kodeAturan)
            .addOnSuccessListener {
                rulesRef.setValue(diseaseValue)
                    .addOnSuccessListener {
                        val symptomRulesRef = database.child("rules").child(kodeAturan).child("symptom_rules")
                        symptomRulesRef.setValue(dataGejala)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, AdminAturan::class.java)
                                startActivity(intent)
                                dialog.dismiss()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Terjadi kesalahan dalam menyimpan data!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, AdminAturan::class.java)
                                startActivity(intent)
                                dialog.dismiss()
                            }
                    }

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Terjadi kesalahan dalam menyimpan data!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateKodeAturanOtomatis() {
        rulesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val totalRules = dataSnapshot.childrenCount
                val newRuleCode = String.format("R%03d", totalRules + 1)
                kodeAturan.setText(newRuleCode)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors during Firebase access
            }
        })
    }

    companion object {
        private const val TAG = "UploadAturan"
    }
}