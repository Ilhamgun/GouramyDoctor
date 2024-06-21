package com.sistempakar.gouramydoctor.adminUI.Gejala

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Symptoms

class SymptomAdapter(private val symptoms: List<Symptoms>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<SymptomAdapter.SymptomViewHolder>() {

    inner class SymptomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val tvSymptomId: TextView = itemView.findViewById(R.id.kodeItem)
        val tvSymptomName: TextView = itemView.findViewById(R.id.deskripsiItem)
        val btnEdit: ImageButton = itemView.findViewById(R.id.editButton)
        val btnDelete: ImageButton = itemView.findViewById(R.id.deleteButton)

        init {
            btnEdit.setOnClickListener(this)
            btnDelete.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                when (v.id) {
                    R.id.editButton -> listener.onEditClick(position)
                    R.id.deleteButton -> listener.onDeleteClick(position)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onEditClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.data_gejala_dan_penyakit, parent, false)
        return SymptomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SymptomViewHolder, position: Int) {
        val currentItem = symptoms[position]
        holder.tvSymptomId.text = currentItem.symptom_id
        holder.tvSymptomName.text = currentItem.symptom_name
    }

    override fun getItemCount(): Int {
        return symptoms.size
    }
}

