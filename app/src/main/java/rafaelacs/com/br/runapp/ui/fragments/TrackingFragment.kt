package rafaelacs.com.br.runapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import rafaelacs.com.br.runapp.db.Run
import rafaelacs.com.br.runapp.other.Constants.ACTION_PAUSE_SERVICE
import rafaelacs.com.br.runapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import rafaelacs.com.br.runapp.other.Constants.ACTION_STOP_SERVICE
import rafaelacs.com.br.runapp.other.Constants.MAP_VIEW_BUNDLE_KEY
import rafaelacs.com.br.runapp.other.Constants.MAP_ZOOM
import rafaelacs.com.br.runapp.other.Constants.POLYLINE_COLOR
import rafaelacs.com.br.runapp.other.Constants.POLYLINE_WIDTH
import rafaelacs.com.br.runapp.other.TrackingUtility
import rafaelacs.com.br.runapp.services.Polyline
import rafaelacs.com.br.runapp.services.TrackingService
import rafaelacs.com.br.runapp.R
import rafaelacs.com.br.runapp.ui.viewmodels.MainViewModel
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

const val CANCEL_TRACKING_DIALOG = "CancelDialog"

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    private var curTimeInMillis = 0L

    private var menu: Menu? = null

    @set:Inject
    var weight: Float = 80f

    /*
     *
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    /*
     * Restore dialog instance.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        mapView.onCreate(mapViewBundle)

        if(savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG) as CancelTrackingDialog
            cancelTrackingDialog.setYesListener {
                stopRun()
            }
        }

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveDb()
        }

        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        subscribeToObservers()
    }

    /*
     *
     */
    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            tvTimer.text = formattedTime
        })
    }

    /*
     * Toggles the tracking state.
     */
    private fun toggleRun() {
        if(isTracking) {
            menu?.getItem(0)?.isVisible = true
            pauseTrackingService()
        } else {
            startOrResumeTrackingService()
            Timber.d("Started service")
        }
    }

    /*
     * Starts the tracking service or resumes it if it is currently paused.
     */
    private fun startOrResumeTrackingService() =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = ACTION_START_OR_RESUME_SERVICE
            requireContext().startService(it)
        }

    /*
     * Pauses the tracking service.
     */
    private fun pauseTrackingService() =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = ACTION_PAUSE_SERVICE
            requireContext().startService(it)
        }

    /*
     * Stops the tracking service.
     */
    private fun stopTrackingService() =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = ACTION_STOP_SERVICE
            requireContext().startService(it)
        }

    /*
     *
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    /*
     * Checks for the isTracking doesm't trigger this when rotating the device in paused mode.
     */
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(curTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    /*
     *
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*
     * Shows a dialog to cancel the current run.
     */
    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG)
    }

    /*
     * Finishes the tracking.
     */
    @SuppressLint("SetTextI18n")
    private fun stopRun() {
        Timber.d("PARANDO CORRIDA")
        tvTimer.text = "00:00:00:00"
        stopTrackingService()
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    /*
     * Updates the tracking variable and the UI accordingly.
     */
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if(!isTracking && curTimeInMillis > 0L) {
            btnToggleRun.text = getString(R.string.start)
            btnFinishRun.visibility = View.VISIBLE
        } else if(isTracking) {
            btnToggleRun.text = getString(R.string.stop)
            menu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }
    }

    /*
     * Will move the camera to the user's location.
     */
    private fun moveCameraToUser() {
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    /*
     * Zooms out until the whole track is visible.
     * Used to make a screenshot of the MapView to save it in the database.
     */
    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()

        for(polyline in pathPoints) {
            for(pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    /*
     * Saves the recente run in the Room database and ends it.
     */
    private fun endRunAndSaveDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for(polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed =
                ((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10).roundToInt() / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run =
                Run(bmp, dateTimestamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Corrida salva com sucesso.",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    /*
     * Adds all polylines to the pathPoints list to display them after screen rotations.
     */
    private fun addAllPolylines() {
        for(polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    /*
     * Draws a polyline between the two latest points.
     * It only adds a polyline if it has at least two elements in the last polyline.
     */
    private fun addLatestPolyline() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)
        }
    }

    /*
     *
     */
    private fun sendCommandToService(action: String)  =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    /*
     * Lifecycle methods
     */
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}