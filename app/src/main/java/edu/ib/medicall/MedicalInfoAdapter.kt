package edu.ib.medicall

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicalInfoAdapter(private var medicalInfoList: List<Pair<String, String>>) :
    RecyclerView.Adapter<MedicalInfoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvKey: TextView = view.findViewById(R.id.tv_key)
        val tvValue: TextView = view.findViewById(R.id.tv_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medical_info, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (key, value) = medicalInfoList[position]
        holder.tvKey.text = key
        holder.tvValue.text = value
    }

    override fun getItemCount(): Int {
        return medicalInfoList.size
    }

    fun updateData(newList: List<Pair<String, String>>) {
        medicalInfoList = newList
        notifyDataSetChanged()
    }
}
