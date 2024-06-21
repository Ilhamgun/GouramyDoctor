package com.sistempakar.gouramydoctor.adminUI.Penyakit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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

class EditPenyakit : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var diseaseId: String
    private lateinit var etDiseaseId: EditText
    private lateinit var etDiseaseName: EditText
    private lateinit var etDiseaseDesc: EditText
    private lateinit var etDiseaseSolution: EditText
    private lateinit var disease: Diseases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_penyakit)

        // Mendapatkan symptomId dari Intent
        diseaseId = intent.getStringExtra("diseaseId") ?: ""

        // Inisialisasi DatabaseReference
        database = FirebaseDatabase.getInstance().getReference("diseases")

        // Menginisialisasi EditText
        etDiseaseId = findViewById(R.id.kodePenyakit)
        etDiseaseName = findViewById(R.id.namaPenyakit)
        etDiseaseDesc = findViewById(R.id.deskripsiPenyakit)
        etDiseaseSolution = findViewById(R.id.solusiPenyakit)

        etDiseaseId.isEnabled = false

        // Mengambil data yang akan diedit berdasarkan diseaseId
        database.child(diseaseId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                disease = snapshot.getValue(Diseases::class.java) ?: Diseases()

                // Mengisi EditText dengan data yang akan diedit
                etDiseaseId.setText(disease.disease_id)
                etDiseaseName.setText(disease.disease_name)
                etDiseaseDesc.setText(disease.description)
                etDiseaseSolution.setText(disease.solution)
            }

            override fun onCancelled(error: DatabaseError) {
                // Penanganan kesalahan saat mengambil data dari Firebase Realtime Database
            }
        })

        // Setel TextWatcher untuk EditText
        etDiseaseName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Tidak ada aksi sebelum perubahan teks
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Tidak ada aksi saat teks berubah
            }

            override fun afterTextChanged(s: Editable?) {
                // Mengupdate data yang akan diedit saat pengguna melakukan perubahan di EditText
                disease.disease_name = s.toString()
            }
        })

        // Setel listener untuk tombol Simpan
        val btnSave = findViewById<Button>(R.id.buttonSimpan)
        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun populateData(diseases: Diseases) {
        // Mengisi tampilan dengan data yang akan diedit
        val etDiseaseId = findViewById<EditText>(R.id.kodePenyakit)
        val etDiseaseName = findViewById<EditText>(R.id.namaPenyakit)
        val etDiseaseDesc = findViewById<EditText>(R.id.deskripsiPenyakit)
        val etDiseaseSolution = findViewById<EditText>(R.id.solusiPenyakit)

        etDiseaseId.setText(disease.disease_id)
        etDiseaseName.setText(disease.disease_name)
        etDiseaseDesc.setText(disease.description)
        etDiseaseSolution.setText(disease.solution)
    }

    private fun saveChanges() {
        val builder = AlertDialog.Builder(this@EditPenyakit)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()


        val etDiseaseName = findViewById<EditText>(R.id.namaPenyakit)
        val newDiseaseName = etDiseaseName.text.toString()
        val updatedData = HashMap<String, Any>()
        updatedData["disease_name"] = newDiseaseName

        // Memperbarui data yang akan diedit di Firebase Realtime Database
        database.child(diseaseId).updateChildren(updatedData)
            .addOnSuccessListener {
                // Data berhasil diperbarui
                // Lakukan tindakan yang diperlukan, misalnya menampilkan pesan sukses
                Toast.makeText(this, "Data berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish() // Menutup halaman edit setelah berhasil memperbarui data
                dialog.dismiss()
            }
            .addOnFailureListener { error ->
                // Gagal memperbarui data
                // Lakukan penanganan kesalahan, misalnya menampilkan pesan kesalahan
                Toast.makeText(this, "Gagal update data: ${error.message}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

        val intent = Intent(this, AdminPenyakit::class.java)
        startActivity(intent)
        finish()
        dialog.dismiss()

    }
}