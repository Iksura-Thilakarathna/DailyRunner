package com.dailyrunner.drivertracker

import android.app.Application
import com.dailyrunner.drivertracker.data.database.AppDatabase
import com.dailyrunner.drivertracker.data.preferences.UserPreferencesRepository
import com.dailyrunner.drivertracker.data.repository.TripRepository

class DailyRunnerApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }
    val repository by lazy {
        TripRepository(
            dailyTripDao = database.dailyTripDao(),
            weeklyChequeDao = database.weeklyChequeDao(),
            userPreferencesRepository = userPreferencesRepository
        )
    }
}
