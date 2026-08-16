package com.dailyrunner.drivertracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dailyrunner.drivertracker.data.model.DailyTrip
import com.dailyrunner.drivertracker.data.model.WeeklyCheque
import com.dailyrunner.drivertracker.data.repository.TripRepository
import com.dailyrunner.drivertracker.data.repository.WeeklyChequeSummary
import com.dailyrunner.drivertracker.util.PayPeriodUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

enum class ChequeFilter {
    ALL,
    UNPAID,
    PAID
}

data class WeeklyChequesUiState(
    val selectedFilter: ChequeFilter = ChequeFilter.ALL,
    val cheques: List<WeeklyChequeSummary> = emptyList(),
    val expandedWeekId: String? = null,
    val pendingToggleCheque: WeeklyChequeSummary? = null,
    val showUnpaidConfirmDialog: Boolean = false
)

class WeeklyChequesViewModel(
    private val repository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyChequesUiState())
    val uiState: StateFlow<WeeklyChequesUiState> = _uiState.asStateFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllTrips(),
                repository.getAllWeeklyCheques()
            ) { trips, savedCheques ->
                buildWeeklySummaries(trips, savedCheques)
            }.collect { summaries ->
                _uiState.value = _uiState.value.copy(cheques = filterSummaries(summaries, _uiState.value.selectedFilter))
            }
        }
    }

    fun selectFilter(filter: ChequeFilter) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedFilter = filter)
            // Refresh stream
            val trips = repository.getTripForDateSync(PayPeriodUtils.getTodayIso())
            // Collect will automatically re-run buildWeeklySummaries via flow
        }
    }

    private fun buildWeeklySummaries(
        allTrips: List<DailyTrip>,
        savedCheques: List<WeeklyCheque>
    ): List<WeeklyChequeSummary> {
        if (allTrips.isEmpty()) return emptyList()

        // Group trips by Friday-to-Thursday pay period
        val chequeMap = savedCheques.associateBy { it.weekId }
        val groupedTrips = allTrips.groupBy { trip ->
            PayPeriodUtils.getPayPeriodForDateString(trip.date).weekId
        }

        val summaries = groupedTrips.map { (weekId, tripsInWeek) ->
            val payPeriod = PayPeriodUtils.getPayPeriodForDateString(tripsInWeek.first().date)
            val savedCheque = chequeMap[weekId]

            val daysWorked = tripsInWeek.count { !it.isNoWork }
            val totalKm = tripsInWeek.sumOf { it.totalKm }
            val totalAmount = tripsInWeek.sumOf { it.totalEarnings }
            val isPaid = savedCheque?.isPaid ?: false
            val paidAt = savedCheque?.paidAt

            WeeklyChequeSummary(
                weekId = weekId,
                startDate = payPeriod.startDate.format(PayPeriodUtils.ISO_FORMATTER),
                endDate = payPeriod.endDate.format(PayPeriodUtils.ISO_FORMATTER),
                displayRange = payPeriod.displayRange,
                totalDaysWorked = daysWorked,
                totalDistanceKm = totalKm,
                totalChequeAmount = totalAmount,
                isPaid = isPaid,
                paidAt = paidAt,
                trips = tripsInWeek.sortedBy { it.date }
            )
        }.sortedByDescending { it.startDate }

        return summaries
    }

    private fun filterSummaries(
        summaries: List<WeeklyChequeSummary>,
        filter: ChequeFilter
    ): List<WeeklyChequeSummary> {
        return when (filter) {
            ChequeFilter.ALL -> summaries
            ChequeFilter.UNPAID -> summaries.filter { !it.isPaid }
            ChequeFilter.PAID -> summaries.filter { it.isPaid }
        }
    }

    fun toggleCardExpansion(weekId: String) {
        val current = _uiState.value.expandedWeekId
        _uiState.value = _uiState.value.copy(
            expandedWeekId = if (current == weekId) null else weekId
        )
    }

    fun onRequestPaymentToggle(summary: WeeklyChequeSummary) {
        if (summary.isPaid) {
            // Confirm toggling back to unpaid
            _uiState.value = _uiState.value.copy(
                pendingToggleCheque = summary,
                showUnpaidConfirmDialog = true
            )
        } else {
            // Directly mark as paid
            performPaymentToggle(summary)
        }
    }

    fun dismissConfirmDialog() {
        _uiState.value = _uiState.value.copy(
            pendingToggleCheque = null,
            showUnpaidConfirmDialog = false
        )
    }

    fun confirmUnpaidToggle() {
        val pending = _uiState.value.pendingToggleCheque ?: return
        dismissConfirmDialog()
        performPaymentToggle(pending)
    }

    private fun performPaymentToggle(summary: WeeklyChequeSummary) {
        viewModelScope.launch {
            val newIsPaid = repository.toggleChequePaidStatus(
                weekId = summary.weekId,
                startDate = summary.startDate,
                endDate = summary.endDate
            )
            val statusMsg = if (newIsPaid) "Marked Cheque as PAID ✓" else "Marked Cheque as UNPAID"
            _messageEvent.emit(statusMsg)
        }
    }

    fun buildShareSummaryText(summary: WeeklyChequeSummary): String {
        val sb = StringBuilder()
        sb.append("📦 DAILY RUNNER - CHEQUE SUMMARY\n")
        sb.append("Pay Period: ${summary.displayRange}\n")
        sb.append("------------------------------\n")
        sb.append("Days Worked: ${summary.totalDaysWorked} days\n")
        sb.append("Total Distance: ${String.format(Locale.ENGLISH, "%.1f", summary.totalDistanceKm)} KM\n")
        sb.append("Rate: Rs. ${String.format(Locale.ENGLISH, "%.0f", repository.getRatePerKm())}/KM\n")
        sb.append("Cheque Amount: Rs. ${String.format(Locale.ENGLISH, "%,.2f", summary.totalChequeAmount)}\n")
        val statusText = if (summary.isPaid) "PAID (on ${PayPeriodUtils.formatTimestamp(summary.paidAt)})" else "UNPAID"
        sb.append("Payment Status: $statusText\n\n")

        sb.append("Daily Breakdown:\n")
        for (trip in summary.trips) {
            val shortDate = PayPeriodUtils.formatShortDate(trip.date)
            if (trip.isNoWork) {
                sb.append("• $shortDate: OFF DAY\n")
            } else {
                sb.append("• $shortDate: ${String.format(Locale.ENGLISH, "%.1f", trip.totalKm)} KM (${trip.startKm} -> ${trip.endKm}) - Rs. ${String.format(Locale.ENGLISH, "%.0f", trip.totalEarnings)}\n")
            }
        }
        sb.append("\nGenerated via Daily Runner App.")
        return sb.toString()
    }

    class Factory(private val repository: TripRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WeeklyChequesViewModel(repository) as T
        }
    }
}
