package com.sistempakar.gouramydoctor

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable


data class Gejala(
    val symptom_id: String = "",
    val symptom_name: String = "",
    val cf_value: Double = 0.0,
    var isChecked: Boolean = false,
    var spinnerSelection: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(symptom_id)
        parcel.writeString(symptom_name)
        parcel.writeDouble(cf_value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Gejala> {
        override fun createFromParcel(parcel: Parcel): Gejala {
            return Gejala(parcel)
        }

        override fun newArray(size: Int): Array<Gejala?> {
            return arrayOfNulls(size)
        }
    }
}

data class Users(
    var uid : String? = null,
    var username : String? = null,
    var email : String? = null,
    var password : String? = null,
    var role : String? = null,
    var address : String? = null,
    var phoneNumber : String? = null,
    var imageUrl: String? = null
)

data class Diseases(
    var disease_id : String? = null,
    var disease_name : String? = null,
    var description : String? = null,
    var solution : String? = null
)

data class Symptoms(
    var symptom_id: String? = null,
    var symptom_name: String? = null,
    var image: String? = null,
    var video: String? = null
)

data class Rules(
    var rule_id: String? = null,
    var disease_id: String? = null,
    var disease_name: String? = null,
    var symptom_id: String? = null,
    var symptom_name: String? = null,
    var cf_value: String? = null
)

data class SymptomRule(
    val cf_value: String? = null,
    val symptom_id: String? = null,
    val symptom_name: String? = null
)

data class Rule(
    val disease_id: String? = null,
    val disease_name: String? = null,
    val symptom_rules: List<SymptomRule>? = null
)

data class Riwayat(
    val deskripsi: String? = null,
    val hasilCF: String? = null,
    val namaPenyakit: String? = null,
    val solusi: String? = null,
    val uid: String? = null,
    val waktu: String? = null,
    val hasilGejala: String? = null,
    val penyakitMendekati: String? = null
)

