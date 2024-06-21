package com.sistempakar.gouramydoctor.userUI

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.Diseases
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.adminUI.Penyakit.DetailPenyakit

class DeskripsiFragment : Fragment() {

    private lateinit var tvContent: TextView
    private lateinit var database: DatabaseReference

    private var diseaseId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            diseaseId = it.getString(ARG_DISEASE_ID, "")
        }
        database = FirebaseDatabase.getInstance().reference.child("diseases")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_deskripsi, container, false)
        tvContent = view.findViewById(R.id.tvDeskripsi)
        loadDeskripsi()
        return view
    }

    private fun loadDeskripsi() {
        database.child(diseaseId).child("description").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deskripsi = snapshot.value as? String
                tvContent.text = deskripsi ?: ""
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    companion object {
        private const val ARG_DISEASE_ID = "diseaseId"

        fun newInstance(diseaseId: String): DeskripsiFragment {
            val fragment = DeskripsiFragment()
            val args = Bundle()
            args.putString(ARG_DISEASE_ID, diseaseId)
            fragment.arguments = args
            return fragment
        }
    }
}