package rafaelacs.com.br.runapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import rafaelacs.com.br.runapp.ui.CustomMarkerView
import rafaelacs.com.br.runapp.R
import rafaelacs.com.br.runapp.other.TrackingUtility
import rafaelacs.com.br.runapp.ui.viewmodels.StatisticsViewModel
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewModel by viewModels()

    /*
     * Calls the view created by Statistics Fragment.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()
    }

    /*
     * Setup the bar chart and its x/left/right axis.
     */
    private fun setupBarChart() {
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.apply {
            description.text = "Velocidade Média ao Longo do Tempo"
            legend.isEnabled = false
        }
    }

    /*
     * Shows the result of the settings in a chart.
     * In case db is empty, it will be null.
     */
    private fun subscribeToObservers() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner, {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text = totalTimeRun
            }
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"
                tvTotalDistance.text = totalDistanceString
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                tvAverageSpeed.text = avgSpeedString
            }
        })
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, {
            it?.let {
                val totalCalories = "${it}kcal"
                tvTotalCalories.text = totalCalories
            }
        })
        viewModel.runsSortedByDate.observe(viewLifecycleOwner, {
            it?.let {
                val allAvgSpeeds = it.indices.map { i ->
                    BarEntry(i.toFloat(), it[i].avgSpeedInKmH)
                }

                val bardataSet = BarDataSet(allAvgSpeeds, "Velocidade Média ao Longo do Tempo")
                bardataSet.apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                val lineData = BarData(bardataSet)
                barChart.data = lineData
                val marker = CustomMarkerView(
                    it.reversed(),
                    requireContext(),
                    R.layout.marker_view
                )
                barChart.marker = marker
                barChart.invalidate()
            }
        })
    }
}