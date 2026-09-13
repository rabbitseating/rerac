from pathlib import Path
import re

PATH = Path("android-app/app/src/main/java/com/example/mapbox/TurnByTurnActivity.kt")
text = PATH.read_text()

if "private val httpClient = OkHttpClient()" in text:
    print("TurnByTurnActivity cleanup already applied")
    raise SystemExit(0)


def replace_once(old: str, new: str, label: str):
    global text
    if old not in text:
        raise RuntimeError(f"Could not find expected source block: {label}")
    text = text.replace(old, new, 1)


replace_once("import android.os.Handler\n", "import android.os.Handler\nimport android.os.Looper\n", "Looper import")
text = text.replace("import kotlinx.coroutines.GlobalScope\n", "")
replace_once("import kotlinx.coroutines.launch\n", "import kotlinx.coroutines.launch\nimport kotlinx.coroutines.withContext\n", "withContext import")
text = text.replace("import com.mapbox.navigation.core.replay.MapboxReplayer\n", "")
text = text.replace("import com.mapbox.navigation.core.replay.ReplayLocationEngine\n", "")
text = text.replace("import com.mapbox.navigation.core.replay.route.ReplayProgressObserver\n", "")
text = text.replace("import com.mapbox.navigation.core.replay.route.ReplayRouteMapper\n", "")

replace_once(
    "    private var soundtoggle = true\n",
    "    private var soundtoggle = true\n"
    "    private val httpClient = OkHttpClient()\n"
    "    private var lastGeofenceId: String? = null\n"
    "    private var lastGeofenceZone: String? = null\n",
    "shared client",
)

text = re.sub(
    r"\n    /\*\*\n     \* Debug tool used to play, pause and seek route progress events.*?private val replayProgressObserver = ReplayProgressObserver\(mapboxReplayer\)\n",
    "\n",
    text,
    flags=re.S,
)
text = text.replace("                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)\n", "")
text = text.replace("                mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)\n", "")

replace_once(
    """        val barChart1: BarChart = findViewById(R.id.ONClickbarChart1)
        fetchDataFromServer23(barChart1)//blk23 graph
        val barChart8: BarChart = findViewById(R.id.ONClickbarChart8)
        fetchDataFromServer(barChart8)//blk 8 graph
        val barChartSIT: BarChart = findViewById(R.id.ONClickbarChartSIT)
        fetchDataFromServerSIT(barChartSIT)//blkSIT graph
        val barChart72: BarChart = findViewById(R.id.ONClickbarChart72)
        fetchDataFromServer72(barChart72)//blk 72 graph
        val barChart73: BarChart = findViewById(R.id.ONClickbarChart73)
        fetchDataFromServer73(barChart73)//blk 73 graph
        val barChart51: BarChart = findViewById(R.id.ONClickbarChart51)
        fetchDataFromServer51(barChart51)//blk 51 graph
""",
    """        val barChart1: BarChart = findViewById(R.id.ONClickbarChart1)
        fetchChartData(barChart1, "risk23")
        val barChart8: BarChart = findViewById(R.id.ONClickbarChart8)
        fetchChartData(barChart8, "risk8")
        val barChartSIT: BarChart = findViewById(R.id.ONClickbarChartSIT)
        fetchChartData(barChartSIT, "riskSIT")
        val barChart72: BarChart = findViewById(R.id.ONClickbarChart72)
        fetchChartData(barChart72, "risk72")
        val barChart73: BarChart = findViewById(R.id.ONClickbarChart73)
        fetchChartData(barChart73, "risk73")
        val barChart51: BarChart = findViewById(R.id.ONClickbarChart51)
        fetchChartData(barChart51, "risk51")
""",
    "chart calls",
)

replace_once(
    """        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {""",
    """        val request = Request.Builder()
            .url(url)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {""",
    "weather client",
)

