package com.dailyrunner.drivertracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dailyrunner.drivertracker.data.model.DailyTrip
import com.dailyrunner.drivertracker.data.repository.TripRepository
import com.dailyrunner.drivertracker.util.PayPeriodUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class MonthFilter {
    ALL,
    THIS_MONTH,
    LAST_MONTH
}

data class HistoryUiState(
    val searchQuery: String = "",
    val selectedMonthFilter: MonthFilter = MonthFilter.ALL,
    val allTrips: List<DailyTrip> = emptyList(),
    val filteredTrips: List<DailyTrip> = emptyList(),
    val pendingDeleteTrip: DailyTrip? = null,
    val showDeleteConfirmDialog: Boolean = false
) {
    val averageKmPerWorkDay: Double
        get() {
            val workTrips = filteredTrips.filter { !it.isNoWork }
            if (workTrips.isEmpty()) return 0.0
            return workTrips.sumOf { it.totalKm } / workTrips.size
        }

    val totalKmSum: Double
        get() = filteredTrips.sumOf { it.totalKm }
}

class HistoryViewModel(
    private val repository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.getAllTrips().collectLatest { trips ->
                _uiState.value = _uiState.value.copy(
                    allTrips = trips,
                    filteredTrips = filterTrips(trips, _uiState.value.searchQuery, _uiState.value.selectedMonthFilter)
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredTrips = filterTrips(_uiState.value.allTrips, query, _uiState.value.selectedMonthFilter)
        )
    }

    fun selectMonthFilter(filter: MonthFilter) {
        _uiState.value = _uiState.value.copy(
            selectedMonthFilter = filter,
            filteredTrips = filterTrips(_uiState.value.allTrips, _uiState.value.searchQuery, filter)
        )
    }

    private fun filterTrips(trips: List<DailyTrip>, query: String, filter: MonthFilter): List<DailyTrip> {
        val today = java.time.LocalDate.now()
        val thisMonthPrefix = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
        val lastMonthPrefix = today.minusMonths(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))

        val dateFiltered = when (filter) {
            MonthFilter.ALL -> trips
            MonthFilter.THIS_MONTH -> trips.filter { it.date.startsWith(thisMonthPrefix) }
            MonthFilter.LAST_MONTH -> trips.filter { it.date.startsWith(lastMonthPrefix) }
        }

        if (query.isBlank()) return dateFiltered
        val q = query.trim().lowercase()
        return dateFiltered.filter { trip ->
            trip.date.lowercase().contains(q) ||
            trip.destinations.lowercase().contains(q) ||
            (trip.notes?.lowercase()?.contains(q) ?: false) ||
            PayPeriodUtils.formatDateForDisplay(trip.date).lowercase().contains(q)
        }
    }

    fun onRequestDelete(trip: DailyTrip) {
        _uiState.value = _uiState.value.copy(
            pendingDeleteTrip = trip,
            showDeleteConfirmDialog = true
        )
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            pendingDeleteTrip = null,
            showDeleteConfirmDialog = false
        )
    }

    fun confirmDelete() {
        val pending = _uiState.value.pendingDeleteTrip ?: return
        dismissDeleteDialog()
        viewModelScope.launch {
            repository.deleteTrip(pending.date)
            _messageEvent.emit("Deleted Record for ${PayPeriodUtils.formatDateForDisplay(pending.date)}. Weekly cheque total updated.")
        }
    }

    class Factory(private val repository: TripRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(repository) as T
        }
    }
}
