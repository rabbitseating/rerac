package com.example.mapbox

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, TurnByTurnActivity::class.java)
        startActivity(intent)
        finish() // Optional: Finish the CameraActivity to remove it from the back stack
        return true
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_risk)
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerview.layoutManager = LinearLayoutManager(this)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getlast5(recyclerview)
    }

    private fun getlast5(recyclerview: RecyclerView?) {

        val client = OkHttpClient()
        val getRequest: Request = Request.Builder()
            .url("${BuildConfig.BACKEND_BASE_URL}/last5risks")
            .build()

        client.newCall(getRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                try{
                Log.d("RiskActivity", "onResponse called")
                val responseData = response.body?.string() ?: ""
                val jsonArray = JSONArray(responseData)
                val riskList = mutableListOf<RiskData>()
                Log.d("RiskActivity", "Response Data: $responseData")
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val time = jsonObject.optString("time_val", "")
                    val risk8Value = jsonObject.optInt("blk8", 0)
                    val risk23Value = jsonObject.optInt("blk23", 0)
                    val risk51Value = jsonObject.optInt("blk51", 0)
                    val risk72Value = jsonObject.optInt("blk72", 0)
                    val risk73Value = jsonObject.optInt("blk73", 0)
                    val riskSITValue = jsonObject.optInt("blkSIT", 0)

                    val riskData = RiskData(
                        time,risk8Value, risk23Value, risk51Value, risk72Value, risk73Value, riskSITValue
                    )
                    riskList.add(riskData)

                }

                runOnUiThread {
                    val adapter = RiskDataAdapter(riskList)
                    recyclerview?.adapter = adapter
                }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        })
    }

}