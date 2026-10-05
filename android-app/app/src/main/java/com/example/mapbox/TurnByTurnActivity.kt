package com.example.mapbox

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.mapbox.databinding.ActivityTurnByTurnBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.android.material.navigation.NavigationView
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.Utils
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.DistanceRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.ui.tripprogress.model.PercentDistanceTraveledFormatter
import com.mapbox.navigation.ui.tripprogress.model.TimeRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.ui.tripprogress.view.MapboxTripProgressView
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue
import com.mapbox.navigation.ui.voice.model.SpeechVolume
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteOptions
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.mapbox.search.autocomplete.PlaceAutocompleteType
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Date
import java.util.Locale

/**
 * This example demonstrates a basic turn-by-turn navigation experience by putting together some UI elements to showcase
 * navigation camera transitions, guidance instructions banners and playback, and progress along the route.
 *
 * Local configuration is read from android-app/local.properties. Copy the provided
 * example and supply your own credentials; do not store access tokens in source XML.
 * The launcher requests location permission before opening navigation.
 *
 * How to use this example:
 * - You can long-click the map to select a destination.
 * - The guidance starts to the selected destination using device location updates.
 * - At any point in time you can finish guidance or select a new destination.
 * - You can use buttons to mute/unmute voice instructions, recenter the camera, or show the route overview.
 */

class TurnByTurnActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, TextToSpeech.OnInitListener {

    private var mapView: MapView? = null
    private lateinit var container: LinearLayout
    private lateinit var weathercontainer: LinearLayout
    private lateinit var cardView: CardView
    private lateinit var drawerLayout: DrawerLayout
    private var userLocation: Location? = null
    private lateinit var menubutton: ImageView
    private var tts: TextToSpeech? = null
    private var isBorderVisible = false
    private var isBorderVisible2 = false
    private var isRiskRadius = false
    private lateinit var trafficimg: ImageView
    private lateinit var riskradiusimg: ImageView
    private val apiKey = BuildConfig.OPENWEATHER_API_KEY
    private val cityName = "Singapore"
    private val geofenceRiskMap = mutableMapOf<String, Int>()
    private val geofenceTTCMap = mutableMapOf<String, Double>()
    private var soundtoggle = true
    private val httpClient = OkHttpClient()
    private var lastGeofenceId: String? = null
    private var lastGeofenceZone: String? = null
    private var polygonAnnotation23: PolygonAnnotation? = null
    private var polygonAnnotationManager23: PolygonAnnotationManager? = null
    private var polygonAnnotation8: PolygonAnnotation? = null
    private var polygonAnnotationManager8: PolygonAnnotationManager? = null
    private var polygonAnnotation51: PolygonAnnotation? = null
    private var polygonAnnotationManager51: PolygonAnnotationManager? = null
    private var polygonAnnotation72: PolygonAnnotation? = null
    private var polygonAnnotationManager72: PolygonAnnotationManager? = null
    private var polygonAnnotation73: PolygonAnnotation? = null
    private var polygonAnnotationManager73: PolygonAnnotationManager? = null
    private var polygonAnnotationSIT: PolygonAnnotation? = null
    private var polygonAnnotationManagerSIT: PolygonAnnotationManager? = null
    var risk8Value: Int = 0
    var risk23Value: Int = 0
    var risk51Value: Int = 0
    var risk73Value: Int = 0
    var risk72Value: Int = 0
    var riskSITValue: Int = 0
    var navigation = false

    private lateinit var placeAutocomplete: PlaceAutocomplete

    private lateinit var searchResultsView: SearchResultsView
    private lateinit var placeAutocompleteUiAdapter: PlaceAutocompleteUiAdapter

    private lateinit var queryEditText: EditText

    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapMarkersManager: MapMarkersManager

    private lateinit var searchPlaceView: SearchPlaceBottomSheetView

    private var ignoreNextQueryUpdate = false
    private var userIsWithinGeofence = false

    private companion object {
        private const val BUTTON_ANIMATION_DURATION = 1500L

        const val PERMISSIONS_REQUEST_LOCATION = 0

        const val LOG_TAG = "AutocompleteUiActivity"

        val MARKERS_EDGE_OFFSET = Utils.dpToPx(64F).toDouble()
        val PLACE_CARD_HEIGHT = Utils.dpToPx(300F).toDouble()
        val MARKERS_TOP_OFFSET = Utils.dpToPx(88F).toDouble()

        val MARKERS_INSETS_OPEN_CARD = EdgeInsets(
            MARKERS_TOP_OFFSET, MARKERS_EDGE_OFFSET, PLACE_CARD_HEIGHT, MARKERS_EDGE_OFFSET
        )

        val REGION_LEVEL_TYPES = listOf(
            PlaceAutocompleteType.AdministrativeUnit.Country,
            PlaceAutocompleteType.AdministrativeUnit.Region
        )

        val DISTRICT_LEVEL_TYPES = REGION_LEVEL_TYPES + listOf(
            PlaceAutocompleteType.AdministrativeUnit.Postcode,
            PlaceAutocompleteType.AdministrativeUnit.District
        )

        val LOCALITY_LEVEL_TYPES = DISTRICT_LEVEL_TYPES + listOf(
            PlaceAutocompleteType.AdministrativeUnit.Place,
            PlaceAutocompleteType.AdministrativeUnit.Locality
        )

        private val ALL_TYPES = listOf(
            PlaceAutocompleteType.Poi,
            PlaceAutocompleteType.AdministrativeUnit.Country,
            PlaceAutocompleteType.AdministrativeUnit.Region,
            PlaceAutocompleteType.AdministrativeUnit.Postcode,
            PlaceAutocompleteType.AdministrativeUnit.District,
            PlaceAutocompleteType.AdministrativeUnit.Place,
            PlaceAutocompleteType.AdministrativeUnit.Locality,
            PlaceAutocompleteType.AdministrativeUnit.Neighborhood,
            PlaceAutocompleteType.AdministrativeUnit.Street,
            PlaceAutocompleteType.AdministrativeUnit.Address,
        )
    }


