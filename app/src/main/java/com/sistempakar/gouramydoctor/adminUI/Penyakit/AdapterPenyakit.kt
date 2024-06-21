package com.sistempakar.gouramydoctor.adminUI.Penyakit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sistempakar.gouramydoctor.Diseases
import com.sistempakar.gouramydoctor.R

class DiseaseAdapter(
    private val diseaseList: List<Diseases>,
    private val onEditClickListener: (Diseases) -> Unit,
    private val onDeleteClickListener: (Diseases) -> Unit,
    private val onItemClick: (Diseases) -> Unit
) : RecyclerView.Adapter<DiseaseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.data_gejala_dan_penyakit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val disease = diseaseList[position]
        holder.bind(disease)
    }

    override fun getItemCount(): Int {
        return diseaseList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDiseaseId: TextView = itemView.findViewById(R.id.kodeItem)
        private val tvDiseaseName: TextView = itemView.findViewById(R.id.deskripsiItem)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.editButton)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(disease: Diseases) {
            tvDiseaseId.text = disease.disease_id
            tvDiseaseName.text = disease.disease_name

            btnEdit.setOnClickListener {
                onEditClickListener.invoke(disease)
            }

            btnDelete.setOnClickListener {
                onDeleteClickListener.invoke(disease)
            }

            itemView.setOnClickListener {
                onItemClick.invoke(disease)
            }
        }
    }
}
