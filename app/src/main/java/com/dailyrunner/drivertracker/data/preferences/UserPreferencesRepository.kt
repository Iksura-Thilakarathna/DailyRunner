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

    private val _driverName = MutableStateFlow(prefs.getString(KEY_DRIVER_NAME, "Kamal Perera") ?: "Kamal Perera")
    val driverName: StateFlow<String> = _driverName.asStateFlow()

    private val _vehicleNumber = MutableStateFlow(prefs.getString(KEY_VEHICLE_NUMBER, "WP ABC-1234") ?: "WP ABC-1234")
    val vehicleNumber: StateFlow<String> = _vehicleNumber.asStateFlow()

    private val _phoneNumber = MutableStateFlow(prefs.getString(KEY_PHONE_NUMBER, "077 1234567") ?: "077 1234567")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _employeeType = MutableStateFlow(prefs.getString(KEY_EMPLOYEE_TYPE, "Distributor") ?: "Distributor")
    val employeeType: StateFlow<String> = _employeeType.asStateFlow()

    fun getRatePerKmSync(): Double {
        return prefs.getFloat(KEY_RATE_PER_KM, DEFAULT_RATE).toDouble()
    }

    fun setRatePerKm(rate: Double) {
        prefs.edit().putFloat(KEY_RATE_PER_KM, rate.toFloat()).apply()
        _ratePerKm.value = rate
    }

    fun updateDriverProfile(name: String, vehicle: String, phone: String, empType: String) {
        prefs.edit()
            .putString(KEY_DRIVER_NAME, name)
            .putString(KEY_VEHICLE_NUMBER, vehicle)
            .putString(KEY_PHONE_NUMBER, phone)
            .putString(KEY_EMPLOYEE_TYPE, empType)
            .apply()
        _driverName.value = name
        _vehicleNumber.value = vehicle
        _phoneNumber.value = phone
        _employeeType.value = empType
    }

    companion object {
        private const val KEY_RATE_PER_KM = "rate_per_km"
        private const val KEY_DRIVER_NAME = "driver_name"
        private const val KEY_VEHICLE_NUMBER = "vehicle_number"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_EMPLOYEE_TYPE = "employee_type"
        const val DEFAULT_RATE = 104.0f
    }
}