weather_old = '''            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val weatherResponse = JSONObject(responseData)

                val mainData = weatherResponse.getJSONObject("main")
                val temperature = mainData.getDouble("temp")
                val humidity = mainData.getInt("humidity")

                val weatherArray = weatherResponse.getJSONArray("weather")
                val weatherObject = weatherArray.getJSONObject(0)
                val weatherDescription = weatherObject.getString("description")

                // Update your UI with the weather data
                // For example, update TextViews with the retrieved data
                runOnUiThread {
                    // Update your UI with the weather data
                    val textViewTemperature = findViewById<TextView>(R.id.textViewTemperature)
                    val textViewHumidity = findViewById<TextView>(R.id.textViewHumidity)
                    val textViewWeatherDescription = findViewById<TextView>(R.id.textViewWeatherDescription)

                    textViewTemperature.text = "Temperature: $temperature °C"
                    textViewHumidity.text = "Humidity: $humidity%"
                    textViewWeatherDescription.text = "Description: $weatherDescription"
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Handle network error
            }
'''
weather_new = '''            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.w("Weather", "Weather request failed with HTTP ${response.code}")
                        return
                    }
                    try {
                        val weatherResponse = JSONObject(response.body?.string().orEmpty())
                        val mainData = weatherResponse.getJSONObject("main")
                        val temperature = mainData.getDouble("temp")
                        val humidity = mainData.getInt("humidity")
                        val weatherDescription = weatherResponse.getJSONArray("weather")
                            .getJSONObject(0).getString("description")
                        runOnUiThread {
                            findViewById<TextView>(R.id.textViewTemperature).text = "Temperature: $temperature °C"
                            findViewById<TextView>(R.id.textViewHumidity).text = "Humidity: $humidity%"
                            findViewById<TextView>(R.id.textViewWeatherDescription).text = "Description: $weatherDescription"
                        }
                    } catch (e: Exception) {
                        Log.e("Weather", "Unable to parse weather response", e)
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("Weather", "Weather request failed", e)
            }
'''
replace_once(weather_old, weather_new, "weather response")

text = text.replace("                .locationEngine(replayLocationEngine)\n", "")
text = text.replace("//comment it out for testing\n        replayOriginLocation()\n", "")
text = re.sub(r"\n//change location for current location\n    private fun replayOriginLocation\(\) \{.*?\n    \}\n\n    private fun findRoute", "\n\n    private fun findRoute", text, flags=re.S)
text = text.replace("        mapboxReplayer.finish()\n", "")
text = text.replace("        mapboxReplayer.stop()\n", "")

replace_once(
    """    override fun onDestroy() {
        super.onDestroy()
        //handler.removeCallbacks(updateDataRunnable)
""",
    """    override fun onDestroy() {
        handler.removeCallbacks(dataFetchRunnable)
""",
    "destroy cleanup",
)
replace_once(
    """        if(tts != null){
            tts!!.stop()
            tts!!.shutdown()
        }
    }
""",
    """        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
""",
    "tts cleanup",
)

text = text.replace("riskValue >= 3 && riskValue <= 7", "riskValue >= 3 && riskValue < 7")
text = text.replace("Point.fromLngLat(targetLat, targetLng)", "Point.fromLngLat(targetLng, targetLat)")

replace_once(
    """        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            callback(null)
        }

        getLastLocation""",
    """        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            callback(null)
            return
        }

        getLastLocation""",
    "last location permission",
)

replace_once(
    """            R.id.nav_home -> {
                val intent = Intent(this, TurnByTurnActivity::class.java)
                startActivity(intent)
            }
""",
    """            R.id.nav_home -> {
                // Already on the navigation screen.
            }
""",
    "home navigation",
)

replace_once(
    """    private val handler = Handler()
    private val delay: Long = 3000 // 3 seconds in milliseconds

    private fun startDataFetching() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Call the GettingData function here to fetch data
                GettingData()
                handler.postDelayed(this, delay)
            }
        }, delay)
    }
""",
    """    private val handler = Handler(Looper.getMainLooper())
    private val delay: Long = 3000
    private val dataFetchRunnable = object : Runnable {
        override fun run() {
            gettingData()
            handler.postDelayed(this, delay)
        }
    }

    private fun startDataFetching() {
        handler.postDelayed(dataFetchRunnable, delay)
    }
""",
    "polling lifecycle",
)
text = text.replace("    private fun GettingData() {", "    private fun gettingData() {")

