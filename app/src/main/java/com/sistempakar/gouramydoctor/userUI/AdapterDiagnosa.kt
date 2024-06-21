package com.sistempakar.gouramydoctor.userUI

import android.content.Context
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import androidx.core.util.set
import androidx.recyclerview.widget.RecyclerView
import com.sistempakar.gouramydoctor.Gejala
import com.sistempakar.gouramydoctor.R

class AdapterDiagnosa(private val context: Context,
                      private var gejalaList: List<Gejala>
) : RecyclerView.Adapter<AdapterDiagnosa.GejalaViewHolder>() {

    private val checkedStatusMap: SparseBooleanArray = SparseBooleanArray()
    private val spinnerSelectionMap: SparseArray<Int> = SparseArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GejalaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.diagnosa_item, parent, false)
        return GejalaViewHolder(view)
    }

    override fun onBindViewHolder(holder: GejalaViewHolder, position: Int) {
        val gejala = gejalaList[position]
        holder.checkBox.setOnLongClickListener(null)

        holder.checkBox.isChecked = checkedStatusMap.get(position, false)
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.gejalaTextView.text = gejala.symptom_name

        val keyakinanAdapter = ArrayAdapter.createFromResource(
            context,
            R.array.keyakinan_array,
            android.R.layout.simple_spinner_item
        )
        keyakinanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.keyakinanSpinner.adapter = keyakinanAdapter

        // Set posisi spinner dari pilihan sebelumnya (jika ada)
        val selectedPosition = spinnerSelectionMap.get(position, 0)
        holder.keyakinanSpinner.setSelection(selectedPosition)

        // Simpan posisi pilihan spinner saat ada perubahan
        holder.keyakinanSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    spinnerPosition: Int,
                    id: Long
                ) {
                    val adapterPosition = holder.adapterPosition
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        spinnerSelectionMap.put(adapterPosition, spinnerPosition)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Tidak melakukan apa-apa saat tidak ada yang dipilih
                }
            }

        // Listener untuk mengatur status checkbox di data model
        holder.checkBox.setOnClickListener {
            val position = holder.adapterPosition
            val isChecked = holder.checkBox.isChecked
            checkedStatusMap[position] = isChecked
        }
    }

    override fun getItemCount(): Int {
        return gejalaList.size
    }

    fun getCheckedStatus(position: Int): Boolean {
        return checkedStatusMap.get(position, false)
    }

    fun getSpinnerSelection(position: Int): Int {
        return spinnerSelectionMap.get(position, 0)
    }

    fun updateData(newGejalaList: MutableList<Gejala>) {
        gejalaList = newGejalaList
        notifyDataSetChanged()
    }

    fun getGejalaList(): List<Gejala> {
        return gejalaList
    }

    inner class GejalaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val gejalaTextView: TextView = itemView.findViewById(R.id.namaGejala)
        val keyakinanSpinner: Spinner = itemView.findViewById(R.id.spinnerCF)
    }
}

