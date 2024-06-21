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
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Rule
import com.sistempakar.gouramydoctor.Rules

class GejalaFragment : Fragment() {

    private lateinit var tvGejala: TextView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var diseaseId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gejala, container, false)
        tvGejala = view.findViewById(R.id.tvGejala)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        diseaseId = arguments?.getString("diseaseId") ?: ""
        databaseReference = FirebaseDatabase.getInstance().getReference("rules")

        loadSymptoms()
    }

    private fun loadSymptoms() {
        databaseReference.orderByChild("disease_id").equalTo(diseaseId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val symptomsList = mutableListOf<String>()
                    for (postSnapshot in snapshot.children) {
                        val symptomRules = postSnapshot.child("symptom_rules")
                        for (symptomRuleSnapshot in symptomRules.children) {
                            val symptomName = symptomRuleSnapshot.child("symptom_name").getValue(String::class.java)
                            if (symptomName != null) {
                                symptomsList.add(symptomName)
                            }
                        }
                    }
                    if (symptomsList.isNotEmpty()) {
                        tvGejala.text = symptomsList.joinToString("\n")
                    } else {
                        tvGejala.text = "No symptoms available for this disease."
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
    }

    companion object {
        fun newInstance(diseaseId: String): GejalaFragment {
            val fragment = GejalaFragment()
            val args = Bundle()
            args.putString("diseaseId", diseaseId)
            fragment.arguments = args
            return fragment
        }
    }
}