# Meter-accurate risk circles.
count = 0
circle_pattern = r"    //Circle Around Markers\n    private fun addCircleToMarker\(lng: Double, lat: Double\) \{.*?\n    \}\n\n    private fun getColorForRisk"
circle_replacement = '''    // Circle around each monitored camera location. Turf keeps the radius in real metres.
    private fun addCircleToMarker(lng: Double, lat: Double) {
        val currentMapView = mapView ?: return
        val polygonAnnotationManager = currentMapView.annotations.createPolygonAnnotationManager(currentMapView)
        val center = Point.fromLngLat(lng, lat)
        val points = (0..64).map { index ->
            TurfMeasurement.destination(
                center,
                15.0,
                index * (360.0 / 64.0),
                TurfConstants.UNIT_METERS
            )
        }
        val options = PolygonAnnotationOptions()
            .withPoints(listOf(points))
            .withFillColor("#00ff00")
            .withFillOpacity(0.2)
            .withDraggable(false)
            .withFillOutlineColor("#00ff00")

        when {
            lat == 1.333706 && lng == 103.775743 -> { polygonAnnotation23 = polygonAnnotationManager.create(options); polygonAnnotationManager23 = polygonAnnotationManager }
            lat == 1.3349557 && lng == 103.7758805 -> { polygonAnnotation8 = polygonAnnotationManager.create(options); polygonAnnotationManager8 = polygonAnnotationManager }
            lat == 1.333876 && lng == 103.773636 -> { polygonAnnotationSIT = polygonAnnotationManager.create(options); polygonAnnotationManagerSIT = polygonAnnotationManager }
            lat == 1.3325963 && lng == 103.774189 -> { polygonAnnotation51 = polygonAnnotationManager.create(options); polygonAnnotationManager51 = polygonAnnotationManager }
            lat == 1.3318412 && lng == 103.7753838 -> { polygonAnnotation72 = polygonAnnotationManager.create(options); polygonAnnotationManager72 = polygonAnnotationManager }
            lat == 1.332582 && lng == 103.776578 -> { polygonAnnotation73 = polygonAnnotationManager.create(options); polygonAnnotationManager73 = polygonAnnotationManager }
        }
    }

    private fun setRiskRadiusVisible(visible: Boolean) {
        val opacity = if (visible) 0.2 else 0.0
        listOf(
            polygonAnnotation23 to polygonAnnotationManager23,
            polygonAnnotation8 to polygonAnnotationManager8,
            polygonAnnotation51 to polygonAnnotationManager51,
            polygonAnnotation72 to polygonAnnotationManager72,
            polygonAnnotation73 to polygonAnnotationManager73,
            polygonAnnotationSIT to polygonAnnotationManagerSIT
        ).forEach { (annotation, manager) ->
            annotation?.let { item ->
                item.fillOpacity = opacity
                manager?.update(item)
            }
        }
    }

    private fun getColorForRisk'''
text, count = re.subn(circle_pattern, circle_replacement, text, flags=re.S)
if count != 1:
    raise RuntimeError(f"Expected one risk-circle block, found {count}")

replace_once(
    """            isBorderVisible2 = !isBorderVisible2
            isRiskRadius = !isRiskRadius
            if (isBorderVisible2) {""",
    """            isBorderVisible2 = !isBorderVisible2
            isRiskRadius = !isRiskRadius
            setRiskRadiusVisible(isRiskRadius)
            if (isBorderVisible2) {""",
    "risk radius toggle",
)

# Avoid nullable map force-unwrapping when creating marker annotations.
replace_once(
    """            val annotationApi = mapView?.annotations
            val pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView!!)""",
    """            val currentMapView = mapView ?: return@let
            val pointAnnotationManager = currentMapView.annotations.createPointAnnotationManager(currentMapView)""",
    "marker map view",
)

