package com.example.mapbox

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class StatisticsActivity : AppCompatActivity() {

    private val httpClient = OkHttpClient()

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val charts = listOf(
            findViewById<BarChart>(R.id.barChart) to "risk8",
            findViewById<BarChart>(R.id.barChart1) to "risk23",
            findViewById<BarChart>(R.id.barChart2) to "risk51",
            findViewById<BarChart>(R.id.barChart3) to "risk72",
            findViewById<BarChart>(R.id.barChart4) to "risk73",
            findViewById<BarChart>(R.id.barChart5) to "riskSIT"
        )

        charts.forEach { (chart, endpoint) ->
            fetchChartData(chart, endpoint)
        }
    }

    private fun fetchChartData(chart: BarChart, endpoint: String) {
        lifecycleScope.launch {
            try {
                val entries = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("${BuildConfig.BACKEND_BASE_URL}/$endpoint")
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw IllegalStateException("$endpoint returned HTTP ${response.code}")
                        }
                        parseRiskData(response.body?.string())
                    }
                }

                updateBarChart(chart, entries)
            } catch (e: Exception) {
                Log.e(TAG, "Unable to load $endpoint statistics", e)
                Toast.makeText(
                    this@StatisticsActivity,
                    "Unable to load risk statistics.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun parseRiskData(jsonData: String?): ArrayList<BarEntry> {
        val riskValues = FloatArray(HOUR_COUNT)

        if (!jsonData.isNullOrBlank()) {
            val jsonArray = JSONArray(jsonData)
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(i) ?: continue
                val hour = item.optInt("hour", -1)
                if (hour !in START_HOUR..END_HOUR) {
                    Log.w(TAG, "Ignoring out-of-range statistics hour: $hour")
                    continue
                }
                val risk = item.optDouble("avgrisk", 0.0).toFloat()
                riskValues[hour - START_HOUR] = risk
            }
        }

        return ArrayList<BarEntry>().apply {
            riskValues.forEachIndexed { index, value ->
                add(BarEntry(index.toFloat(), value))
            }
        }
    }

    private fun updateBarChart(chart: BarChart, entries: ArrayList<BarEntry>) {
        val dataSet = BarDataSet(entries, "Average risk").apply {
            color = ContextCompat.getColor(this@StatisticsActivity, R.color.blue)
            setDrawValues(true)
            valueFormatter = RiskValueFormatter()
            isHighlightEnabled = false
        }

        chart.data = BarData(dataSet)
        chart.description = null
        chart.axisRight.isEnabled = false
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = 10f
        chart.setNoDataText("No risk data available")

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(generateXAxisLabels())
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            labelCount = HOUR_COUNT
            granularity = 1f
        }

        chart.invalidate()
    }

    private fun generateXAxisLabels(): List<String> =
        (START_HOUR..END_HOUR).map { hour -> String.format("%02d", hour) }

    private class RiskValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String = String.format("%.2f", value)
    }

    companion object {
        private const val TAG = "StatisticsActivity"
        private const val START_HOUR = 7
        private const val END_HOUR = 19
        private const val HOUR_COUNT = END_HOUR - START_HOUR + 1
    }
}
