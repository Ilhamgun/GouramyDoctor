package com.sistempakar.gouramydoctor.userUI

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sistempakar.gouramydoctor.R

class UserDetailPenyakit : AppCompatActivity() {
    private lateinit var tvDiseaseName: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private lateinit var diseaseId: String

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail_penyakit)
        supportActionBar?.hide()

        database = FirebaseDatabase.getInstance().reference.child("diseases")

        tvDiseaseName = findViewById(R.id.tvNamaPenyakit)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        diseaseId = intent.getStringExtra("diseaseId") ?: ""
        loadDiseaseData()
        setupViewPager()
    }

    private fun loadDiseaseData() {
        database.child(diseaseId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val diseaseName = snapshot.child("disease_name").value as? String
                tvDiseaseName.text = diseaseName ?: ""
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun setupViewPager() {
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 3

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> DeskripsiFragment.newInstance(diseaseId)
                    1 -> GejalaFragment.newInstance(diseaseId)
                    2 -> SolusiFragment.newInstance(diseaseId)
                    else -> throw IllegalArgumentException("Invalid position")
                }
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Deskripsi"
                1 -> "Gejala"
                2 -> "Solusi"
                else -> ""
            }
        }.attach()
    }
}