# Robust nearest-geofence logic and one-shot threshold announcements.
geofence_pattern = r"    @SuppressLint\(\"MissingPermission\", \"SuspiciousIndentation\"\)\n    private fun calculateDistance\(\) \{.*?\n    \}\n\n\n    private data class Geofence"
geofence_replacement = '''    private fun calculateDistance() {
        val currentLocation = userLocation ?: return
        val currentPoint = Point.fromLngLat(currentLocation.longitude, currentLocation.latitude)
        val riskImage: ImageView = findViewById(R.id.riskimg)
        val ttcView: TextView = findViewById(R.id.ttc)

        val nearest = _geofenceList.map { geofence ->
            val point = Point.fromLngLat(geofence.longitude, geofence.latitude)
            geofence to TurfMeasurement.distance(currentPoint, point, TurfConstants.UNIT_METERS)
        }.minByOrNull { it.second }

        runOnUiThread {
            if (nearest == null || nearest.second > 50.0) {
                userIsWithinGeofence = false
                riskImage.visibility = View.INVISIBLE
                ttcView.visibility = View.INVISIBLE
                lastGeofenceId = null
                lastGeofenceZone = null
                return@runOnUiThread
            }

            val (geofence, distanceMeters) = nearest
            userIsWithinGeofence = true
            riskImage.visibility = View.VISIBLE
            ttcView.visibility = View.VISIBLE
            val riskValue = geofenceRiskMap[geofence.id] ?: 0
            val ttcValue = geofenceTTCMap[geofence.id] ?: 0.0
            ttcView.text = String.format(Locale.US, "TTC: %.2f", ttcValue)
            riskImage.setImageResource(
                when {
                    riskValue < 3 -> R.drawable.risk_green
                    riskValue < 7 -> R.drawable.risk_yellow
                    else -> R.drawable.risk_red
                }
            )

            val zone = if (distanceMeters <= 25.0) "nearby" else "50m"
            if (lastGeofenceId != geofence.id || lastGeofenceZone != zone) {
                if (soundtoggle) {
                    speakOut(if (zone == "nearby") "You are nearby ${geofence.id}" else "You are 50 meters away from ${geofence.id}")
                }
                lastGeofenceId = geofence.id
                lastGeofenceZone = zone
            }
        }
    }


    private data class Geofence'''
text, count = re.subn(geofence_pattern, geofence_replacement, text, flags=re.S)
if count != 1:
    raise RuntimeError(f"Expected one geofence block, found {count}")

# Replace polling response/update code with one main-thread UI update.
data_start = text.index("    private fun gettingData() {")
graph_start = text.index("    //Graph", data_start)
data_replacement = '''    private fun gettingData() {
        val request = Request.Builder().url("${BuildConfig.BACKEND_BASE_URL}/risks").build()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("RiskData", "Risk request failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.w("RiskData", "Risk request failed with HTTP ${response.code}")
                        return
                    }
                    try {
                        val array = JSONArray(response.body?.string().orEmpty())
                        if (array.length() == 0) return
                        val data = array.getJSONObject(0)
                        val risks = mapOf(
                            "Blk 8" to data.optInt("blk8", 0), "Blk 23" to data.optInt("blk23", 0),
                            "Blk 51" to data.optInt("blk51", 0), "Blk 72" to data.optInt("blk72", 0),
                            "Blk 73" to data.optInt("blk73", 0), "SIT" to data.optInt("blkSIT", 0)
                        )
                        val ttc = mapOf(
                            "Blk 8" to data.optDouble("ttc8", 0.0), "Blk 23" to data.optDouble("ttc23", 0.0),
                            "Blk 51" to data.optDouble("ttc51", 0.0), "Blk 72" to data.optDouble("ttc72", 0.0),
                            "Blk 73" to data.optDouble("ttc73", 0.0), "SIT" to data.optDouble("ttcSIT", 0.0)
                        )
                        geofenceRiskMap.putAll(risks)
                        geofenceTTCMap.putAll(ttc)
                        risk8Value = risks.getValue("Blk 8")
                        risk23Value = risks.getValue("Blk 23")
                        risk51Value = risks.getValue("Blk 51")
                        risk72Value = risks.getValue("Blk 72")
                        risk73Value = risks.getValue("Blk 73")
                        riskSITValue = risks.getValue("SIT")
                        runOnUiThread { updateAllRiskUi(); calculateDistance() }
                    } catch (e: Exception) {
                        Log.e("RiskData", "Unable to parse risk response", e)
                    }
                }
            }
        })
    }

    private fun updateAllRiskUi() {
        updateCircle(polygonAnnotation8, polygonAnnotationManager8, risk8Value)
        updateCircle(polygonAnnotation23, polygonAnnotationManager23, risk23Value)
        updateCircle(polygonAnnotation51, polygonAnnotationManager51, risk51Value)
        updateCircle(polygonAnnotation72, polygonAnnotationManager72, risk72Value)
        updateCircle(polygonAnnotation73, polygonAnnotationManager73, risk73Value)
        updateCircle(polygonAnnotationSIT, polygonAnnotationManagerSIT, riskSITValue)
        updateRiskContainer23Color(risk23Value); updateRiskContainer8Color(risk8Value)
        updateRiskContainer51Color(risk51Value); updateRiskContainer72Color(risk72Value)
        updateRiskContainer73Color(risk73Value); updateRiskContainerSITColor(riskSITValue)
        findViewById<TextView>(R.id.blk23risktextView).text = "Current Risk Value: $risk23Value"
        findViewById<TextView>(R.id.blk8risktextView).text = "Current Risk Value: $risk8Value"
        findViewById<TextView>(R.id.blk51risktextView).text = "Current Risk Value: $risk51Value"
        findViewById<TextView>(R.id.blk72risktextView).text = "Current Risk Value: $risk72Value"
        findViewById<TextView>(R.id.blk73risktextView).text = "Current Risk Value: $risk73Value"
        findViewById<TextView>(R.id.SITrisktextView).text = "Current Risk Value: $riskSITValue"
    }

    private fun updateCircle(annotation: PolygonAnnotation?, manager: PolygonAnnotationManager?, riskValue: Int) {
        annotation?.let { item ->
            item.fillColorString = getColorForRisk(riskValue)
            item.fillOutlineColorString = getColorForRisk(riskValue)
            manager?.update(item)
        }
    }
'''
text = text[:data_start] + data_replacement + text[graph_start:]

