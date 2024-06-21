package com.sistempakar.gouramydoctor.userUI

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Users
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

class UbahAkun : AppCompatActivity() {
    private var filePath: Uri? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var currentUserUid: String

    private lateinit var profileImageView: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var saveButton: Button

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            filePath = uri
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                profileImageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubah_akun)

        auth = FirebaseAuth.getInstance()
        currentUserUid = auth.currentUser?.uid ?: ""
        database = FirebaseDatabase.getInstance().reference.child("users").child(currentUserUid)
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        profileImageView = findViewById(R.id.uploadImage)
        usernameEditText = findViewById(R.id.uploadUsername)
        emailEditText = findViewById(R.id.uploadEmail)
        addressEditText = findViewById(R.id.uploadAlamat)
        phoneNumberEditText = findViewById(R.id.uploadPhone)
        saveButton = findViewById(R.id.btnUpload)

        emailEditText.isEnabled = false

        profileImageView.setOnClickListener {
            openGallery()
        }

        loadUserData()

        saveButton.setOnClickListener {
            val builder = AlertDialog.Builder(this@UbahAkun)
            builder.setCancelable(false)
            builder.setView(R.layout.progress_layout)
            val dialog = builder.create()
            dialog.show()

            uploadImage()

            val intent = Intent(this@UbahAkun, Profil::class.java)
            startActivity(intent)
            dialog.dismiss()
            finish()
        }
    }

    private fun loadUserData() {
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(Users::class.java)
                user?.let {
                    usernameEditText.setText(it.username)
                    emailEditText.setText(it.email)
                    addressEditText.setText(it.address)
                    phoneNumberEditText.setText(it.phoneNumber)
                    // Load profile image using the URL stored in the database
                    // Uncomment the following line if you have 'imageUrl' field in the User class.
                    // Glide.with(this).load(it.imageUrl).into(profileImageView)
                }
            }
        }
    }

    private fun openGallery() {
        getContent.launch("image/*")
    }

    private fun uploadImage() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // User is authenticated, proceed with the upload
            if (filePath != null) {
                val ref = storageReference.child("images/${UUID.randomUUID()}")
                val bitmap = (profileImageView.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val data = baos.toByteArray()

                ref.putBytes(data)
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val imageUrl = task.result.toString()
                                saveUserDataWithImage(imageUrl)
                            } else {
                                Toast.makeText(this, "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show()
                    }
            } else {
                saveUserData()
            }
        } else {
            // User is not authenticated, prompt them to sign in or sign up
            Toast.makeText(this, "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()

        // Create a map of the fields to be updated
        val userUpdates = mapOf(
            "username" to username,
            "address" to address,
            "phoneNumber" to phoneNumber
        )

        // Step 3: Update the existing data in the user's reference
        database.updateChildren(userUpdates).addOnSuccessListener {
            Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserDataWithImage(imageUrl: String) {
        val username = usernameEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()

        // Create a map of the fields to be updated
        val userUpdates = mapOf(
            "username" to username,
            "address" to address,
            "phoneNumber" to phoneNumber,
            "imageUrl" to imageUrl
        )

        // Step 3: Update the existing data in the user's reference
        database.updateChildren(userUpdates).addOnSuccessListener {
            Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("filePath", filePath)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        filePath = savedInstanceState.getParcelable("filePath")
        if (filePath != null) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                profileImageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}