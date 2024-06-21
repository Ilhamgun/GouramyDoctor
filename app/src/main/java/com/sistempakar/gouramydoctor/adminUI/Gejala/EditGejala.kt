package com.sistempakar.gouramydoctor.adminUI.Gejala

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Symptoms
import java.util.UUID

class EditGejala : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var symptomsRef: DatabaseReference
    private lateinit var symptomId: String
    private lateinit var etSymptomId: EditText
    private lateinit var etSymptomName: EditText
    private lateinit var gambarGejala: ImageView
    private lateinit var videoGejala: VideoView
    private lateinit var symptom: Symptoms

    private var imageUri: Uri? = null
    private var videoUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data
            gambarGejala.setImageURI(imageUri)
        }
    }

    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            videoUri = data?.data
            videoGejala.setVideoURI(videoUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_gejala)

        // Mendapatkan symptomId dari Intent
        symptomId = intent.getStringExtra("symptomId") ?: ""

        // Menginisialisasi EditText
        etSymptomId = findViewById(R.id.kodeGejala)
        etSymptomName = findViewById(R.id.namaGejala)

        gambarGejala = findViewById(R.id.gambarGejala)
        videoGejala = findViewById(R.id.videoGejala)

        etSymptomId.isEnabled = false

        val kodeGejala = etSymptomId.text.toString()

        // Inisialisasi DatabaseReference
        database = FirebaseDatabase.getInstance().getReference("symptoms")
        storageRef = FirebaseStorage.getInstance().reference.child("symptoms").child(symptomId)
        symptomsRef = FirebaseDatabase.getInstance().reference.child("symptoms").child(symptomId)

        // Mengambil data yang akan diedit berdasarkan symptomId
        database.child(symptomId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                symptom = snapshot.getValue(Symptoms::class.java) ?: Symptoms()

                // Set existing image and video URIs
                imageUri = if (!symptom.image.isNullOrEmpty()) Uri.parse(symptom.image) else null
                videoUri = if (!symptom.video.isNullOrEmpty()) Uri.parse(symptom.video) else null

                // Mengisi EditText dengan data yang akan diedit
                etSymptomId.setText(symptom.symptom_id)
                etSymptomName.setText(symptom.symptom_name)

                // Set existing image and video
                if (imageUri != null) {
                    Glide.with(this@EditGejala)
                        .load(imageUri)
                        .into(gambarGejala)
                }

                if (videoUri != null) {
                    videoGejala.setVideoURI(videoUri)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Penanganan kesalahan saat mengambil data dari Firebase Realtime Database
            }
        })

        // Setel TextWatcher untuk EditText
        etSymptomName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Tidak ada aksi sebelum perubahan teks
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Tidak ada aksi saat teks berubah
            }

            override fun afterTextChanged(s: Editable?) {
                // Mengupdate data yang akan diedit saat pengguna melakukan perubahan di EditText
                symptom.symptom_name = s.toString()
            }
        })

        gambarGejala.setOnClickListener {
            pickImageFromGallery()
        }

        videoGejala.setOnClickListener {
            pickVideoFromGallery()
        }

        // Setel listener untuk tombol Simpan
        val btnSave = findViewById<Button>(R.id.buttonSimpan)
        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun pickVideoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        videoPickerLauncher.launch(intent)
    }

    private fun populateData(symptom: Symptoms) {
        // Mengisi tampilan dengan data yang akan diedit
        val etSymptomId = findViewById<EditText>(R.id.kodeGejala)
        val etSymptomName = findViewById<EditText>(R.id.namaGejala)

        etSymptomId.setText(symptom.symptom_id)
        etSymptomName.setText(symptom.symptom_name)
    }

    private fun saveChanges() {
        val builder = AlertDialog.Builder(this@EditGejala)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        // Upload image to Firebase Storage
        if (imageUri != null) {
            val imageRef = storageRef.child("images/${UUID.randomUUID()}")
            imageRef.putFile(imageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the URL of the uploaded image and save it to Firebase Realtime Database
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        symptomsRef.child("image").setValue(uri.toString())
                    }
                    // Continue with updating symptom name after image upload is successful
                    updateSymptomName(dialog)
                }
                .addOnFailureListener {
                    // Handle image upload failure
                    dialog.dismiss()
                }
        } else {
            // If imageUri is null, do not update the image URL in Firebase Realtime Database
            updateSymptomName(dialog)
        }

        // Upload video to Firebase Storage
        if (videoUri != null) {
            val videoRef = storageRef.child("videos/${UUID.randomUUID()}")
            videoRef.putFile(videoUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the URL of the uploaded video and save it to Firebase Realtime Database
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        symptomsRef.child("video").setValue(uri.toString())
                    }
                    // Continue with updating symptom name after video upload is successful
                    updateSymptomName(dialog)
                }
                .addOnFailureListener {
                    // Handle video upload failure
                    dialog.dismiss()
                }
        } else {
            // If videoUri is null, do not update the video URL in Firebase Realtime Database
            updateSymptomName(dialog)
        }
    }

    private fun updateSymptomName(dialog: AlertDialog) {
        val etSymptomName = findViewById<EditText>(R.id.namaGejala)
        val newSymptomName = etSymptomName.text.toString()
        val updatedData = HashMap<String, Any>()
        updatedData["symptom_name"] = newSymptomName

        // Update symptom name in Firebase Realtime Database
        symptomsRef.updateChildren(updatedData)
            .addOnSuccessListener {
                // Data successfully updated
                // Perform necessary actions, e.g., showing success message
                Toast.makeText(this, "Data berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish() // Close the edit page after successfully updating data
                dialog.dismiss()
            }
            .addOnFailureListener { error ->
                // Failed to update data
                // Handle the error, e.g., showing error message
                Toast.makeText(this, "Gagal update data: ${error.message}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

        val intent = Intent(this, AdminGejala::class.java)
        startActivity(intent)
    }
}