# Collapse six duplicate chart implementations into one lifecycle-aware loader.
graph_start = text.index("    //Graph")
generic_graph = '''    // Historical risk charts shown in the map detail cards.
    private fun fetchChartData(barChart: BarChart, endpoint: String) {
        lifecycleScope.launch {
            try {
                val entries = withContext(Dispatchers.IO) {
                    val request = Request.Builder().url("${BuildConfig.BACKEND_BASE_URL}/$endpoint").build()
                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IllegalStateException("$endpoint returned HTTP ${response.code}")
                        parseChartData(response.body?.string())
                    }
                }
                updateBarChart(barChart, entries)
            } catch (e: Exception) {
                Log.e("RiskChart", "Unable to load $endpoint", e)
            }
        }
    }

    private fun parseChartData(jsonData: String?): ArrayList<BarEntry> {
        val values = FloatArray(13)
        if (!jsonData.isNullOrBlank()) {
            val array = JSONArray(jsonData)
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val hour = item.optInt("hour", -1)
                if (hour in 7..19) values[hour - 7] = item.optDouble("avgrisk", 0.0).toFloat()
            }
        }
        return ArrayList<BarEntry>().apply { values.forEachIndexed { i, value -> add(BarEntry(i.toFloat(), value)) } }
    }

    private fun updateBarChart(barChart: BarChart, entries: ArrayList<BarEntry>) {
        val dataSet = BarDataSet(entries, "Average risk").apply {
            color = ContextCompat.getColor(this@TurnByTurnActivity, R.color.blue)
            setDrawValues(true)
            valueFormatter = MyValueFormatter()
            isHighlightEnabled = false
        }
        barChart.data = BarData(dataSet)
        barChart.description = null
        barChart.axisRight.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.axisMaximum = 10f
        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(generateXAxisLabels())
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            labelCount = 13
            granularity = 1f
        }
        barChart.invalidate()
    }

    private fun generateXAxisLabels(): List<String> = (7..19).map { String.format("%02d", it) }

    class MyValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String = String.format("%.2f", value)
    }
}
'''
text = text[:graph_start] + generic_graph

# Update stale example comments that still mention simulation.
text = text.replace("The example also uses replay\n * location engine to facilitate navigation without actually physically moving.", "The final portfolio version uses the device location for navigation.")
text = text.replace(" * - The guidance will start to the selected destination while simulating location updates.\n * You can disable simulation by commenting out the [replayLocationEngine] setter in [NavigationOptions].\n * Then, the device's real location will be used.\n", " * - The guidance starts to the selected destination using device location updates.\n")

PATH.write_text(text)
print("Updated", PATH)
