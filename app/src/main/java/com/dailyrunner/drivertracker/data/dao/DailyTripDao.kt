package com.dailyrunner.drivertracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dailyrunner.drivertracker.data.model.DailyTrip
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTrip(trip: DailyTrip)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trips: List<DailyTrip>)

    @Query("SELECT * FROM daily_trips WHERE date = :date LIMIT 1")
    fun getTripForDate(date: String): Flow<DailyTrip?>

    @Query("SELECT * FROM daily_trips WHERE date = :date LIMIT 1")
    suspend fun getTripForDateSync(date: String): DailyTrip?

    @Query("SELECT * FROM daily_trips WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getTripsBetweenDates(startDate: String, endDate: String): Flow<List<DailyTrip>>

    @Query("SELECT * FROM daily_trips ORDER BY date DESC")
    fun getAllTrips(): Flow<List<DailyTrip>>

    @Query("SELECT * FROM daily_trips ORDER BY date DESC")
    suspend fun getAllTripsSync(): List<DailyTrip>

    @Query("SELECT * FROM daily_trips WHERE date < :date AND is_no_work = 0 ORDER BY date DESC LIMIT 1")
    suspend fun getLatestTripBefore(date: String): DailyTrip?

    @Query("SELECT * FROM daily_trips ORDER BY date DESC LIMIT 1")
    suspend fun getLatestTrip(): DailyTrip?

    @Query("DELETE FROM daily_trips WHERE date = :date")
    suspend fun deleteTripByDate(date: String)

    @Query("DELETE FROM daily_trips WHERE id = :id")
    suspend fun deleteTripById(id: String)

    @Query("DELETE FROM daily_trips")
    suspend fun deleteAllTrips()
}
