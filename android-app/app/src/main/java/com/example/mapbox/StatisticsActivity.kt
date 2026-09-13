package com.example.mapbox

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class StatisticsActivity : AppCompatActivity() {

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, TurnByTurnActivity::class.java)
        startActivity(intent)
        finish() // Optional: Finish the CameraActivity to remove it from the back stack
        return true
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize the BARChart
        val barChart: BarChart = findViewById(R.id.barChart)
        val barChart1: BarChart = findViewById(R.id.barChart1)
        val barChart2: BarChart = findViewById(R.id.barChart2)
        val barChart3: BarChart = findViewById(R.id.barChart3)
        val barChart4: BarChart = findViewById(R.id.barChart4)
        val barChart5: BarChart = findViewById(R.id.barChart5)



        // Fetch data from Node.js using OkHttp
        fetchDataFromServer(barChart)
        fetchDataFromServer23(barChart1)
        fetchDataFromServer51(barChart2)
        fetchDataFromServer72(barChart3)
        fetchDataFromServer73(barChart4)
        fetchDataFromServerSIT(barChart5)
    }
//BLK 8
    private fun fetchDataFromServer(barChart: BarChart) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("${BuildConfig.BACKEND_BASE_URL}/risk8")
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                // Parse the JSON data
                val entries = parseJsonData(responseData)

                // Update the BarChart with the data and x-axis labels
                updateBarChart(barChart, entries)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun parseJsonData(jsonData: String?): ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()

        // Initialize an array to hold the risk values for each hour from 7 to 19
        val riskValues = Array(13) { 0f }

        jsonData?.let {
            val jsonArray = JSONArray(it)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val riskValue = jsonObject.getDouble("avgrisk").toFloat() // Parse as float
                val time = jsonObject.getString("hour").toInt()

                // Store the risk value in the corresponding index of the riskValues array
                // For example, if the hour is 8, the risk value will be stored at index 1 (8-7)
                riskValues[time - 7] = riskValue
            }
        }

        // Populate the entries list with the risk values and hours from 7 to 19
        for (i in 0 until 13) {
            val riskValue = riskValues[i]
            val timeFloat = i.toFloat() // Use i as the hour index (7 to 19)
            entries.add(BarEntry(timeFloat, riskValue))
        }

        return entries
    }

    private fun updateBarChart(barChart: BarChart, entries: ArrayList<BarEntry>) {
        val dataSet = BarDataSet(entries, "Risk Values")
        val dataSets: ArrayList<IBarDataSet> = ArrayList()
        dataSets.add(dataSet)

        val barData = BarData(dataSets)
        barChart.data = barData
        barChart.description=null

        // Customize the appearance of the chart if needed
        // For example:
        dataSet.color = resources.getColor(R.color.blue)
        dataSet.setDrawValues(true) // Enable displaying values above the bars
        dataSet.valueFormatter = MyValueFormatter() // Set a custom value formatter for the data values
        dataSet.isHighlightEnabled = false // Disable highlighting bars when selected

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(generateXAxisLabels())
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = 13 // Set the number of labels to be displayed (13 for 7 am to 7 pm)
        xAxis.granularity = 1f

        val yAxisLeft = barChart.axisLeft
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 10f
        barChart.axisRight.isEnabled = false
        // Refresh the chart
        barChart.invalidate()
    }

    private fun generateXAxisLabels(): List<String> {
        val labels = mutableListOf<String>()
        for (hour in 7 until 20) {
            labels.add(String.format("%02d", hour))
        }
        return labels
    }

    class MyValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return String.format("%.2f", value) // Format the value with 2 decimal places
        }
    }

