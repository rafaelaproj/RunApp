package rafaelacs.com.br.runapp.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import rafaelacs.com.br.runapp.repositories.MainRepository

class StatisticsViewModel @ViewModelInject constructor(
        private val mainRepository: MainRepository
) : ViewModel() {

    val totalDistance = mainRepository.getTotalDistance()
    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()

}