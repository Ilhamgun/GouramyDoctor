package com.sistempakar.gouramydoctor.adminUI.Aturan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sistempakar.gouramydoctor.R
import com.sistempakar.gouramydoctor.Rules

class RulesAdapter(
    private val ruleList: List<Rules>,
    private val onEditClickListener: (Rules) -> Unit,
    private val onDeleteClickListener: (Rules) -> Unit
) : RecyclerView.Adapter<RulesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RulesAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.data_rule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RulesAdapter.ViewHolder, position: Int) {
        val rule = ruleList[position]
        holder.bind(rule)
    }

    override fun getItemCount(): Int {
        return ruleList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRuleId: TextView = itemView.findViewById(R.id.kodeRule)
        private val tvDiseaseId: TextView = itemView.findViewById(R.id.kodePenyakit)
        private val tvDiseaseName: TextView = itemView.findViewById(R.id.namaPenyakit)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.editButton)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(rules: Rules) {
            tvRuleId.text = rules.rule_id
            tvDiseaseId.text = rules.disease_id
            tvDiseaseName.text = rules.disease_name


            btnEdit.setOnClickListener {
                onEditClickListener.invoke(rules)
            }

            btnDelete.setOnClickListener {
                onDeleteClickListener.invoke(rules)
            }
        }
    }
}