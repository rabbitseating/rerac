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
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
 * Before running the example make sure you have put your access_token in the correct place
 * inside [app/src/main/res/values/mapbox_access_token.xml]. If not present then add this file
 * at the location mentioned above and add the following content to it
 *
 * <?xml version="1.0" encoding="utf-8"?>
 * <resources xmlns:tools="http://schemas.android.com/tools">
 *     <string name="mapbox_access_token"><PUT_YOUR_ACCESS_TOKEN_HERE></string>
 * </resources>
 *
 * The example assumes that you have granted location permissions and does not enforce it. However,
 * the permission is essential for proper functioning of this example. The example also uses replay
 * location engine to facilitate navigation without actually physically moving.
 *
 * How to use this example:
 * - You can long-click the map to select a destination.
 * - The guidance will start to the selected destination while simulating location updates.
 * You can disable simulation by commenting out the [replayLocationEngine] setter in [NavigationOptions].
 * Then, the device's real location will be used.
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
     * Debug tool used to play, pause and seek route progress events that can be used to produce mocked location updates along the route.
     */
    private val mapboxReplayer = MapboxReplayer()

    /**
     * Debug tool that mocks location updates with an input from the [mapboxReplayer].
     */
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)

    /**
     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
     */
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

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
                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
                mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
                // start the trip session to being receiving location updates in free drive
                // and later when a route is set also receiving route progress updates
                mapboxNavigation.startTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
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

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
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
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        //handler.removeCallbacks(updateDataRunnable)
        mapboxReplayer.finish()
        maneuverApi.cancel()
        routeLineApi.cancel()
        routeLineView.cancel()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
        if(tts != null){
            tts!!.stop()
            tts!!.shutdown()
        }
    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                // comment out the location engine setting block to disable simulation
                .locationEngine(replayLocationEngine)
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
//comment it out for testing
        replayOriginLocation()
    }
