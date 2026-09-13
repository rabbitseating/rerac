package com.example.mapbox

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class RiskActivity : AppCompatActivity() {

    private val httpClient = OkHttpClient()

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_risk)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@RiskActivity)
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadRecentRisks(recyclerView)
    }

    private fun loadRecentRisks(recyclerView: RecyclerView) {
        val request = Request.Builder()
            .url("${BuildConfig.BACKEND_BASE_URL}/last5risks")
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Unable to load recent risks", e)
                showLoadError()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.w(TAG, "Recent risk request returned HTTP ${response.code}")
                        showLoadError()
                        return
                    }

                    try {
                        val jsonArray = JSONArray(response.body?.string().orEmpty())
                        val risks = buildList {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.optJSONObject(i) ?: continue
                                add(
                                    RiskData(
                                        time = item.optString("time_val", ""),
                                        risk8 = item.optInt("blk8", 0),
                                        risk23 = item.optInt("blk23", 0),
                                        risk51 = item.optInt("blk51", 0),
                                        risk72 = item.optInt("blk72", 0),
                                        risk73 = item.optInt("blk73", 0),
                                        riskSIT = item.optInt("blkSIT", 0)
                                    )
                                )
                            }
                        }

                        runOnUiThread {
                            recyclerView.adapter = RiskDataAdapter(risks)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Unable to parse recent risk response", e)
                        showLoadError()
                    }
                }
            }
        })
    }

    private fun showLoadError() {
        runOnUiThread {
            if (!isFinishing && !isDestroyed) {
                Toast.makeText(this, "Unable to load recent risk data.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "RiskActivity"
    }
}
