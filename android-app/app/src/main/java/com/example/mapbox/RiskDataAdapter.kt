package com.example.mapbox

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class RiskDataAdapter(private val riskList: List<RiskData>) :
    RecyclerView.Adapter<RiskDataAdapter.RiskDataViewHolder>() {

    class RiskDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        val textViewRisk8: TextView = itemView.findViewById(R.id.textViewRisk8)
        val textViewRisk23: TextView = itemView.findViewById(R.id.textViewRisk23)
        val textViewRisk51: TextView = itemView.findViewById(R.id.textViewRisk51)
        val textViewRisk72: TextView = itemView.findViewById(R.id.textViewRisk72)
        val textViewRisk73: TextView = itemView.findViewById(R.id.textViewRisk73)
        val textViewRiskSIT: TextView = itemView.findViewById(R.id.textViewRiskSIT)

        fun setRiskColor(riskData: RiskData) {
            textViewRisk8.setTextColor(getRiskColor(riskData.risk8))
            textViewRisk23.setTextColor(getRiskColor(riskData.risk23))
            textViewRisk51.setTextColor(getRiskColor(riskData.risk51))
            textViewRisk72.setTextColor(getRiskColor(riskData.risk72))
            textViewRisk73.setTextColor(getRiskColor(riskData.risk73))
            textViewRiskSIT.setTextColor(getRiskColor(riskData.riskSIT))
        }

        private fun getRiskColor(riskValue: Int): Int {
            return when {
                riskValue < 3 -> ContextCompat.getColor(itemView.context, R.color.colorNormalRisk)
                riskValue < 7 -> ContextCompat.getColor(itemView.context, R.color.colorMediumRisk)
                else -> ContextCompat.getColor(itemView.context, R.color.colorHighRisk)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiskDataViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_risk_data, parent, false)
        return RiskDataViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RiskDataViewHolder, position: Int) {
        val risk = riskList[position]
        holder.textViewRisk8.text = "Block 8: ${risk.risk8}"
        holder.textViewRisk23.text = "Block 23: ${risk.risk23}"
        holder.textViewRisk51.text = "Block 51: ${risk.risk51}"
        holder.textViewRisk72.text = "Block 72: ${risk.risk72}"
        holder.textViewRisk73.text = "Block 73: ${risk.risk73}"
        holder.textViewRiskSIT.text = "SIT: ${risk.riskSIT}"
        holder.textViewTime.text = "Time: ${risk.time}"
        holder.setRiskColor(risk)
    }

    override fun getItemCount(): Int = riskList.size
}
