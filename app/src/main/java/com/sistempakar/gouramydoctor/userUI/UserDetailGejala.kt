package com.sistempakar.gouramydoctor.userUI

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Symptoms

class UserDetailGejala : AppCompatActivity() {
    private lateinit var ivGejala: ImageView
    private lateinit var vvGejala: VideoView
    private lateinit var tvGejala: TextView
    private lateinit var symptomsRef: DatabaseReference
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail_gejala)

        ivGejala = findViewById(R.id.ivGejala)
        vvGejala = findViewById(R.id.vvGejala)
        tvGejala = findViewById(R.id.tvNamaGejala)

        val kodeGejala = intent.getStringExtra("symptomId") ?: ""
        if (kodeGejala.isNotEmpty()) {
            symptomsRef = FirebaseDatabase.getInstance().reference.child("symptoms").child(kodeGejala)
            storageRef = FirebaseStorage.getInstance().reference.child("symptoms")

            symptomsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val gejala = dataSnapshot.getValue(Symptoms::class.java)
                        gejala?.let {
                            tvGejala.text = it.symptom_name

                            // Load image from Firebase Database URL
                            if (!it.image.isNullOrEmpty()) {
                                val imageUrl = it.image // Assuming the URL is stored in the "image" field
                                Glide.with(this@UserDetailGejala)
                                    .load(imageUrl)
                                    .apply(RequestOptions().placeholder(R.drawable.default_image))
                                    .into(ivGejala)
                                ivGejala.visibility = View.VISIBLE
                            } else {
                                // If image is empty, set visibility to GONE
                                ivGejala.visibility = View.GONE
                            }

                            // Load video from Firebase Database URL
                            if (!it.video.isNullOrEmpty()) {
                                val videoUrl = it.video // Assuming the URL is stored in the "video" field
                                vvGejala.setVideoURI(Uri.parse(videoUrl))
                                vvGejala.visibility = View.VISIBLE
                            } else {
                                // If video is empty, set visibility to GONE
                                vvGejala.visibility = View.GONE
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors during Firebase access
                }
            })
        }

    }
}