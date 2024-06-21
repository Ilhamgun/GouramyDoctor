package com.sistempakar.gouramydoctor.userUI

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.text.HtmlCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.Gejala
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Rule
import com.sistempakar.gouramydoctor.SymptomRule
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HasilDiagnosa : AppCompatActivity() {
    private lateinit var tvHasilGejala: TextView
    private lateinit var hasilGejalaData: String
    private lateinit var tvHasilDiagnosa: TextView
    private lateinit var tvHasilCF: TextView
    private lateinit var tvHasilDeskripsi: TextView
    private lateinit var tvHasilSolusi: TextView
    private lateinit var tvPenyakitMendekati: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hasil_diagnosa)

        // Inisialisasi tampilan dari layout
        tvHasilGejala = findViewById(R.id.tvHasilGejala)
        tvHasilDiagnosa = findViewById(R.id.tvHasilDiagnosa)
        tvHasilCF = findViewById(R.id.tvHasilPersen)
        tvHasilDeskripsi = findViewById(R.id.tvHasilDeskripsi)
        tvHasilSolusi = findViewById(R.id.tvHasilSolusi)
        tvPenyakitMendekati = findViewById(R.id.tvPenyakitMendekati)
        val btnSimpanRiwayat: Button = findViewById(R.id.btnSimpanRiwayat)
        val cvPenyakitMendekati: CardView = findViewById(R.id.cvPenyakitMendekati)
        val cvDeskripsi: CardView = findViewById(R.id.cvDeskripsi)
        val cvSaran: CardView = findViewById(R.id.cvSaran)

        // Mendapatkan gejala yang dipilih dari aktivitas sebelumnya dan menampilkannya di TextView
        val selectedGejala = intent.getParcelableArrayListExtra<Gejala>("selectedGejala")
        if (selectedGejala != null) {
            val stringBuilder = StringBuilder()
            for (gejala in selectedGejala) {
                val keyakinan = getKeyakinanString(gejala.cf_value)
                stringBuilder.append("${gejala.symptom_id}: ${gejala.symptom_name}, Keyakinan: $keyakinan\n")
            }
            val hasilGejala = stringBuilder.toString()

            // Simpan hasilGejala ke dalam variabel hasilGejalaData
            hasilGejalaData = hasilGejala

            // Tampilkan hasilGejala di TextView
            tvHasilGejala.text = hasilGejala
        }

        // Mendapatkan daftar symptomId dari selectedGejala
        val symptomIdList = selectedGejala?.map { it.symptom_id }

        // Mengakses node "rules" dalam FirebaseDatabase
        val databaseReference = FirebaseDatabase.getInstance().reference.child("rules")

        // Mendengarkan perubahan data pada node "rules"
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // List untuk menyimpan nilai CF gabungan dan daftar nama penyakit
                val cfGabungan = mutableListOf<Double>()
                val diseaseNameList = mutableListOf<String>()

                // Loop untuk setiap rule dalam dataSnapshot
                for (ruleSnapshot in dataSnapshot.children) {
                    val ruleId = ruleSnapshot.key
                    val rule = ruleSnapshot.getValue(Rule::class.java)
                    val symptomRules = rule?.symptom_rules

                    if (symptomRules != null) {
                        val ruleSymptomIds = symptomRules.map { it.symptom_id }

                        // Periksa apakah rule mengandung symptomIds yang sama dengan selectedGejala
                        if (symptomIdList != null) {
                            if (ruleSymptomIds.size == symptomIdList.size && symptomIdList.let { ruleSymptomIds.containsAll(it) }) {
                                val cfValues = mutableListOf<Double>()

                                // Hitung nilai CF gabungan untuk setiap symptom dalam rule
                                for (symptomRule in symptomRules) {
                                    val symptomId = symptomRule.symptom_id
                                    val selectedGejalaCf = selectedGejala.find { it.symptom_id == symptomId }?.cf_value ?: 0.0
                                    val symptomRuleCf = symptomRule.cf_value?.toDoubleOrNull() ?: 0.0
                                    val cf = selectedGejalaCf * symptomRuleCf
                                    cfValues.add(cf)
                                }

                                // Hitung nilai CF gabungan untuk rule berdasarkan jumlah symptom
                                val cfGabunganValue = when (symptomIdList.size) {
                                    1 -> cfValues.first()
                                    2 -> {
                                        val cf1 = cfValues[0]
                                        val cf2 = cfValues[1]
                                        cf1 + (cf2 * (1 - cf1))
                                    }
                                    else -> {
                                        var cfGabunganValue = cfValues.first()
                                        for (i in 1 until cfValues.size) {
                                            val cf = cfValues[i]
                                            cfGabunganValue += cf * (1 - cfGabunganValue)
                                        }
                                        cfGabunganValue
                                    }
                                }

                                cfGabungan.add(cfGabunganValue)
                                diseaseNameList.add(rule.disease_name ?: "")
                            }
                        }
                    }
                }

                // Jika terdapat nilai CF, cari penyakit dengan nilai CF tertinggi
                if (cfGabungan.isNotEmpty()) {
                    val maxCfIndex = cfGabungan.indexOf(cfGabungan.maxOrNull())
                    val maxCfDiseaseName = diseaseNameList[maxCfIndex]

                    cvPenyakitMendekati.visibility = View.GONE
                    // Tampilkan hasil di TextView yang sesuai
                    val formattedCFValue = formatCFValue(cfGabungan[maxCfIndex])
                    tvHasilCF.text = formattedCFValue
                    tvHasilDiagnosa.text = maxCfDiseaseName

                    // Dapatkan data penyakit dari node "diseases" dalam FirebaseDatabase
                    val diseaseDatabaseReference = FirebaseDatabase.getInstance().reference.child("diseases")
                    diseaseDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Temukan data penyakit dengan nama penyakit yang sesuai
                            val diseaseSnapshot = dataSnapshot.children.find { it.child("disease_name").getValue(String::class.java) == maxCfDiseaseName }
                            if (diseaseSnapshot != null) {
                                // Dapatkan dan tampilkan deskripsi dan solusi penyakit di TextView
                                val description = diseaseSnapshot.child("description").getValue(String::class.java)
                                val solution = diseaseSnapshot.child("solution").getValue(String::class.java)

                                val convertContent =
                                    solution?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY) }
                                tvHasilDeskripsi.text = description
                                tvHasilSolusi.text = convertContent
                            } else {
                                // Jika data penyakit tidak ditemukan, kosongkan TextView deskripsi dan solusi
                                tvHasilDeskripsi.text = ""
                                tvHasilSolusi.text = ""
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Tangani kesalahan
                        }
                    })
                } else {
                    // Jika tidak ada nilai CF ditemukan, tampilkan pesan yang sesuai di TextView
                    tvHasilDiagnosa.text = "Penyakit tidak ditemukan"
                    cvPenyakitMendekati.visibility = View.VISIBLE
                    tvHasilDeskripsi.text = ""
                    tvHasilSolusi.text = ""
                    cvDeskripsi.visibility = View.GONE
                    cvSaran.visibility = View.GONE

                    val similarDiseases = symptomIdList?.let { cariPenyakitMirip(it) }
                    if (!similarDiseases.isNullOrEmpty()) {
                        // Jika terdapat penyakit mendekati, tampilkan teksnya pada tvPenyakitMendekati
                        cvPenyakitMendekati.visibility = View.VISIBLE
                        tvPenyakitMendekati.text = similarDiseases.joinToString("\n")
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Tangani kesalahan
            }
        })

        // Atur onClickListener untuk tombol btnSimpanRiwayat
        btnSimpanRiwayat.setOnClickListener {
            // Buat timestamp untuk waktu saat ini
            val currentTime = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("dd-MM-yyyy, HH:mm:ss", Locale.getDefault())
            val formattedTime = dateFormat.format(Date(currentTime))

            // Dapatkan data dari TextView dan FirebaseAuth

            val hasilCFValue = tvHasilCF.text.toString()
            val namaPenyakitValue = tvHasilDiagnosa.text.toString()
            val deskripsiValue = tvHasilDeskripsi.text.toString()
            val solusiValue = tvHasilSolusi.text.toString()
            val penyakitMendekati = tvPenyakitMendekati.text.toString()
            val uidValue = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val waktuValue = formattedTime

            // Buat map historyData untuk menyimpan data
            val historyData = mapOf<String?, Any>(
                "hasilCF" to hasilCFValue,
                "namaPenyakit" to namaPenyakitValue,
                "deskripsi" to deskripsiValue,
                "solusi" to solusiValue,
                "uid" to uidValue,
                "waktu" to waktuValue,
                "hasilGejala" to hasilGejalaData,
                "penyakitMendekati" to penyakitMendekati
            )

            // Akses node "histories" dalam FirebaseDatabase
            val databaseReference = FirebaseDatabase.getInstance().reference.child("histories")
            val historyId = databaseReference.push().key

            // Simpan historyData ke database dan tampilkan pesan Toast berdasarkan hasilnya
            if (historyId != null) {
                databaseReference.child(historyId).setValue(historyData)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            // Jalankan MainActivity setelah menyimpan riwayat
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onBackPressed() {
        // Buat timestamp untuk waktu saat ini
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy, HH:mm:ss", Locale.getDefault())
        val formattedTime = dateFormat.format(Date(currentTime))

        // Dapatkan data dari TextView dan FirebaseAuth

        val hasilCFValue = tvHasilCF.text.toString()
        val namaPenyakitValue = tvHasilDiagnosa.text.toString()
        val deskripsiValue = tvHasilDeskripsi.text.toString()
        val solusiValue = tvHasilSolusi.text.toString()
        val penyakitMendekati = tvPenyakitMendekati.text.toString()
        val uidValue = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val waktuValue = formattedTime

        // Buat map historyData untuk menyimpan data
        val historyData = mapOf<String?, Any>(
            "hasilCF" to hasilCFValue,
            "namaPenyakit" to namaPenyakitValue,
            "deskripsi" to deskripsiValue,
            "solusi" to solusiValue,
            "uid" to uidValue,
            "waktu" to waktuValue,
            "hasilGejala" to hasilGejalaData,
            "penyakitMendekati" to penyakitMendekati
        )

        // Akses node "histories" dalam FirebaseDatabase
        val databaseReference = FirebaseDatabase.getInstance().reference.child("histories")
        val historyId = databaseReference.push().key

        // Simpan historyData ke database dan tampilkan pesan Toast berdasarkan hasilnya
        if (historyId != null) {
            databaseReference.child(historyId).setValue(historyData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Jalankan MainActivity setelah menyimpan riwayat
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Fungsi untuk membuat format nilai CF dengan 2 angka dibelakang koma dan menambahkan simbol persen
    private fun formatCFValue(cfValue: Double): String {
        val formattedValue = String.format("%.2f", cfValue * 100)
        return "$formattedValue%"
    }

    // Fungsi pembantu untuk mengonversi nilai keyakinan menjadi string deskriptif
    private fun getKeyakinanString(keyakinan: Double): String {
        return when (keyakinan) {
            0.0 -> "Tidak"
            0.2 -> "Tidak Tahu"
            0.4 -> "Sedikit Yakin"
            0.6 -> "Cukup Yakin"
            0.8 -> "Yakin"
            1.0 -> "Sangat Yakin"
            else -> "Tidak"
        }
    }

    // Fungsi untuk mencari penyakit yang mirip berdasarkan gejala yang dipilih
    private fun cariPenyakitMirip(selectedSymptomIds: List<String>): List<String> {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("rules")
        val similarDiseaseNames = mutableMapOf<String, Int>()

        // Mendengarkan perubahan data pada node "rules"
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ruleSnapshot in dataSnapshot.children) {
                    val rule = ruleSnapshot.getValue(Rule::class.java)
                    val symptomRules = rule?.symptom_rules

                    if (symptomRules != null) {
                        val ruleSymptomIds = symptomRules.map { it.symptom_id }

                        // Periksa apakah rule mengandung symptomIds yang hampir sama dengan selectedSymptomIds
                        val countIntersection = selectedSymptomIds.intersect(ruleSymptomIds).count()

                        // Tentukan batasan jumlah symptom yang sama untuk dianggap "hampir mirip"
                        val similarSymptomThreshold = 2

                        if (countIntersection >= similarSymptomThreshold) {
                            similarDiseaseNames[rule.disease_name ?: ""] = countIntersection
                        }
                    }
                }

                // Urutkan nama-nama penyakit yang mirip berdasarkan jumlah symptom yang sama
                val sortedSimilarDiseaseNames = similarDiseaseNames.entries
                    .sortedByDescending { it.value }
                    .map { it.key }

                // Tampilkan 5 penyakit dengan symptom yang paling mirip
                val topSimilarDiseaseNames = sortedSimilarDiseaseNames.take(5)
                // Tampilkan topSimilarDiseaseNames ke dalam TextView yang sesuai
                tvPenyakitMendekati.text = topSimilarDiseaseNames.joinToString("\n")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Tangani kesalahan
            }
        })

        return similarDiseaseNames.keys.toList()
    }
}