//change location for current location
    private fun replayOriginLocation() {
        mapboxReplayer.pushEvents(
            listOf(
                ReplayRouteMapper.mapToUpdateLocation(
                    Date().time.toDouble(),
                    Point.fromLngLat( 103.774232,1.334372)
                )
            )
        )
        mapboxReplayer.playFirstLocation()
        mapboxReplayer.playbackSpeed(3.0)
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
        mapboxReplayer.stop()

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
            val annotationApi = mapView?.annotations
            val pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView!!)
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
                            val targetCoordinate = Point.fromLngLat(targetLat, targetLng)
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
                            val targetCoordinate = Point.fromLngLat(targetLat, targetLng)
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
                            val targetCoordinate = Point.fromLngLat(targetLat, targetLng)
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
                            val targetCoordinate = Point.fromLngLat(targetLat, targetLng)
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
                            val targetCoordinate = Point.fromLngLat(targetLat, targetLng)
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
                            val targetCoordinate = Point.fromLngLat(targetLat, targetLng)
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

    //Circle Around Markers
    private fun addCircleToMarker(lng: Double, lat: Double) {
        val annotationApi = mapView?.annotations
        val polygonAnnotationManager = annotationApi?.createPolygonAnnotationManager(mapView!!)

        // Define a fixed geographical radius in meters
        val fixedRadiusInMeters = 15.0

        // Calculate the circle radius based on the zoom level
        val currentZoomLevel = mapView?.getMapboxMap()?.cameraState?.zoom ?: 0.0
        val adjustedRadiusInMeters = fixedRadiusInMeters / Math.pow(2.0, 15 - currentZoomLevel)

        // Calculate the polygon points to create a circle
        val points = ArrayList<Point>()
        val numPoints = 100
        val anglePerPoint = 360.0 / numPoints
        for (i in 0 until numPoints) {
            val angle = i * anglePerPoint
            val x = lng + adjustedRadiusInMeters * Math.cos(Math.toRadians(angle))
            val y = lat + adjustedRadiusInMeters * Math.sin(Math.toRadians(angle))
            points.add(Point.fromLngLat(x, y))
        }

        // Set options for the resulting polygon layer
        val polygonAnnotationOptions: PolygonAnnotationOptions = PolygonAnnotationOptions()
            .withPoints(listOf(points))
            .withFillColor("#00ff00")
            .withFillOpacity(0.2)
            .withDraggable(false)
            .withFillOutlineColor("#00ff00") // You can choose to use the same color for outline as well

        if(lat==  1.333706 && lng ==103.775743 )
        {
            polygonAnnotation23=polygonAnnotationManager?.create(polygonAnnotationOptions)
            polygonAnnotationManager23 = polygonAnnotationManager
            Log.d("Circle created","Blk 23 circle created")
        }
        if(lng==103.7758805 && lat ==1.3349557)
        {
            polygonAnnotation8=polygonAnnotationManager?.create(polygonAnnotationOptions)
            polygonAnnotationManager8 = polygonAnnotationManager
        }
        if(lng==103.773636 && lat ==1.333876)
        {
            polygonAnnotationSIT=polygonAnnotationManager?.create(polygonAnnotationOptions)
            polygonAnnotationManagerSIT = polygonAnnotationManager
        }
        if(lng==103.774189 && lat ==1.3325963)
        {
            polygonAnnotation51=polygonAnnotationManager?.create(polygonAnnotationOptions)
            polygonAnnotationManager51 = polygonAnnotationManager
        }
        if(lng==103.7753838 && lat ==1.3318412)
        {
            polygonAnnotation72=polygonAnnotationManager?.create(polygonAnnotationOptions)
            polygonAnnotationManager72 = polygonAnnotationManager
        }
        if(lng==103.776578 && lat ==1.332582)
        {
            polygonAnnotation73=polygonAnnotationManager?.create(polygonAnnotationOptions)
            polygonAnnotationManager73 = polygonAnnotationManager
        }

    }

    private fun getColorForRisk(riskValue: Int): String {
        return when {
            riskValue >= 0 && riskValue < 3 -> "#00ff00" // Green for low risk
            riskValue >= 3 && riskValue <= 7 -> "#ffff00" // Yellow for medium risk
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

    @SuppressLint("MissingPermission", "SuspiciousIndentation")
    private fun calculateDistance() {
        val riskImage:ImageView = findViewById(R.id.riskimg)
        val ttc:TextView = findViewById(R.id.ttc)
        runOnUiThread {
            for (geofence in _geofenceList) {
                val currentPosition = userLocation

                val currentPoint = currentPosition?.let {
                    Point.fromLngLat(it.longitude, it.latitude)
                }
                val geofencePoint = Point.fromLngLat(geofence.longitude, geofence.latitude)
                Log.d("User Location", "User Location: $currentPoint")
                Log.d("Geofence point", "Geofence point: ${geofence.id}")

                val distanceInMeters = TurfMeasurement.distance(
                    currentPoint!!,
                    geofencePoint,
                    TurfConstants.UNIT_METERS
                )
                Log.d("Distance in Meters", "Distance in Meters: $distanceInMeters")


                if (distanceInMeters <= 100 && distanceInMeters > 50) {
                    riskImage.visibility = View.INVISIBLE
                    ttc.visibility = View.INVISIBLE
                    Log.d("Distance", "You are 100m away from ${geofence.id}")
                    val message = "You are 100 meters away from ${geofence.id}"

                    if (soundtoggle) {
                        speakOut(message)
                    }
                    break
                }
                if (distanceInMeters <= 50 && distanceInMeters > 30) {
                    userIsWithinGeofence = true
                    riskImage.visibility = View.VISIBLE
                    ttc.visibility = View.VISIBLE
                    Log.d("Distance", "You are 50m away from ${geofence.id}")
                    val message = "You are 50 meters away from ${geofence.id}"
                    if (soundtoggle) {
                        speakOut(message)
                    }
                    val geofenceId = geofence.id
                    val riskValue = geofenceRiskMap[geofenceId] ?: 0
                    val ttcValue = geofenceTTCMap[geofenceId]?.toDouble() ?: 0.0
                    ttc.text = String.format("TTC:%.2f", ttcValue)
                    when {
                        riskValue >= 0 && riskValue < 3 -> riskImage.setImageResource(R.drawable.risk_green)
                        riskValue >= 3 && riskValue <= 7 -> riskImage.setImageResource(R.drawable.risk_yellow)
                        else -> riskImage.setImageResource(R.drawable.risk_red)
                    }
                    // Once we find a geofence within 50 meters, we can exit the loop
                    break

                }

                if (distanceInMeters <= 30) {
                    userIsWithinGeofence = true
                    riskImage.visibility = View.VISIBLE
                    ttc.visibility = View.VISIBLE

                    Log.d("Distance", "You are nearby ${geofence.id}")
                    val message = "You are nearby ${geofence.id}"
                    if (soundtoggle) {
                        speakOut(message)
                    }

                    val geofenceId = geofence.id
                    val riskValue = geofenceRiskMap[geofenceId] ?: 0
                    val ttcValue = geofenceTTCMap[geofenceId]?.toDouble() ?: 0.0
                    ttc.text = String.format("TTC:%.2f", ttcValue)

                    when {
                        riskValue >= 0 && riskValue < 3 -> riskImage.setImageResource(R.drawable.risk_green)
                        riskValue >= 3 && riskValue <= 7 -> riskImage.setImageResource(R.drawable.risk_yellow)
                        else -> riskImage.setImageResource(R.drawable.risk_red)
                    }
                    // Once we find a geofence within 50 meters, we can exit the loop
                    break
                }
                if (distanceInMeters > 50) {
                    riskImage.visibility = View.INVISIBLE
                    ttc.visibility = View.INVISIBLE
                }

            }
            if (!userIsWithinGeofence) {
                riskImage.visibility = View.INVISIBLE
                ttc.visibility = View.INVISIBLE
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
                val intent = Intent(this, TurnByTurnActivity::class.java)
                startActivity(intent)
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

    private val handler = Handler()
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

    private fun GettingData() {

        val client = OkHttpClient()
        val getRequest: Request = Request.Builder()
            .url("${BuildConfig.BACKEND_BASE_URL}/risks")
            .build()

        client.newCall(getRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @SuppressLint("SuspiciousIndentation")
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string() ?: ""
                val jsonArray = JSONArray(responseData)
                if (jsonArray.length() > 0) {
                    val jsonObject = jsonArray.getJSONObject(0)
                    geofenceRiskMap["Blk 51"] = jsonObject.optInt("blk51", 0)
                    geofenceRiskMap["Blk 72"] = jsonObject.optInt("blk72", 0)
                    geofenceRiskMap["Blk 73"] = jsonObject.optInt("blk73", 0)
                    geofenceRiskMap["Blk 23"] = jsonObject.optInt("blk23", 0)
                    geofenceRiskMap["Blk 8"] = jsonObject.optInt("blk8", 0)
                    geofenceRiskMap["SIT"] = jsonObject.optInt("blkSIT", 0)
                    geofenceTTCMap["Blk 51"] = jsonObject.optDouble("ttc51", 0.0).toDouble()
                    geofenceTTCMap["Blk 72"] = jsonObject.optDouble("ttc72", 0.0).toDouble()
                    geofenceTTCMap["Blk 73"] = jsonObject.optDouble("ttc73", 0.0).toDouble()
                    geofenceTTCMap["Blk 23"] = jsonObject.optDouble("ttc23", 0.0).toDouble()
                    geofenceTTCMap["Blk 8"] = jsonObject.optDouble("ttc8", 0.0).toDouble()
                    geofenceTTCMap["SIT"] = jsonObject.optDouble("ttcSIT", 0.0).toDouble()

                    risk8Value = jsonObject.optInt("blk8", 0)
                    risk23Value = jsonObject.optInt("blk23", 0)
                    risk73Value = jsonObject.optInt("blk73", 0)
                    risk72Value = jsonObject.optInt("blk72", 0)
                    risk51Value = jsonObject.optInt("blk51", 0)
                    riskSITValue = jsonObject.optInt("blkSIT", 0)

                        updateCircle23()
                        updateCircle8()
                        updateCircle51()
                        updateCircle72()
                        updateCircle73()
                        updateCircleSIT()


                    val textView23 = findViewById<TextView>(R.id.blk23risktextView)
                    runOnUiThread {
                        updateRiskContainer23Color(risk23Value)
                        val displayText = "Current Risk Value: $risk23Value"
                        textView23.text = displayText
                    }

                    val textView8 = findViewById<TextView>(R.id.blk8risktextView)
                    runOnUiThread {
                        updateRiskContainer8Color(risk8Value)
                        val displayText = "Current Risk Value: $risk8Value"
                        textView8.text = displayText
                    }

                    val textViewSIT = findViewById<TextView>(R.id.SITrisktextView)
                    runOnUiThread {
                        updateRiskContainerSITColor(riskSITValue)
                        val displayText = "Current Risk Value: $riskSITValue"
                        textViewSIT.text = displayText
                    }

                    val textView72 = findViewById<TextView>(R.id.blk72risktextView)
                    runOnUiThread {
                        updateRiskContainer72Color(risk72Value)
                        val displayText = "Current Risk Value: $risk72Value"
                        textView72.text = displayText
                    }

                    val textView73 = findViewById<TextView>(R.id.blk73risktextView)
                    runOnUiThread {
                        updateRiskContainer73Color(risk73Value)
                        val displayText = "Current Risk Value: $risk73Value"
                        textView73.text = displayText
                    }

                    val textView51 = findViewById<TextView>(R.id.blk51risktextView)
                    runOnUiThread {
                        updateRiskContainer51Color(risk51Value)
                        val displayText = "Current Risk Value: $risk51Value"
                        textView51.text = displayText
                    }

                } else {
                    // Handle empty or invalid JSON response here
                }
                calculateDistance()
            }
        })
    }
    private fun updateCircle8(){
        polygonAnnotation8?.fillColorString = getColorForRisk(risk8Value)
        polygonAnnotation8?.fillOutlineColorString= getColorForRisk(risk8Value)
        polygonAnnotationManager8?.update(polygonAnnotation8!!)
        Log.d("Circle color", "Circle color updated")
    }
    private fun updateCircle23(){
        polygonAnnotation23?.fillColorString = getColorForRisk(risk23Value)
        polygonAnnotation23?.fillOutlineColorString= getColorForRisk(risk23Value)
        polygonAnnotationManager23?.update(polygonAnnotation23!!)
    }
    private fun updateCircle51(){
        polygonAnnotation51?.fillColorString = getColorForRisk(risk51Value)
        polygonAnnotation51?.fillOutlineColorString= getColorForRisk(risk51Value)
        polygonAnnotationManager51?.update(polygonAnnotation51!!)
    }
    private fun updateCircle72(){
        polygonAnnotation72?.fillColorString = getColorForRisk(risk72Value)
        polygonAnnotation72?.fillOutlineColorString= getColorForRisk(risk72Value)
        polygonAnnotationManager72?.update(polygonAnnotation72!!)
    }
    private fun updateCircle73(){
        polygonAnnotation73?.fillColorString = getColorForRisk(risk73Value)
        polygonAnnotation73?.fillOutlineColorString= getColorForRisk(risk73Value)
        polygonAnnotationManager73?.update(polygonAnnotation73!!)
    }
    private fun updateCircleSIT(){
        polygonAnnotationSIT?.fillColorString = getColorForRisk(riskSITValue)
        polygonAnnotationSIT?.fillOutlineColorString= getColorForRisk(riskSITValue)
        polygonAnnotationManagerSIT?.update(polygonAnnotationSIT!!)
    }
    //Graph
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




