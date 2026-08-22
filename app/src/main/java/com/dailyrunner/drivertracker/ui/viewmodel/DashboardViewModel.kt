package com.dailyrunner.drivertracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dailyrunner.drivertracker.data.model.DailyTrip
import com.dailyrunner.drivertracker.data.repository.TripRepository
import com.dailyrunner.drivertracker.util.PayPeriod
import com.dailyrunner.drivertracker.util.PayPeriodUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class DayTab {
    TODAY,
    YESTERDAY
}

data class DashboardUiState(
    val selectedTab: DayTab = DayTab.TODAY,
    val selectedDateIso: String = PayPeriodUtils.getTodayIso(),
    val currentPayPeriod: PayPeriod = PayPeriodUtils.getCurrentPayPeriod(),
    val ratePerKm: Double = 104.0,
    val startKmText: String = "",
    val endKmText: String = "",
    val destinationsText: String = "",
    val notesText: String = "",
    val isNoWork: Boolean = false,
    val existingTrip: DailyTrip? = null,
    val previousEndKmPrefill: Double? = null,
    val validationError: String? = null,
    val runningWeekKm: Double = 0.0,
    val runningWeekEarnings: Double = 0.0,
    val isSaving: Boolean = false
)

class DashboardViewModel(
    private val repository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ratePerKmFlow.collectLatest { rate ->
                _uiState.value = _uiState.value.copy(ratePerKm = rate)
                recalculateLiveValues()
            }
        }
        observeRunningWeekTotals()
        loadSelectedDateTrip(_uiState.value.selectedDateIso)
    }

    fun selectTab(tab: DayTab) {
        val dateIso = if (tab == DayTab.TODAY) PayPeriodUtils.getTodayIso() else PayPeriodUtils.getYesterdayIso()
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            selectedDateIso = dateIso
        )
        loadSelectedDateTrip(dateIso)
    }

    private fun observeRunningWeekTotals() {
        viewModelScope.launch {
            repository.getAllTrips().collectLatest { trips ->
                val currentPeriod = PayPeriodUtils.getCurrentPayPeriod()
                val weekTrips = trips.filter { t ->
                    t.date >= currentPeriod.startDate.format(PayPeriodUtils.ISO_FORMATTER) &&
                    t.date <= currentPeriod.endDate.format(PayPeriodUtils.ISO_FORMATTER)
                }
                val totalKm = weekTrips.sumOf { it.totalKm }
                val totalEarnings = weekTrips.sumOf { it.totalEarnings }

                _uiState.value = _uiState.value.copy(
                    runningWeekKm = totalKm,
                    runningWeekEarnings = totalEarnings
                )
            }
        }
    }

    private fun loadSelectedDateTrip(dateIso: String) {
        viewModelScope.launch {
            val trip = repository.getTripForDateSync(dateIso)
            val prevEndKm = repository.getPreviousEndKm(dateIso)

            if (trip != null) {
                _uiState.value = _uiState.value.copy(
                    existingTrip = trip,
                    startKmText = if (trip.isNoWork) "" else formatKm(trip.startKm),
                    endKmText = if (trip.isNoWork) "" else formatKm(trip.endKm),
                    destinationsText = if (trip.isNoWork) "" else trip.destinations,
                    notesText = trip.notes ?: "",
                    isNoWork = trip.isNoWork,
                    previousEndKmPrefill = prevEndKm,
                    validationError = null
                )
            } else {
                // Auto prefill start KM from previous trip end KM!
                val prefillStart = prevEndKm?.let { formatKm(it) } ?: ""
                _uiState.value = _uiState.value.copy(
                    existingTrip = null,
                    startKmText = prefillStart,
                    endKmText = "",
                    destinationsText = "",
                    notesText = "",
                    isNoWork = false,
                    previousEndKmPrefill = prevEndKm,
                    validationError = null
                )
            }
            recalculateLiveValues()
        }
    }

    fun onStartKmChanged(input: String) {
        _uiState.value = _uiState.value.copy(startKmText = input, validationError = null)
        recalculateLiveValues()
    }

    fun onEndKmChanged(input: String) {
        _uiState.value = _uiState.value.copy(endKmText = input, validationError = null)
        recalculateLiveValues()
    }

    fun onDestinationsChanged(input: String) {
        _uiState.value = _uiState.value.copy(destinationsText = input)
    }

    fun onNotesChanged(input: String) {
        _uiState.value = _uiState.value.copy(notesText = input)
    }

    fun appendDestinationChip(chipName: String) {
        val current = _uiState.value.destinationsText
        val newText = if (current.isBlank()) {
            chipName
        } else if (current.endsWith("-> ") || current.endsWith(", ") || current.endsWith(" ")) {
            "$current$chipName"
        } else {
            "$current -> $chipName"
        }
        _uiState.value = _uiState.value.copy(destinationsText = newText)
    }

    fun toggleNoWork(isNoWork: Boolean) {
        _uiState.value = _uiState.value.copy(
            isNoWork = isNoWork,
            validationError = null
        )
        recalculateLiveValues()
    }

    private fun recalculateLiveValues() {
        val state = _uiState.value
        if (state.isNoWork) return

        val start = state.startKmText.toDoubleOrNull()
        val end = state.endKmText.toDoubleOrNull()

        if (start != null && end != null) {
            if (end < start) {
                _uiState.value = _uiState.value.copy(validationError = "End KM must be ≥ Start KM")
            } else {
                _uiState.value = _uiState.value.copy(validationError = null)
            }
        }
    }

    fun saveTrip() {
        val state = _uiState.value
        viewModelScope.launch {
            if (state.isNoWork) {
                repository.saveTrip(
                    date = state.selectedDateIso,
                    startKm = 0.0,
                    endKm = 0.0,
                    destinations = "No Work Day",
                    notes = state.notesText.ifBlank { null },
                    isNoWork = true
                )
                _messageEvent.emit("Marked as No Work Day for ${PayPeriodUtils.formatShortDate(state.selectedDateIso)}")
                loadSelectedDateTrip(state.selectedDateIso)
                return@launch
            }

            val start = state.startKmText.toDoubleOrNull()
            val end = state.endKmText.toDoubleOrNull()

            if (start == null) {
                _uiState.value = _uiState.value.copy(validationError = "Please enter valid Start KM")
                return@launch
            }
            if (end == null) {
                _uiState.value = _uiState.value.copy(validationError = "Please enter valid End KM")
                return@launch
            }
            if (end < start) {
                _uiState.value = _uiState.value.copy(validationError = "End KM must be greater than or equal to Start KM")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isSaving = true)

            repository.saveTrip(
                date = state.selectedDateIso,
                startKm = start,
                endKm = end,
                destinations = state.destinationsText.ifBlank { "Delivery Route" },
                notes = state.notesText.ifBlank { null },
                isNoWork = false
            )

            _uiState.value = _uiState.value.copy(isSaving = false)
            val actionName = if (state.existingTrip != null) "Updated" else "Saved"
            _messageEvent.emit("Successfully $actionName Trip for ${PayPeriodUtils.formatShortDate(state.selectedDateIso)}")
            loadSelectedDateTrip(state.selectedDateIso)
        }
    }

    private fun formatKm(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }

    class Factory(private val repository: TripRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
