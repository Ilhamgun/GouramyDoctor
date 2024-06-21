package com.sistempakar.gouramydoctor.userUI

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.Diseases
import com.sistempakar.gouramydoctor.R


class SolusiFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_solusi, container, false)
        tvContent = view.findViewById(R.id.tvSolusi)
        loadSolusi()
        return view
    }

    private fun loadSolusi() {
        database.child(diseaseId).child("solution").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val solusi = snapshot.value as? String
                val convertContent =
                    solusi?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY) }
                tvContent.text = convertContent ?: ""
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    companion object {
        private const val ARG_DISEASE_ID = "diseaseId"

        fun newInstance(diseaseId: String): SolusiFragment {
            val fragment = SolusiFragment()
            val args = Bundle()
            args.putString(ARG_DISEASE_ID, diseaseId)
            fragment.arguments = args
            return fragment
        }
    }
}