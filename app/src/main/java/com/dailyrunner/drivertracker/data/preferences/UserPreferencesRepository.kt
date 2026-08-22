package com.dailyrunner.drivertracker.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("driver_tracker_prefs", Context.MODE_PRIVATE)

    private val _ratePerKm = MutableStateFlow(getRatePerKmSync())
    val ratePerKm: StateFlow<Double> = _ratePerKm.asStateFlow()

    fun getRatePerKmSync(): Double {
        return prefs.getFloat(KEY_RATE_PER_KM, DEFAULT_RATE).toDouble()
    }

    fun setRatePerKm(rate: Double) {
        prefs.edit().putFloat(KEY_RATE_PER_KM, rate.toFloat()).apply()
        _ratePerKm.value = rate
    }

    companion object {
        private const val KEY_RATE_PER_KM = "rate_per_km"
        const val DEFAULT_RATE = 104.0f
    }
}
