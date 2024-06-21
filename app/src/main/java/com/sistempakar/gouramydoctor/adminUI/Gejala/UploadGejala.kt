package com.sistempakar.gouramydoctor.adminUI.Gejala

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Video
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
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

class UploadGejala : AppCompatActivity() {
    private lateinit var etKodeGejala: EditText
    private lateinit var etNamaGejala: EditText
    private lateinit var gambarGejala: ImageView
    private lateinit var videoGejala: VideoView
    private lateinit var btnSimpan: Button
    private lateinit var symptomsRef: DatabaseReference
    private lateinit var storageRef: StorageReference
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
        setContentView(R.layout.activity_upload_gejala)

        etKodeGejala = findViewById(R.id.kodeGejala)
        etNamaGejala = findViewById(R.id.namaGejala)
        gambarGejala = findViewById(R.id.gambarGejala)
        videoGejala = findViewById(R.id.videoGejala)
        btnSimpan = findViewById(R.id.buttonSimpan)

        etKodeGejala.isEnabled = false

        val kodeGejala = etKodeGejala.text.toString()

        generateKodeGejalaOtomatis()

        btnSimpan.setOnClickListener {
            simpanData()
        }

        gambarGejala.setOnClickListener {
            pickImageFromGallery()
        }

        videoGejala.setOnClickListener {
            pickVideoFromGallery()
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

    private fun generateKodeGejalaOtomatis() {
        symptomsRef = FirebaseDatabase.getInstance().reference.child("symptoms")
        symptomsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val totalSymptoms = dataSnapshot.childrenCount
                val newSymptomCode = String.format("G%03d", totalSymptoms + 1)
                etKodeGejala.setText(newSymptomCode)

                // Update kodeGejala variable here
                val kodeGejala = newSymptomCode

                symptomsRef = FirebaseDatabase.getInstance().reference.child("symptoms")
                storageRef = FirebaseStorage.getInstance().reference.child("symptoms").child(kodeGejala)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors during Firebase access
            }
        })
    }

    private fun simpanData() {
        val builder = AlertDialog.Builder(this@UploadGejala)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        val kodeGejala = etKodeGejala.text.toString()
        val namaGejala = etNamaGejala.text.toString()

        // Check if kodeGejala is not null or empty before proceeding
        if (kodeGejala.isNotEmpty()) {
            // Buat objek data gejala baru
            val gejalaBaru = Symptoms(kodeGejala, namaGejala)

            // Simpan data ke Firebase
            symptomsRef.child(kodeGejala).setValue(gejalaBaru)

            // Upload image and video to Firebase Storage
            if (imageUri != null) {
                val imageRef = storageRef.child("images/${UUID.randomUUID()}")
                imageRef.putFile(imageUri!!)
                    .addOnSuccessListener { taskSnapshot ->
                        // Get the URL of the uploaded image and save it to Firebase Database
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                            symptomsRef.child(kodeGejala).child("image").setValue(uri.toString())
                        }
                    }
                    .addOnFailureListener {
                        // Handle image upload failure
                    }
            } else {
                // If imageUri is null, send an empty string to Firebase
                symptomsRef.child(kodeGejala).child("image").setValue("")
            }

            if (videoUri != null) {
                val videoRef = storageRef.child("videos/${UUID.randomUUID()}")
                videoRef.putFile(videoUri!!)
                    .addOnSuccessListener { taskSnapshot ->
                        // Get the URL of the uploaded video and save it to Firebase Database
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                            symptomsRef.child(kodeGejala).child("video").setValue(uri.toString())
                        }
                    }
                    .addOnFailureListener {
                        // Handle video upload failure
                    }
            } else {
                // If videoUri is null, send an empty string to Firebase
                symptomsRef.child(kodeGejala).child("video").setValue("")
            }

            // Clear EditText fields
            etKodeGejala.text.clear()
            etNamaGejala.text.clear()

            finish()
            val intent = Intent(this, AdminGejala::class.java)
            startActivity(intent)
            dialog.dismiss()
        } else {
            // Handle the case when kodeGejala is null or empty
            // Display an error message or take appropriate action
        }
    }
}