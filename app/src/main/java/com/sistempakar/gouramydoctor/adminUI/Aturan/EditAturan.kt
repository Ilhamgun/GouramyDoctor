package com.sistempakar.gouramydoctor.adminUI.Aturan

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
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
import com.sistempakar.gouramydoctor.Diseases
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Rules

class EditAturan : AppCompatActivity() {
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
        setContentView(R.layout.activity_edit_aturan)

        kodeAturan = findViewById(R.id.kodeAturan)
        spinnerContainer = findViewById(R.id.spinnerContainer)
        tambahGejala = findViewById(R.id.tambahGejala)
        simpanButton = findViewById(R.id.simpanButton)
        spinnerPenyakit = findViewById(R.id.spinnerPenyakit)

        // Inisialisasi list penyakit
        diseaseList = ArrayList()

        rulesRef = FirebaseDatabase.getInstance().reference.child("rules")
        symptomsRef = FirebaseDatabase.getInstance().reference.child("symptoms")
        database = FirebaseDatabase.getInstance().reference

        val kodeAturanExtra = intent.getStringExtra("rulesId")
        if (kodeAturanExtra != null) {
            kodeAturan.setText(kodeAturanExtra)
            retrieveDataFromFirebase(kodeAturanExtra)
        }

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
                val adapter = ArrayAdapter(this@EditAturan, android.R.layout.simple_spinner_item, diseaseList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPenyakit.adapter = adapter

                // Pilih item pada spinner sesuai dengan data di ruleId
                if (kodeAturanExtra != null) {
                    rulesRef.child(kodeAturanExtra).child("disease_id")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val diseaseId = dataSnapshot.getValue(String::class.java)
                                val index = diseaseList.indexOfFirst { it.startsWith(diseaseId.toString()) }
                                spinnerPenyakit.setSelection(index)
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e(TAG, "Failed to read value.", databaseError.toException())
                            }
                        })
                }
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
    }

    private fun retrieveDataFromFirebase(kodeAturan: String) {
        val ruleRef = rulesRef.child(kodeAturan)
        ruleRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val diseaseId = dataSnapshot.child("disease_id").getValue(String::class.java)
                val index = diseaseList.indexOfFirst { it.startsWith(diseaseId.toString()) }
                spinnerPenyakit.setSelection(index)

                val symptomRulesRef = dataSnapshot.child("symptom_rules")
                for (snapshot in symptomRulesRef.children) {
                    val symptomId = snapshot.child("symptom_id").getValue(String::class.java)
                    val cfValue = snapshot.child("cf_value").getValue(String::class.java)
                    addDynamicView(symptomId, cfValue)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Failed to read value.", databaseError.toException())
            }
        })
    }

    private fun addDynamicView(symptomId: String? = null, cfValue: String? = null) {
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
                val symptomList = mutableListOf<String>()
                for (snapshot in dataSnapshot.children) {
                    val symptomId = snapshot.key
                    val symptomName = snapshot.child("symptom_name").getValue(String::class.java)
                    val symptomString = "$symptomId - $symptomName"
                    symptomList.add(symptomString)
                }
                spinnerAdapter.addAll(symptomList)

                // Pilih item pada spinner sesuai dengan data di symptomId
                if (symptomId != null) {
                    val index = symptomList.indexOfFirst { it.startsWith(symptomId) }
                    spinner.setSelection(index)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })

        if (cfValue != null) {
            cfEditText.setText(cfValue)
        }

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

        val builder = AlertDialog.Builder(this@EditAturan)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        // Menyimpan data rules_id, disease_id, dan disease_name ke Firebase DB di child rules
        val ruleRef = rulesRef.child(kodeAturan)
        ruleRef.child("disease_id").setValue(diseaseId)
            .addOnSuccessListener {
                ruleRef.child("disease_name").setValue(diseaseName)
                    .addOnSuccessListener {
                        ruleRef.child("symptom_rules").setValue(dataGejala)
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

    override fun onBackPressed() {
        finish()
    }

    companion object {
        private const val TAG = "EditAturan"
    }
}