    /**
     * Bindings to the example layout.
     */
    private lateinit var binding: ActivityTurnByTurnBinding

    /**
     * Used to execute camera transitions based on the data generated by the [viewportDataSource].
     * This includes transitions from route overview to route following and continuously updating the camera as the location changes.
     */
    private lateinit var navigationCamera: NavigationCamera

    /**
     * Produces the camera frames based on the location and routing data for the [navigationCamera] to execute.
     */
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    /*
     * Below are generated camera padding values to ensure that the route fits well on screen while
     * other elements are overlaid on top of the map (including instruction view, buttons, etc.)
     */
    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            20.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    /**
     * Generates updates for the [MapboxManeuverView] to display the upcoming maneuver instructions
     * and remaining distance to the maneuver point.
     */
    private lateinit var maneuverApi: MapboxManeuverApi

    /**
     * Generates updates for the [MapboxTripProgressView] that include remaining time and distance to the destination.
     */
    private lateinit var tripProgressApi: MapboxTripProgressApi

    /**
     * Generates updates for the [routeLineView] with the geometries and properties of the routes that should be drawn on the map.
     */
    private lateinit var routeLineApi: MapboxRouteLineApi

    /**
     * Draws route lines on the map based on the data from the [routeLineApi]
     */
    private lateinit var routeLineView: MapboxRouteLineView

    /**
     * Generates updates for the [routeArrowView] with the geometries and properties of maneuver arrows that should be drawn on the map.
     */
    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()

    /**
     * Draws maneuver arrows on the map based on the data [routeArrowApi].
     */
    private lateinit var routeArrowView: MapboxRouteArrowView