//blk 23
private fun fetchDataFromServer23(barChart: BarChart) {
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("${BuildConfig.BACKEND_BASE_URL}/risk23")
                .build()

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()

            // Parse the JSON data
            val entries = parseJsonData23(responseData)

            // Update the BarChart with the data and x-axis labels
            updateBarChart23(barChart, entries)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

    private fun parseJsonData23(jsonData: String?): ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()

        // Initialize an array to hold the risk values for each hour from 7 to 19
        val riskValues = Array(13) { 0f }

        jsonData?.let {
            val jsonArray = JSONArray(it)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val riskValue = jsonObject.getDouble("avgrisk").toFloat() // Parse as float
                val time = jsonObject.getString("hour").toInt()

                // Store the risk value in the corresponding index of the riskValues array
                // For example, if the hour is 8, the risk value will be stored at index 1 (8-7)
                riskValues[time - 7] = riskValue
            }
        }

        // Populate the entries list with the risk values and hours from 7 to 19
        for (i in 0 until 13) {
            val riskValue = riskValues[i]
            val timeFloat = i.toFloat() // Use i as the hour index (7 to 19)
            entries.add(BarEntry(timeFloat, riskValue))
        }

        return entries
    }

    private fun updateBarChart23(barChart: BarChart, entries: ArrayList<BarEntry>) {
        val dataSet = BarDataSet(entries, "Risk Values")
        val dataSets: ArrayList<IBarDataSet> = ArrayList()
        dataSets.add(dataSet)

        val barData = BarData(dataSets)
        barChart.data = barData
        barChart.description=null
        // Customize the appearance of the chart if needed
        // For example:
        dataSet.color = resources.getColor(R.color.blue)
        dataSet.setDrawValues(true) // Enable displaying values above the bars
        dataSet.valueFormatter = MyValueFormatter() // Set a custom value formatter for the data values
        dataSet.isHighlightEnabled = false // Disable highlighting bars when selected

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(generateXAxisLabels())
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = 13 // Set the number of labels to be displayed (13 for 7 am to 7 pm)
        xAxis.granularity = 1f

        val yAxisLeft = barChart.axisLeft
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 10f
        barChart.axisRight.isEnabled = false
        // Refresh the chart
        barChart.invalidate()
    }
    //block 51

    private fun fetchDataFromServer51(barChart: BarChart) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("${BuildConfig.BACKEND_BASE_URL}/risk51")
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                // Parse the JSON data
                val entries = parseJsonData51(responseData)

                // Update the BarChart with the data and x-axis labels
                updateBarChart51(barChart, entries)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun parseJsonData51(jsonData: String?): ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()

        // Initialize an array to hold the risk values for each hour from 7 to 19
        val riskValues = Array(13) { 0f }

        jsonData?.let {
            val jsonArray = JSONArray(it)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val riskValue = jsonObject.getDouble("avgrisk").toFloat() // Parse as float
                val time = jsonObject.getString("hour").toInt()

                // Store the risk value in the corresponding index of the riskValues array
                // For example, if the hour is 8, the risk value will be stored at index 1 (8-7)
                riskValues[time - 7] = riskValue
            }
        }

        // Populate the entries list with the risk values and hours from 7 to 19
        for (i in 0 until 13) {
            val riskValue = riskValues[i]
            val timeFloat = i.toFloat() // Use i as the hour index (7 to 19)
            entries.add(BarEntry(timeFloat, riskValue))
        }

        return entries
    }

    private fun updateBarChart51(barChart: BarChart, entries: ArrayList<BarEntry>) {
        val dataSet = BarDataSet(entries, "Risk Values")
        val dataSets: ArrayList<IBarDataSet> = ArrayList()
        dataSets.add(dataSet)

        val barData = BarData(dataSets)
        barChart.data = barData
        barChart.description=null
        // Customize the appearance of the chart if needed
        // For example:
        dataSet.color = resources.getColor(R.color.blue)
        dataSet.setDrawValues(true) // Enable displaying values above the bars
        dataSet.valueFormatter = MyValueFormatter() // Set a custom value formatter for the data values
        dataSet.isHighlightEnabled = false // Disable highlighting bars when selected

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(generateXAxisLabels())
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = 13 // Set the number of labels to be displayed (13 for 7 am to 7 pm)
        xAxis.granularity = 1f

        val yAxisLeft = barChart.axisLeft
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 10f
        barChart.axisRight.isEnabled = false
        // Refresh the chart
        barChart.invalidate()
    }
    //blk 72

    private fun fetchDataFromServer72(barChart: BarChart) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("${BuildConfig.BACKEND_BASE_URL}/risk72")
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                // Parse the JSON data
                val entries = parseJsonData72(responseData)

                // Update the BarChart with the data and x-axis labels
                updateBarChart72(barChart, entries)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun parseJsonData72(jsonData: String?): ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()

        // Initialize an array to hold the risk values for each hour from 7 to 19
        val riskValues = Array(13) { 0f }

        jsonData?.let {
            val jsonArray = JSONArray(it)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val riskValue = jsonObject.getDouble("avgrisk").toFloat() // Parse as float
                val time = jsonObject.getString("hour").toInt()

                // Store the risk value in the corresponding index of the riskValues array
                // For example, if the hour is 8, the risk value will be stored at index 1 (8-7)
                riskValues[time - 7] = riskValue
            }
        }

        // Populate the entries list with the risk values and hours from 7 to 19
        for (i in 0 until 13) {
            val riskValue = riskValues[i]
            val timeFloat = i.toFloat() // Use i as the hour index (7 to 19)
            entries.add(BarEntry(timeFloat, riskValue))
        }

        return entries
    }

    private fun updateBarChart72(barChart: BarChart, entries: ArrayList<BarEntry>) {
        val dataSet = BarDataSet(entries, "Risk Values")
        val dataSets: ArrayList<IBarDataSet> = ArrayList()
        dataSets.add(dataSet)

        val barData = BarData(dataSets)
        barChart.data = barData
        barChart.description=null
        // Customize the appearance of the chart if needed
        // For example:
        dataSet.color = resources.getColor(R.color.blue)
        dataSet.setDrawValues(true) // Enable displaying values above the bars
        dataSet.valueFormatter = MyValueFormatter() // Set a custom value formatter for the data values
        dataSet.isHighlightEnabled = false // Disable highlighting bars when selected

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(generateXAxisLabels())
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = 13 // Set the number of labels to be displayed (13 for 7 am to 7 pm)
        xAxis.granularity = 1f

        val yAxisLeft = barChart.axisLeft
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 10f
        barChart.axisRight.isEnabled = false
        // Refresh the chart
        barChart.invalidate()
    }

    //block 73
    private fun fetchDataFromServer73(barChart: BarChart) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("${BuildConfig.BACKEND_BASE_URL}/risk73")
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                // Parse the JSON data
                val entries = parseJsonData73(responseData)

                // Update the BarChart with the data and x-axis labels
                updateBarChart73(barChart, entries)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun parseJsonData73(jsonData: String?): ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()

        // Initialize an array to hold the risk values for each hour from 7 to 19
        val riskValues = Array(13) { 0f }

        jsonData?.let {
            val jsonArray = JSONArray(it)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val riskValue = jsonObject.getDouble("avgrisk").toFloat() // Parse as float
                val time = jsonObject.getString("hour").toInt()

                // Store the risk value in the corresponding index of the riskValues array
                // For example, if the hour is 8, the risk value will be stored at index 1 (8-7)
                riskValues[time - 7] = riskValue
            }
        }

        // Populate the entries list with the risk values and hours from 7 to 19
        for (i in 0 until 13) {
            val riskValue = riskValues[i]
            val timeFloat = i.toFloat() // Use i as the hour index (7 to 19)
            entries.add(BarEntry(timeFloat, riskValue))
        }

        return entries
    }

    private fun updateBarChart73(barChart: BarChart, entries: ArrayList<BarEntry>) {
        val dataSet = BarDataSet(entries, "Risk Values")
        val dataSets: ArrayList<IBarDataSet> = ArrayList()
        dataSets.add(dataSet)

        val barData = BarData(dataSets)
        barChart.data = barData
        barChart.description=null
        // Customize the appearance of the chart if needed
        // For example:
        dataSet.color = resources.getColor(R.color.blue)
        dataSet.setDrawValues(true) // Enable displaying values above the bars
        dataSet.valueFormatter = MyValueFormatter() // Set a custom value formatter for the data values
        dataSet.isHighlightEnabled = false // Disable highlighting bars when selected

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(generateXAxisLabels())
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = 13 // Set the number of labels to be displayed (13 for 7 am to 7 pm)
        xAxis.granularity = 1f

        val yAxisLeft = barChart.axisLeft
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 10f
        barChart.axisRight.isEnabled = false
        // Refresh the chart
        barChart.invalidate()
    }

    //SIT
    private fun fetchDataFromServerSIT(barChart: BarChart) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("${BuildConfig.BACKEND_BASE_URL}/riskSIT")
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                // Parse the JSON data
                val entries = parseJsonDataSIT(responseData)

                // Update the BarChart with the data and x-axis labels
                updateBarChartSIT(barChart, entries)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun parseJsonDataSIT(jsonData: String?): ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()

        // Initialize an array to hold the risk values for each hour from 7 to 19
        val riskValues = Array(13) { 0f }

        jsonData?.let {
            val jsonArray = JSONArray(it)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val riskValue = jsonObject.getDouble("avgrisk").toFloat() // Parse as float
                val time = jsonObject.getString("hour").toInt()

                // Store the risk value in the corresponding index of the riskValues array
                // For example, if the hour is 8, the risk value will be stored at index 1 (8-7)
                riskValues[time - 7] = riskValue
            }
        }

        // Populate the entries list with the risk values and hours from 7 to 19
        for (i in 0 until 13) {
            val riskValue = riskValues[i]
            val timeFloat = i.toFloat() // Use i as the hour index (7 to 19)
            entries.add(BarEntry(timeFloat, riskValue))
        }

        return entries
    }

    private fun updateBarChartSIT(barChart: BarChart, entries: ArrayList<BarEntry>) {
        val dataSet = BarDataSet(entries, "Risk Values")
        val dataSets: ArrayList<IBarDataSet> = ArrayList()
        dataSets.add(dataSet)

        val barData = BarData(dataSets)
        barChart.data = barData
        barChart.description=null
        // Customize the appearance of the chart if needed
        // For example:
        dataSet.color = resources.getColor(R.color.blue)
        dataSet.setDrawValues(true) // Enable displaying values above the bars
        dataSet.valueFormatter = MyValueFormatter() // Set a custom value formatter for the data values
        dataSet.isHighlightEnabled = false // Disable highlighting bars when selected

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(generateXAxisLabels())
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = 13 // Set the number of labels to be displayed (13 for 7 am to 7 pm)
        xAxis.granularity = 1f

        val yAxisLeft = barChart.axisLeft
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 10f
        barChart.axisRight.isEnabled = false
        // Refresh the chart
        barChart.invalidate()
    }
}

