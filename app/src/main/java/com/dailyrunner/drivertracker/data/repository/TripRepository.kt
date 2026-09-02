package com.dailyrunner.drivertracker.data.repository

import com.dailyrunner.drivertracker.data.dao.DailyTripDao
import com.dailyrunner.drivertracker.data.dao.WeeklyChequeDao
import com.dailyrunner.drivertracker.data.model.DailyTrip
import com.dailyrunner.drivertracker.data.model.WeeklyCheque
import com.dailyrunner.drivertracker.data.preferences.UserPreferencesRepository
import com.dailyrunner.drivertracker.util.BackupDataPayload
import com.dailyrunner.drivertracker.util.BackupHelper
import com.dailyrunner.drivertracker.util.PayPeriodUtils
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

data class WeeklyChequeSummary(
    val weekId: String,
    val startDate: String, // Friday
    val endDate: String,   // Thursday
    val displayRange: String,
    val totalDaysWorked: Int,
    val totalDistanceKm: Double,
    val totalChequeAmount: Double,
    val isPaid: Boolean,
    val paidAt: Long?,
    val trips: List<DailyTrip>
)

class TripRepository(
    private val dailyTripDao: DailyTripDao,
    private val weeklyChequeDao: WeeklyChequeDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    val ratePerKmFlow: Flow<Double> = userPreferencesRepository.ratePerKm
    val driverNameFlow: Flow<String> = userPreferencesRepository.driverName
    val vehicleNumberFlow: Flow<String> = userPreferencesRepository.vehicleNumber
    val phoneNumberFlow: Flow<String> = userPreferencesRepository.phoneNumber
    val employeeTypeFlow: Flow<String> = userPreferencesRepository.employeeType

    fun getRatePerKm(): Double = userPreferencesRepository.getRatePerKmSync()

    fun setRatePerKm(rate: Double) {
        userPreferencesRepository.setRatePerKm(rate)
    }

    fun updateDriverProfile(name: String, vehicle: String, phone: String, empType: String) {
        userPreferencesRepository.updateDriverProfile(name, vehicle, phone, empType)
    }

    fun getTripForDate(date: String): Flow<DailyTrip?> {
        return dailyTripDao.getTripForDate(date)
    }

    suspend fun getTripForDateSync(date: String): DailyTrip? {
        return dailyTripDao.getTripForDateSync(date)
    }

    suspend fun getPreviousEndKm(forDate: String): Double? {
        val previousTrip = dailyTripDao.getLatestTripBefore(forDate)
        return previousTrip?.endKm
    }

    suspend fun saveTrip(
        date: String,
        startKm: Double,
        endKm: Double,
        destinations: String,
        notes: String?,
        isNoWork: Boolean
    ) {
        val currentRate = getRatePerKm()
        val totalKm = if (isNoWork) 0.0 else (endKm - startKm).coerceAtLeast(0.0)
        val totalEarnings = totalKm * currentRate

        val existing = dailyTripDao.getTripForDateSync(date)
        val trip = DailyTrip(
            id = existing?.id ?: java.util.UUID.randomUUID().toString(),
            date = date,
            startKm = if (isNoWork) 0.0 else startKm,
            endKm = if (isNoWork) 0.0 else endKm,
            totalKm = totalKm,
            ratePerKm = currentRate,
            totalEarnings = totalEarnings,
            destinations = if (isNoWork) "No Work Day" else destinations,
            notes = notes,
            isNoWork = isNoWork,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dailyTripDao.insertOrUpdateTrip(trip)
    }

    suspend fun deleteTrip(date: String) {
        dailyTripDao.deleteTripByDate(date)
    }

    fun getAllTrips(): Flow<List<DailyTrip>> {
        return dailyTripDao.getAllTrips()
    }

    fun getAllWeeklyCheques(): Flow<List<WeeklyCheque>> {
        return weeklyChequeDao.getAllCheques()
    }

    suspend fun toggleChequePaidStatus(weekId: String, startDate: String, endDate: String): Boolean {
        val existing = weeklyChequeDao.getChequeForWeekSync(weekId)
        val newPaidStatus = !(existing?.isPaid ?: false)
        val cheque = WeeklyCheque(
            weekId = weekId,
            startDate = startDate,
            endDate = endDate,
            isPaid = newPaidStatus,
            paidAt = if (newPaidStatus) System.currentTimeMillis() else null
        )
        weeklyChequeDao.upsertCheque(cheque)
        return newPaidStatus
    }

    suspend fun exportBackupJson(outputStream: OutputStream) {
        val trips = dailyTripDao.getAllTripsSync()
        val cheques = weeklyChequeDao.getAllChequesSync()
        val payload = BackupDataPayload(
            trips = trips,
            cheques = cheques,
            ratePerKm = getRatePerKm()
        )
        BackupHelper.exportToJson(payload, outputStream)
    }

    suspend fun exportBackupCsv(outputStream: OutputStream) {
        val trips = dailyTripDao.getAllTripsSync()
        BackupHelper.exportToCsv(trips, outputStream)
    }

    suspend fun importBackupJson(inputStream: InputStream): Boolean {
        val payload = BackupHelper.importFromJson(inputStream) ?: return false
        if (payload.trips.isNotEmpty()) {
            dailyTripDao.insertAll(payload.trips)
        }
        if (payload.cheques.isNotEmpty()) {
            weeklyChequeDao.insertAll(payload.cheques)
        }
        if (payload.ratePerKm > 0) {
            userPreferencesRepository.setRatePerKm(payload.ratePerKm)
        }
        return true
    }
}