    /**
     * Stores and updates the state of whether the voice instructions should be played as they come or muted.
     */
    private var isVoiceInstructionsMuted = false
        set(value) {
            field = value
            if (value) {
                binding.soundButton.muteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
                binding.soundButton.unmuteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(1f))
            }
        }

    /**
     * Extracts message that should be communicated to the driver about the upcoming maneuver.
     * When possible, downloads a synthesized audio file that can be played back to the driver.
     */
    private lateinit var speechApi: MapboxSpeechApi

    /**
     * Plays the synthesized audio files with upcoming maneuver instructions
     * or uses an on-device Text-To-Speech engine to communicate the message to the driver.
     * NOTE: do not use lazy initialization for this class since it takes some time to initialize
     * the system services required for on-device speech synthesis. With lazy initialization
     * there is a high risk that said services will not be available when the first instruction
     * has to be played. [MapboxVoiceInstructionsPlayer] should be instantiated in
     * `Activity#onCreate`.
     */
    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer

    /**
     * Observes when a new voice instruction should be played.
     */
    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(voiceInstructions, speechCallback)
    }

    /**
     * Based on whether the synthesized audio file is available, the callback plays the file
     * or uses the fall back which is played back using the on-device Text-To-Speech engine.
     */
    private val speechCallback =
        MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
            expected.fold(
                { error ->
                    // play the instruction via fallback text-to-speech engine
                    voiceInstructionsPlayer.play(
                        error.fallback,
                        voiceInstructionsPlayerCallback
                    )
                },
                { value ->
                    // play the sound file from the external generator
                    voiceInstructionsPlayer.play(
                        value.announcement,
                        voiceInstructionsPlayerCallback
                    )
                }
            )
        }

    /**
     * When a synthesized audio file was downloaded, this callback cleans up the disk after it was played.
     */
    private val voiceInstructionsPlayerCallback =
        MapboxNavigationConsumer<SpeechAnnouncement> { value ->
            // remove already consumed file to free-up space
            speechApi.clean(value)
        }


    /**
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider = NavigationLocationProvider()

    /**
     * Gets notified with location updates.
     *
     * Exposes raw updates coming directly from the location services
     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
     */
    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            // update location puck's position on the map
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )

            // update camera position to account for new location
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            // if this is the first location update the activity has received,
            // it's best to immediately move the camera to the current user location
            if (!firstLocationUpdateReceived) {
                firstLocationUpdateReceived = true
                navigationCamera.requestNavigationCameraToOverview(
                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                        .maxDuration(0) // instant transition
                        .build()
                )
            }
            userLocation = enhancedLocation
            //Log.d("User Location", "Latitude: ${userLocation?.latitude}, Longitude: ${userLocation?.longitude}")
        }
    }

    /**
     * Gets notified with progress along the currently active route.
     */
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        // draw the upcoming maneuver arrow on the map
        val style = binding.mapView.getMapboxMap().getStyle()
        if (style != null) {
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        // update top banner with maneuver instructions
        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error ->
                Toast.makeText(
                    this@TurnByTurnActivity,
                    error.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            },
            {
                binding.maneuverView.visibility = View.VISIBLE
                binding.maneuverView.renderManeuvers(maneuvers)
            }
        )

        // update bottom trip progress summary
        binding.tripProgressView.render(
            tripProgressApi.getTripProgress(routeProgress)
        )
    }

    /**
     * Gets notified whenever the tracked routes change.
     *
     * A change can mean:
     * - routes get changed with [MapboxNavigation.setRoutes]
     * - routes annotations get refreshed (for example, congestion annotation that indicate the live traffic along the route)
     * - driver got off route and a reroute was executed
     */
    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
            // generate route geometries asynchronously and render them
            routeLineApi.setNavigationRoutes(
                routeUpdateResult.navigationRoutes
            ) { value ->
                binding.mapView.getMapboxMap().getStyle()?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }

            // update the camera position to account for the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
            viewportDataSource.evaluate()
        } else {
            // remove the route line and route arrow from the map
            val style = binding.mapView.getMapboxMap().getStyle()
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        style,
                        value
                    )
                }
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }

            // remove the route reference from camera position evaluations
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
            navigation = false
        }
    }

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
                // start the trip session to being receiving location updates in free drive
                // and later when a route is set also receiving route progress updates
                mapboxNavigation.startTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
            }
        },
        onInitialize = this::initNavigation
    )

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTurnByTurnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //graph

        val barChart1: BarChart = findViewById(R.id.ONClickbarChart1)
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


        //Getting mysql data
        startDataFetching()

        //Getting weather data
        fetchWeatherData()

        tts = TextToSpeech(this, this)

        placeAutocomplete = PlaceAutocomplete.create(getString(R.string.mapbox_access_token))

        queryEditText = findViewById(R.id.query_text)
        mapView = findViewById(R.id.mapView)
        binding.mapView.getMapboxMap().also { mapboxMap ->
            this.mapboxMap = mapboxMap

            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                binding.mapView.location.updateSettings {
                    enabled = true
                }

                binding.mapView.location.addOnIndicatorPositionChangedListener(object :
                    OnIndicatorPositionChangedListener {
                    override fun onIndicatorPositionChanged(point: Point) {
                        binding.mapView.getMapboxMap().setCamera(
                            CameraOptions.Builder()
                                .center(point)
                                .zoom(14.0)
                                .build()
                        )
                        binding.mapView.location.removeOnIndicatorPositionChangedListener(this)
                    }
                })
            }
        }
        mapMarkersManager = MapMarkersManager(binding.mapView)
        mapboxMap.addOnMapLongClickListener {
            reverseGeocoding(it)
            return@addOnMapLongClickListener true
        }

        searchResultsView = findViewById(R.id.search_results_view)

        searchResultsView.initialize(
            SearchResultsView.Configuration(
                commonConfiguration = CommonSearchViewConfiguration()
            )
        )

        placeAutocompleteUiAdapter = PlaceAutocompleteUiAdapter(
            view = searchResultsView,
            placeAutocomplete = placeAutocomplete
        )

        searchPlaceView = findViewById<SearchPlaceBottomSheetView>(R.id.search_place_view).apply {
            initialize(CommonSearchViewConfiguration())

            isFavoriteButtonVisible = false

            addOnCloseClickListener {
                hide()
                closePlaceCard()
            }

            addOnNavigateClickListener { searchPlace ->
                findRoute(searchPlace.coordinate)
            }

            addOnShareClickListener { searchPlace ->
                startActivity(shareIntent(searchPlace))
            }
        }

        LocationEngineProvider.getBestLocationEngine(applicationContext)
            .lastKnownLocation(this) { point ->
                point?.let {
                    binding.mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(9.0)
                            .build()
                    )
                }
            }

        placeAutocompleteUiAdapter.addSearchListener(object :
            PlaceAutocompleteUiAdapter.SearchListener {

            override fun onSuggestionsShown(suggestions: List<PlaceAutocompleteSuggestion>) {
                // Nothing to do
            }

            override fun onSuggestionSelected(suggestion: PlaceAutocompleteSuggestion) {
                openPlaceCard(suggestion)
            }

            override fun onPopulateQueryClick(suggestion: PlaceAutocompleteSuggestion) {
                queryEditText.setText(suggestion.name)
            }

            override fun onError(e: Exception) {
                // Nothing to do
            }
        })

        queryEditText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                if (ignoreNextQueryUpdate) {
                    ignoreNextQueryUpdate = false
                } else {
                    closePlaceCard()
                }

                lifecycleScope.launchWhenStarted {
                    placeAutocompleteUiAdapter.search(text.toString())
                    searchResultsView.isVisible = text.isNotEmpty()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Nothing to do
            }

            override fun afterTextChanged(s: Editable) {
                // Nothing to do
            }
        })

        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSIONS_REQUEST_LOCATION
            )
        }

        //MAP SETTINGS
        trafficimg = findViewById(R.id.traffic)
        trafficimg.setOnClickListener {
            isBorderVisible = !isBorderVisible
            if (isBorderVisible) {
                trafficimg.setBackgroundResource(R.drawable.border_selector)
            } else {
                trafficimg.setBackgroundResource(R.drawable.traffic)
            }
        }
        riskradiusimg = findViewById(R.id.risk_radius)
        riskradiusimg.setOnClickListener {
            isBorderVisible2 = !isBorderVisible2
            isRiskRadius = !isRiskRadius
            setRiskRadiusVisible(isRiskRadius)
            if (isBorderVisible2) {
                riskradiusimg.setBackgroundResource(R.drawable.border_selector)
            } else {
                riskradiusimg.setBackgroundResource(R.drawable.risk_radius)
            }
        }

        container = findViewById(R.id.container)
        cardView = findViewById(R.id.cardView)
        Log.d("Start onCreate", "oncreate started")
        binding.fab1.setOnClickListener {
            toggleContainerVisibility()
            Log.d("ToggleContainer", "fab1 pressed")
        }
        weathercontainer = findViewById(R.id.weathercontainer)
        binding.fab2.setOnClickListener {
            toggleweatherContainerVisibility()
            Log.d("ToggleWeatherContainer", "fab2 pressed")
        }

        binding.fabSound.setOnClickListener {
            soundtoggle = !soundtoggle
            if (soundtoggle) {
                binding.fabSound.setImageResource(R.drawable.baseline_hearing_24)
            } else {
                binding.fabSound.setImageResource(R.drawable.baseline_hearing_disabled_24)
            }
        }
        drawerLayout = findViewById(R.id.drawer_layout)
        menubutton = findViewById(R.id.menubutton)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)
        binding.menubutton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        val closeIcon1 = findViewById<ImageView>(R.id.closeIcon1)
        closeIcon1.setOnClickListener {
            cardView.visibility = View.INVISIBLE
            container.visibility = View.INVISIBLE
            isContainerVisible = false
        }

        val closeIcon2 = findViewById<ImageView>(R.id.closeIcon2)
        closeIcon2.setOnClickListener {
            cardView.visibility = View.INVISIBLE
            weathercontainer.visibility = View.INVISIBLE
            isContainerVisible = false
        }

        val closeIcon3 = findViewById<ImageView>(R.id.closeIcon3)
        closeIcon3.setOnClickListener {
            cardView.visibility = View.INVISIBLE
            binding.blk8container.visibility = View.INVISIBLE
            weathercontainer.visibility = View.INVISIBLE
            isContainerVisible = false
        }

        val closeIcon4 = findViewById<ImageView>(R.id.closeIcon4)
        closeIcon4.setOnClickListener {
            cardView.visibility = View.INVISIBLE
            binding.SITcontainer.visibility = View.INVISIBLE
            weathercontainer.visibility = View.INVISIBLE
            isContainerVisible = false
        }
        val closeIcon5 = findViewById<ImageView>(R.id.closeIcon5)
        closeIcon5.setOnClickListener {
            cardView.visibility = View.INVISIBLE
            binding.blk72container.visibility = View.INVISIBLE
            weathercontainer.visibility = View.INVISIBLE
            isContainerVisible = false
        }
        val closeIcon6 = findViewById<ImageView>(R.id.closeIcon6)
        closeIcon6.setOnClickListener {
            cardView.visibility = View.INVISIBLE
            binding.blk73container.visibility = View.INVISIBLE
            weathercontainer.visibility = View.INVISIBLE
            isContainerVisible = false
        }
        val closeIcon7 = findViewById<ImageView>(R.id.closeIcon7)
        closeIcon7.setOnClickListener {
            cardView.visibility = View.INVISIBLE
            binding.blk51container.visibility = View.INVISIBLE
            weathercontainer.visibility = View.INVISIBLE
            isContainerVisible = false
        }

        //Markers
        addAnnotationToMap(103.775743, 1.333706) // CAM23
        addAnnotationToMap(103.7758805, 1.3349557) //CAM8
        addAnnotationToMap(103.773636, 1.333876)//CAM SIT
        addAnnotationToMap(103.774189, 1.3325963) //CAM51
        addAnnotationToMap(103.7753838, 1.3318412) //CAM72
        addAnnotationToMap(103.776578, 1.332582)//CAM73

        addCircleToMarker(103.775743, 1.333706)
        addCircleToMarker(103.7758805, 1.3349557)
        addCircleToMarker(103.773636, 1.333876)
        addCircleToMarker(103.774189, 1.3325963)
        addCircleToMarker(103.7753838, 1.3318412)
        addCircleToMarker(103.776578, 1.332582)

        // initialize Navigation Camera
        viewportDataSource = MapboxNavigationViewportDataSource(binding.mapView.getMapboxMap())
        navigationCamera = NavigationCamera(
            binding.mapView.getMapboxMap(),
            binding.mapView.camera,
            viewportDataSource
        )
        // set the animations lifecycle listener to ensure the NavigationCamera stops
        // automatically following the user location when the map is interacted with
        binding.mapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )
        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
            // shows/hide the recenter button depending on the camera state
            when (navigationCameraState) {
                NavigationCameraState.TRANSITION_TO_FOLLOWING,
                NavigationCameraState.FOLLOWING -> binding.recenter.visibility = View.INVISIBLE

                NavigationCameraState.TRANSITION_TO_OVERVIEW,
                NavigationCameraState.OVERVIEW,
                NavigationCameraState.IDLE -> binding.recenter.visibility = View.VISIBLE
            }
        }
        // set the padding values depending on screen orientation and visible view layout
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.overviewPadding = landscapeOverviewPadding
        } else {
            viewportDataSource.overviewPadding = overviewPadding
        }
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.followingPadding = landscapeFollowingPadding
        } else {
            viewportDataSource.followingPadding = followingPadding
        }

        // make sure to use the same DistanceFormatterOptions across different features
        val distanceFormatterOptions = DistanceFormatterOptions.Builder(this).build()

        // initialize maneuver api that feeds the data to the top banner maneuver view
        maneuverApi = MapboxManeuverApi(
            MapboxDistanceFormatter(distanceFormatterOptions)
        )

        // initialize bottom progress view
        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(this)
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(
                    TimeRemainingFormatter(this)
                )
                .percentRouteTraveledFormatter(
                    PercentDistanceTraveledFormatter()
                )
                .estimatedTimeToArrivalFormatter(
                    EstimatedTimeToArrivalFormatter(this, TimeFormat.NONE_SPECIFIED)
                )
                .build()
        )

        // initialize voice instructions api and the voice instruction player
        speechApi = MapboxSpeechApi(
            this,
            getString(R.string.mapbox_access_token),
            Locale.US.language
        )
        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
            this,
            getString(R.string.mapbox_access_token),
            Locale.US.language
        )

        // initialize route line, the withRouteLineBelowLayerId is specified to place
        // the route line below road labels layer on the map
        // the value of this option will depend on the style that you are using
        // and under which layer the route line should be placed on the map layers stack
        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(this)
            .withRouteLineBelowLayerId("road-label-navigation")
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

        // initialize maneuver arrow view to draw arrows on the map
        val routeArrowOptions = RouteArrowOptions.Builder(this).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)

        // load map style traffic
