package com.dailyrunner.drivertracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dailyrunner.drivertracker.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

data class SettingsUiState(
    val ratePerKmText: String = "104.0",
    val currentRatePerKm: Double = 104.0,
    val totalLifetimeKm: Double = 0.0,
    val totalLifetimeEarnings: Double = 0.0,
    val totalDaysWorked: Int = 0,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false
)

class SettingsViewModel(
    private val repository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ratePerKmFlow.collectLatest { rate ->
                _uiState.value = _uiState.value.copy(
                    currentRatePerKm = rate,
                    ratePerKmText = if (rate % 1.0 == 0.0) rate.toLong().toString() else rate.toString()
                )
            }
        }
        viewModelScope.launch {
            repository.getAllTrips().collectLatest { trips ->
                val sumKm = trips.sumOf { it.totalKm }
                val sumEarnings = trips.sumOf { it.totalEarnings }
                val days = trips.count { !it.isNoWork }
                _uiState.value = _uiState.value.copy(
                    totalLifetimeKm = sumKm,
                    totalLifetimeEarnings = sumEarnings,
                    totalDaysWorked = days
                )
            }
        }
    }

    fun onRateTextChanged(input: String) {
        _uiState.value = _uiState.value.copy(ratePerKmText = input)
    }

    fun saveRate() {
        val rate = _uiState.value.ratePerKmText.toDoubleOrNull()
        if (rate == null || rate <= 0) {
            viewModelScope.launch { _messageEvent.emit("Please enter a valid rate greater than 0") }
            return
        }
        repository.setRatePerKm(rate)
        viewModelScope.launch { _messageEvent.emit("Base Rate updated to Rs. $rate / KM") }
    }

    fun exportBackupJson(outputStream: OutputStream) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                repository.exportBackupJson(outputStream)
                _messageEvent.emit("JSON Backup exported successfully!")
            } catch (e: Exception) {
                _messageEvent.emit("Backup failed: ${e.localizedMessage}")
            } finally {
                _uiState.value = _uiState.value.copy(isExporting = false)
            }
        }
    }

    fun exportBackupCsv(outputStream: OutputStream) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                repository.exportBackupCsv(outputStream)
                _messageEvent.emit("CSV Trip Log exported successfully!")
            } catch (e: Exception) {
                _messageEvent.emit("CSV Export failed: ${e.localizedMessage}")
            } finally {
                _uiState.value = _uiState.value.copy(isExporting = false)
            }
        }
    }

    fun importBackupJson(inputStream: InputStream) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                val success = repository.importBackupJson(inputStream)
                if (success) {
                    _messageEvent.emit("Data restored successfully!")
                } else {
                    _messageEvent.emit("Failed to parse backup file")
                }
            } catch (e: Exception) {
                _messageEvent.emit("Restore failed: ${e.localizedMessage}")
            } finally {
                _uiState.value = _uiState.value.copy(isImporting = false)
            }
        }
    }

    class Factory(private val repository: TripRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}
