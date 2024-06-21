package com.sistempakar.gouramydoctor.userUI

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Riwayat
import com.sistempakar.gouramydoctor.adminUI.Penyakit.DetailPenyakit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RiwayatDiagnosa : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RiwayatAdapter
    private lateinit var database: DatabaseReference
    private lateinit var currentUser: FirebaseAuth
    private val riwayatList: MutableList<Riwayat> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_diagnosa)

        recyclerView = findViewById(R.id.rvRiwayat)
        database = FirebaseDatabase.getInstance().reference.child("histories")
        currentUser = FirebaseAuth.getInstance()

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RiwayatAdapter()
        recyclerView.adapter = adapter

        val query = database.orderByChild("uid").equalTo(currentUser.currentUser?.uid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tempList: MutableList<Riwayat> = mutableListOf()

                for (snapshot in dataSnapshot.children) {
                    val riwayat = snapshot.getValue(Riwayat::class.java)
                    riwayat?.let {
                        tempList.add(it)
                    }
                }

                // Filter the list based on the currentUser's uid
                val filteredList = tempList.filter { it.uid == currentUser.currentUser?.uid }

                // Sort the filtered list based on the waktu (timestamp)
                val sortedList = filteredList.sortedByDescending { formatDate(it.waktu) }

                // Update the adapter's data list and notify the change
                riwayatList.clear()
                riwayatList.addAll(sortedList)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })

        adapter.setOnItemClickListener { position ->
            val riwayat = riwayatList[position]
            val waktu = riwayat.waktu.toString()
            val hasilGejala = riwayat.hasilGejala
            val uid = riwayat.uid
            val deskripsi = riwayat.deskripsi
            val solusi = riwayat.solusi
            val hasilCF = riwayat.hasilCF
            val namaPenyakit = riwayat.namaPenyakit
            val penyakitMendekati = riwayat.penyakitMendekati

            val intent = Intent(this@RiwayatDiagnosa, DetailRiwayat::class.java)
            intent.putExtra("waktu", waktu)
            intent.putExtra("uid", uid)
            intent.putExtra("deskripsi", deskripsi)
            intent.putExtra("solusi", solusi)
            intent.putExtra("hasilCF", hasilCF)
            intent.putExtra("namaPenyakit", namaPenyakit)
            intent.putExtra("hasilGejala", hasilGejala)
            intent.putExtra("penyakitMendekati", penyakitMendekati)
            startActivity(intent)
        }

        adapter.setOnDeleteClickListener { position ->
            val riwayat = riwayatList[position]
            val waktu = riwayat.waktu.toString()

            // Menghapus data dari Firebase berdasarkan waktu
            val queryDelete = database.orderByChild("waktu").equalTo(waktu)
            queryDelete.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun formatDate(waktu: String?): Long {
        if (waktu != null) {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy, HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse(waktu)
            return date?.time ?: 0L
        }
        return 0L
    }

    private inner class RiwayatAdapter : RecyclerView.Adapter<RiwayatAdapter.RiwayatViewHolder>() {

        private var onItemClickListener: ((Int) -> Unit)? = null
        private var onDeleteClickListener: ((Int) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.riwayat_item, parent, false)
            return RiwayatViewHolder(view)
        }

        override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) {
            val riwayat = riwayatList[position]
            holder.namaPenyakitTextView.text = riwayat.namaPenyakit
            holder.waktuTextView.text = riwayat.waktu?.toString()

            holder.deleteButton.setOnClickListener {
                onDeleteClickListener?.invoke(position)
            }

            holder.itemView.setOnClickListener {
                onItemClickListener?.invoke(position)
            }
        }

        override fun getItemCount(): Int {
            return riwayatList.size
        }

        inner class RiwayatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val namaPenyakitTextView: TextView = itemView.findViewById(R.id.riwayatNamaPenyakit)
            val waktuTextView: TextView = itemView.findViewById(R.id.riwayatWaktu)
            val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        }

        fun setOnItemClickListener(listener: (Int) -> Unit) {
            onItemClickListener = listener
        }

        fun setOnDeleteClickListener(listener: (Int) -> Unit) {
            onDeleteClickListener = listener
        }
    }
}