//        if(isBorderVisible){
        binding.mapView.getMapboxMap().loadStyleUri(NavigationStyles.NAVIGATION_DAY_STYLE) {
        }
//    }
//        else{
//            binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
//        }

        // initialize view interactions
        binding.stop.setOnClickListener {
            clearRouteAndStopNavigation()
        }
            binding.recenter.setOnClickListener {
                if(!navigation) {
                    navigationCamera.requestNavigationCameraToOverview()
                    binding.routeOverview.showTextAndExtend(BUTTON_ANIMATION_DURATION)
                }else {
                    navigationCamera.requestNavigationCameraToFollowing()
                    binding.routeOverview.showTextAndExtend(BUTTON_ANIMATION_DURATION)
                }
            }

        binding.routeOverview.setOnClickListener {
            navigationCamera.requestNavigationCameraToOverview()
            binding.recenter.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        binding.soundButton.setOnClickListener {
            // mute/unmute voice instructions
            isVoiceInstructionsMuted = !isVoiceInstructionsMuted
        }

        // set initial sounds button state
        binding.soundButton.unmute()
    }

    private fun fetchWeatherData() {
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey&units=metric"

        val request = Request.Builder()
            .url(url)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
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
        })
    }


    override fun onDestroy() {
        handler.removeCallbacks(dataFetchRunnable)
        maneuverApi.cancel()
        routeLineApi.cancel()
        routeLineView.cancel()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )

        // initialize location puck
        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            this.locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    this@TurnByTurnActivity,
                    com.mapbox.navigation.R.drawable.mapbox_navigation_puck_icon
                )
            )
            enabled = true
        }
    }

    private fun findRoute(destination: Point) {
        val originLocation = navigationLocationProvider.lastLocation
        val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return

        // execute a route request
        // it's recommended to use the
        // applyDefaultNavigationOptions and applyLanguageAndVoiceUnitOptions
        // that make sure the route request is optimized
        // to allow for support of all of the Navigation SDK features
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(listOf(originPoint, destination))
                // provide the bearing for the origin of the request to ensure
                // that the returned route faces in the direction of the current user movement
                .bearingsList(
                    listOf(
                        Bearing.builder()
                            .angle(originLocation.bearing.toDouble())
                            .degrees(45.0)
                            .build(),
                        null
                    )
                )
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                .build(),
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                    // no impl
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    // no impl
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    setRouteAndStartNavigation(routes)
                    navigation = true
                }
            }
        )
    }

    private fun setRouteAndStartNavigation(routes: List<NavigationRoute>) {
        // set routes, where the first route in the list is the primary route that
        // will be used for active guidance
        mapboxNavigation.setNavigationRoutes(routes)

        // show UI elements
        binding.soundButton.visibility = View.VISIBLE
        binding.routeOverview.visibility = View.VISIBLE
        binding.tripProgressCard.visibility = View.VISIBLE
        binding.ttc.visibility = View.VISIBLE
        binding.fab1.visibility = View.INVISIBLE
        binding.fab2.visibility = View.INVISIBLE
        binding.fabSound.visibility = View.INVISIBLE
        binding.cardView.visibility = View.INVISIBLE
        binding.container.visibility = View.INVISIBLE
        searchPlaceView.hide()

        // move the camera to overview when new route is available
        navigationCamera.requestNavigationCameraToOverview()
    }

    private fun clearRouteAndStopNavigation() {
        // clear
        mapboxNavigation.setNavigationRoutes(listOf())

        // stop simulation

        // hide UI elements
        binding.soundButton.visibility = View.INVISIBLE
        binding.maneuverView.visibility = View.INVISIBLE
        binding.ttc.visibility = View.INVISIBLE
        binding.routeOverview.visibility = View.INVISIBLE
        binding.tripProgressCard.visibility = View.INVISIBLE
        binding.fabSound.visibility = View.VISIBLE
        binding.fab2.visibility = View.VISIBLE
        mapMarkersManager.clearMarkers()
    }


    private fun addAnnotationToMap(lng:Double,lat: Double) {
// Create an instance of the Annotation API and get the PointAnnotationManager.
        var id = "0"
        val closeIcon = findViewById<ImageView>(R.id.closeIcon)

        val cardView = findViewById<CardView>(R.id.cardView)
        bitmapFromDrawableRes(
            this@TurnByTurnActivity,
            R.drawable.camera_icon
        )?.let {
            val currentMapView = mapView ?: return@let
            val pointAnnotationManager = currentMapView.annotations.createPointAnnotationManager(currentMapView)
// Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
// Define a geographic coordinate.
                .withPoint(Point.fromLngLat(lng, lat))
// Specify the bitmap you assigned to the point annotation
// The bitmap will be added to map style automatically.
                .withIconImage(it)
// Add the resulting pointAnnotation to the map.
            pointAnnotationManager?.create(pointAnnotationOptions)
            pointAnnotationManager?.apply {
                addClickListener(
                    OnPointAnnotationClickListener {

                        if(lng == 103.775743 && lat==1.333706){
                            id = "23"
                            val targetLat = 1.33397173881531
                            val targetLng = 103.77531433105469
                            val targetCoordinate = Point.fromLngLat(targetLng, targetLat)
                            CameraOptions.Builder()
                                .center(targetCoordinate)
                                .zoom(15.0)
                                .build().also { cameraOptions ->
                                    mapboxMap.setCamera(cameraOptions)
                                }
                            binding.blk23container.visibility = View.VISIBLE
                            binding.blk72container.visibility = View.INVISIBLE
                            binding.blk73container.visibility = View.INVISIBLE
                            binding.blk8container.visibility = View.INVISIBLE
                            binding.SITcontainer.visibility = View.INVISIBLE
                            binding.blk51container.visibility = View.INVISIBLE
                            binding.cardView.visibility = View.VISIBLE
                            binding.container.visibility = View.INVISIBLE
                            isContainerVisible=false
                            binding.weathercontainer.visibility = View.INVISIBLE
                            isWeatherContainerVisible=false
                        }
                        else if(lat ==1.3349557 && lng ==103.7758805)
                        {
                            id ="8"
                            val targetLat = 1.3349557
                            val targetLng = 103.7758805
                            val targetCoordinate = Point.fromLngLat(targetLng, targetLat)
                            CameraOptions.Builder()
                                .center(targetCoordinate)
                                .zoom(15.0)
                                .build().also { cameraOptions ->
                                    mapboxMap.setCamera(cameraOptions)
                                }
                            binding.blk8container.visibility = View.VISIBLE
                            binding.blk23container.visibility = View.INVISIBLE
                            binding.blk51container.visibility = View.INVISIBLE
                            binding.blk72container.visibility = View.INVISIBLE
                            binding.SITcontainer.visibility = View.INVISIBLE
                            binding.blk73container.visibility = View.INVISIBLE
                            binding.cardView.visibility = View.VISIBLE
                            binding.container.visibility = View.INVISIBLE
                            isContainerVisible=false
                            binding.weathercontainer.visibility = View.INVISIBLE
                            isWeatherContainerVisible=false
                        }
                        else if(lat ==1.333876 && lng ==103.773636)
                        {
                            id ="SIT"
                            val targetLat = 1.333876
                            val targetLng = 103.773636
                            val targetCoordinate = Point.fromLngLat(targetLng, targetLat)
                            CameraOptions.Builder()
                                .center(targetCoordinate)
                                .zoom(15.0)
                                .build().also { cameraOptions ->
                                    mapboxMap.setCamera(cameraOptions)
                                }
                            binding.SITcontainer.visibility = View.VISIBLE
                            binding.blk8container.visibility = View.INVISIBLE
                            binding.blk23container.visibility = View.INVISIBLE
                            binding.blk51container.visibility = View.INVISIBLE
                            binding.blk72container.visibility = View.INVISIBLE
                            binding.blk73container.visibility = View.INVISIBLE
                            binding.cardView.visibility = View.VISIBLE
                            binding.container.visibility = View.INVISIBLE
                            isContainerVisible=false
                            binding.weathercontainer.visibility = View.INVISIBLE
                            isWeatherContainerVisible=false
                        }
                        else if(lat ==1.3325963 && lng ==103.774189)
                        {
                            id ="51"
                            val targetLat = 1.3325963
                            val targetLng = 103.774189
                            val targetCoordinate = Point.fromLngLat(targetLng, targetLat)
                            CameraOptions.Builder()
                                .center(targetCoordinate)
                                .zoom(15.0)
                                .build().also { cameraOptions ->
                                    mapboxMap.setCamera(cameraOptions)
                                }
                            binding.blk51container.visibility= View.VISIBLE
                            binding.blk73container.visibility = View.INVISIBLE
                            binding.blk72container.visibility = View.INVISIBLE
                            binding.SITcontainer.visibility = View.INVISIBLE
                            binding.blk8container.visibility = View.INVISIBLE
                            binding.blk23container.visibility = View.INVISIBLE
                            binding.cardView.visibility = View.VISIBLE
                            binding.container.visibility = View.INVISIBLE
                            isContainerVisible=false
                            binding.weathercontainer.visibility = View.INVISIBLE
                            isWeatherContainerVisible=false
                        }
                        else if(lat ==1.3318412 && lng ==103.7753838)
                        {
                            id ="72"
                            val targetLat = 1.3318412
                            val targetLng = 103.7753838
                            val targetCoordinate = Point.fromLngLat(targetLng, targetLat)
                            CameraOptions.Builder()
                                .center(targetCoordinate)
                                .zoom(15.0)
                                .build().also { cameraOptions ->
                                    mapboxMap.setCamera(cameraOptions)
                                }
                            binding.blk72container.visibility = View.VISIBLE
                            binding.SITcontainer.visibility = View.INVISIBLE
                            binding.blk8container.visibility = View.INVISIBLE
                            binding.blk23container.visibility = View.INVISIBLE
                            binding.blk51container.visibility = View.INVISIBLE
                            binding.blk73container.visibility = View.INVISIBLE
                            binding.cardView.visibility = View.VISIBLE
                            binding.container.visibility = View.INVISIBLE
                            isContainerVisible=false
                            binding.weathercontainer.visibility = View.INVISIBLE
                            isWeatherContainerVisible=false
                        }
                        else if(lat ==1.332582  && lng ==103.776578)
                        {
                            id ="73"
                            val targetLat = 1.332582
                            val targetLng = 103.776578
                            val targetCoordinate = Point.fromLngLat(targetLng, targetLat)
                            CameraOptions.Builder()
                                .center(targetCoordinate)
                                .zoom(15.0)
                                .build().also { cameraOptions ->
                                    mapboxMap.setCamera(cameraOptions)
                                }
                            binding.blk73container.visibility = View.VISIBLE
                            binding.blk72container.visibility = View.INVISIBLE
                            binding.SITcontainer.visibility = View.INVISIBLE
                            binding.blk8container.visibility = View.INVISIBLE
                            binding.blk23container.visibility = View.INVISIBLE
                            binding.blk51container.visibility = View.INVISIBLE
                            binding.cardView.visibility = View.VISIBLE
                            binding.container.visibility = View.INVISIBLE
                            isContainerVisible=false
                            binding.weathercontainer.visibility = View.INVISIBLE
                            isWeatherContainerVisible=false
                        }
                        true
                    }
                )
            }
        }
        closeIcon.setOnClickListener {
            cardView.visibility = View.INVISIBLE
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
// copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    // Circle around each monitored camera location. Turf keeps the radius in real metres.
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

    private fun getColorForRisk(riskValue: Int): String {
        return when {
            riskValue >= 0 && riskValue < 3 -> "#00ff00" // Green for low risk
            riskValue >= 3 && riskValue < 7 -> "#ffff00" // Yellow for medium risk
            else -> "#ff0000" // Red for high risk
        }
    }
    private fun updateRiskContainer23Color(riskValue: Int){
        val riskContainer = findViewById<LinearLayout>(R.id.blk23riskcontainer)
        val color = getColorForRisk(riskValue)
        Log.d("Risk Value", "Current risk value for blk 23: $riskValue")
        riskContainer.setBackgroundColor(Color.parseColor(color))
    }
    private fun updateRiskContainer8Color(riskValue: Int){
        val riskContainer = findViewById<LinearLayout>(R.id.blk8riskcontainer)
        val color = getColorForRisk(riskValue)
        riskContainer.setBackgroundColor(Color.parseColor(color))
    }
    private fun updateRiskContainerSITColor(riskValue: Int){
        val riskContainer = findViewById<LinearLayout>(R.id.SITriskcontainer)
        val color = getColorForRisk(riskValue)
        riskContainer.setBackgroundColor(Color.parseColor(color))
    }
    private fun updateRiskContainer72Color(riskValue: Int){
        val riskContainer = findViewById<LinearLayout>(R.id.blk72riskcontainer)
        val color = getColorForRisk(riskValue)
        riskContainer.setBackgroundColor(Color.parseColor(color))
    }
    private fun updateRiskContainer73Color(riskValue: Int){
        val riskContainer = findViewById<LinearLayout>(R.id.blk73riskcontainer)
        val color = getColorForRisk(riskValue)
        riskContainer.setBackgroundColor(Color.parseColor(color))
    }
    private fun updateRiskContainer51Color(riskValue: Int){
        val riskContainer = findViewById<LinearLayout>(R.id.blk51riskcontainer)
        val color = getColorForRisk(riskValue)
        riskContainer.setBackgroundColor(Color.parseColor(color))
    }


    //Map settings FAB
    private var isContainerVisible = false
    private fun toggleContainerVisibility() {
        if (isContainerVisible) {
            container.visibility = View.INVISIBLE
            cardView.visibility = View.INVISIBLE
            isContainerVisible = false
        } else {
            isWeatherContainerVisible=false
            binding.blk23container.visibility = View.INVISIBLE
            binding.blk8container.visibility = View.INVISIBLE
            binding.SITcontainer.visibility = View.INVISIBLE
            binding.blk72container.visibility = View.INVISIBLE
            binding.blk73container.visibility = View.INVISIBLE
            binding.blk51container.visibility = View.INVISIBLE
            weathercontainer.visibility = View.INVISIBLE
            container.visibility = View.VISIBLE
            cardView.visibility = View.VISIBLE
            isContainerVisible = true
        }
    }

    //Weather Container FAB
    private var isWeatherContainerVisible = false
    private fun toggleweatherContainerVisibility() {

        if (isWeatherContainerVisible) {
            weathercontainer.visibility = View.INVISIBLE
            cardView.visibility = View.INVISIBLE
            isWeatherContainerVisible = false

        } else {
            isContainerVisible=false
            container.visibility = View.INVISIBLE
            binding.blk23container.visibility = View.INVISIBLE
            binding.blk8container.visibility = View.INVISIBLE
            binding.SITcontainer.visibility = View.INVISIBLE
            binding.blk72container.visibility = View.INVISIBLE
            binding.blk73container.visibility = View.INVISIBLE
            binding.blk51container.visibility = View.INVISIBLE
            weathercontainer.visibility = View.VISIBLE
            cardView.visibility = View.VISIBLE
            isWeatherContainerVisible = true

        }
    }

    private fun reverseGeocoding(point: Point) {
        val types: List<PlaceAutocompleteType> = when (mapboxMap.cameraState.zoom) {
            in 0.0..4.0 -> REGION_LEVEL_TYPES
            in 4.0..6.0 -> DISTRICT_LEVEL_TYPES
            in 6.0..12.0 -> LOCALITY_LEVEL_TYPES
            else -> ALL_TYPES
        }

        lifecycleScope.launchWhenStarted {
            val response =
                placeAutocomplete.suggestions(point, PlaceAutocompleteOptions(types = types))
            response.onValue { suggestions ->
                if (suggestions.isEmpty()) {
                    showToast(R.string.place_autocomplete_reverse_geocoding_error_message)
                } else {
                    openPlaceCard(suggestions.first())
                }
            }.onError { error ->
                Log.d(LOG_TAG, "Reverse geocoding error", error)
                showToast(R.string.place_autocomplete_reverse_geocoding_error_message)
            }
        }
    }

    private fun openPlaceCard(suggestion: PlaceAutocompleteSuggestion) {
        ignoreNextQueryUpdate = true
        queryEditText.setText("")

        lifecycleScope.launchWhenStarted {
            placeAutocomplete.select(suggestion).onValue { result ->
                mapMarkersManager.showMarker(suggestion.coordinate)
                searchPlaceView.open(SearchPlace.createFromPlaceAutocompleteResult(result))
                queryEditText.hideKeyboard()
                searchResultsView.isVisible = false
            }.onError { error ->
                Log.d(LOG_TAG, "Suggestion selection error", error)
                showToast(R.string.place_autocomplete_selection_error)
            }
        }
    }

    private fun closePlaceCard() {
        searchPlaceView.hide()
        mapMarkersManager.clearMarkers()
    }

    private class MapMarkersManager(mapView: MapView) {

        private val mapboxMap = mapView.getMapboxMap()
        private val circleAnnotationManager =
            mapView.annotations.createCircleAnnotationManager(null)
        private val markers = mutableMapOf<Long, Point>()

        fun clearMarkers() {
            markers.clear()
            circleAnnotationManager.deleteAll()
        }

        fun showMarker(coordinate: Point) {
            clearMarkers()

            val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
                .withPoint(coordinate)
                .withCircleRadius(8.0)
                .withCircleColor("#ee4e8b")
                .withCircleStrokeWidth(2.0)
                .withCircleStrokeColor("#ffffff")

            val annotation = circleAnnotationManager.create(circleAnnotationOptions)
            markers[annotation.id] = coordinate

            CameraOptions.Builder()
                .center(coordinate)
                .padding(MARKERS_INSETS_OPEN_CARD)
                .zoom(15.0)
                .build().also {
                    mapboxMap.setCamera(it)
                }
        }
    }

    fun shareIntent(searchPlace: SearchPlace): Intent {
        val text = "${searchPlace.name}. " +
                "Address: ${searchPlace.address?.formattedAddress(SearchAddress.FormatStyle.Short) ?: "unknown"}. " +
                "Geo coordinate: (lat=${searchPlace.coordinate.latitude()}, lon=${searchPlace.coordinate.longitude()})"

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }
    @SuppressLint("MissingPermission")
    fun LocationEngine.lastKnownLocation(context: Context, callback: (Point?) -> Unit) {
        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            callback(null)
            return
        }

        getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult?) {
                val location = (result?.locations?.lastOrNull() ?: result?.lastLocation)?.let { location ->
                    Point.fromLngLat(location.longitude, location.latitude)
                }
                callback(location)
            }

            override fun onFailure(exception: Exception) {
                callback(null)
            }
        })
    }

    fun Context.isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun Context.showToast(@StringRes resId: Int) {
        showToast(getString(resId))
    }

    fun Context.showToast(text: CharSequence) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }
    fun View.hideKeyboard() {
        context.inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
    val Context.inputMethodManager: InputMethodManager
        get() = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

    //Geofencing
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported")
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    private fun speakOut(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun calculateDistance() {
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


    private data class Geofence(
        val id: String,
        val latitude: Double,
        val longitude: Double,
        val radius: List<GeofenceRadius>
    )

    private data class GeofenceRadius(
        val id: String,
        val length: Int
    )

    private val _geofenceList = listOf(
        Geofence(
            id = "Blk 51",
            latitude = 1.3325963,
            longitude = 103.774189,
            radius = listOf(
                GeofenceRadius("radius_25m", 5),
                GeofenceRadius("radius_100m", 25),
                GeofenceRadius("radius_200m", 100)
            )
        ),
        Geofence(
            id = "Blk 72",
            latitude = 1.3318412,
            longitude = 103.7753838,
            radius = listOf(
                GeofenceRadius("radius_25m", 5),
                GeofenceRadius("radius_100m", 25),
                GeofenceRadius("radius_200m", 100)
            )
        ),
        Geofence(
            id = "Blk 73",
            latitude = 1.332582,
            longitude = 103.776578,
            radius = listOf(
                GeofenceRadius("radius_25m", 5),
                GeofenceRadius("radius_100m", 25),
                GeofenceRadius("radius_200m", 100)
            )
        ),
        Geofence(
            id = "Blk 23",
            latitude = 1.333706,
            longitude = 103.775743,
            radius = listOf(
                GeofenceRadius("radius_25m", 5),
                GeofenceRadius("radius_100m", 25),
                GeofenceRadius("radius_200m", 100)
            )
        ),
        Geofence(
            id = "Blk 8",
            latitude = 1.3349557,
            longitude = 103.7758805,
            radius = listOf(
                GeofenceRadius("radius_25m", 5),
                GeofenceRadius("radius_100m", 25),
                GeofenceRadius("radius_200m", 100)
            )
        ),
        Geofence(
            id = "SIT",
            latitude = 1.333876,
            longitude = 103.773636,
            radius = listOf(
                GeofenceRadius("radius_25m", 5),
                GeofenceRadius("radius_100m", 25),
                GeofenceRadius("radius_200m", 100)
            )
        ),
    )

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Already on the navigation screen.
            }
            R.id.nav_camera -> {
                val intent = Intent(this, RiskActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_saved -> {
                val intent = Intent(this, SavedLocationsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_stats -> {
                val intent = Intent(this, StatisticsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_report -> {
                val intent = Intent(this, ReportActivity::class.java)
                startActivity(intent)
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private val handler = Handler(Looper.getMainLooper())
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

    private fun gettingData() {
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
    // Historical risk charts shown in the map detail cards.
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
