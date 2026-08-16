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

data class HistoryUiState(
    val searchQuery: String = "",
    val allTrips: List<DailyTrip> = emptyList(),
    val filteredTrips: List<DailyTrip> = emptyList(),
    val pendingDeleteTrip: DailyTrip? = null,
    val showDeleteConfirmDialog: Boolean = false
)

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
                    filteredTrips = filterTrips(trips, _uiState.value.searchQuery)
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredTrips = filterTrips(_uiState.value.allTrips, query)
        )
    }

    private fun filterTrips(trips: List<DailyTrip>, query: String): List<DailyTrip> {
        if (query.isBlank()) return trips
        val q = query.trim().lowercase()
        return trips.filter { trip ->
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
