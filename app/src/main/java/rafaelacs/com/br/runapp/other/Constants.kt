package rafaelacs.com.br.runapp.other

import android.graphics.Color
import com.github.mikephil.charting.data.LineDataSet

object Constants {
    // MapView
    const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    const val MAP_VIEW_HEIGHT_IN_DP = 200f
    const val MAP_DEFAULT_ZOOM = 15f

    const val RUN_DATABASE_NAME = "run_db"

    const val REQUEST_CODE_LOCATION_PERMISSION = 0
    const val REQUEST_ACCESS_FINE_LOCATION = 1
    const val REQUEST_CODE_AUTOCOMPLETE = 2
    const val REQUEST_CHECK_SETTINGS = 3
    const val REQUEST_PLACE_PICKER = 4

    // Service Intent Actions
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    // Timer
    const val TIMER_UPDATE_INTERVAL = 50L

    // Shared Preferences
    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"

    // Tracking Options
    const val LOCATION_UPDATE_INTERVAL = 5000L  //5 seconds
    const val FASTEST_LOCATION_INTERVAL = 2000L  //2 seconds

    // Map Options
    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 15f

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    // LineChart
    val LINE_DATA_MODE = LineDataSet.Mode.CUBIC_BEZIER

}