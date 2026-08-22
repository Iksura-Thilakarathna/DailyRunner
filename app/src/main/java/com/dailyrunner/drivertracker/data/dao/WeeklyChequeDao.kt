package com.dailyrunner.drivertracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dailyrunner.drivertracker.data.model.WeeklyCheque
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyChequeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheque(cheque: WeeklyCheque)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cheques: List<WeeklyCheque>)

    @Query("SELECT * FROM weekly_cheques WHERE week_id = :weekId LIMIT 1")
    fun getChequeForWeek(weekId: String): Flow<WeeklyCheque?>

    @Query("SELECT * FROM weekly_cheques WHERE week_id = :weekId LIMIT 1")
    suspend fun getChequeForWeekSync(weekId: String): WeeklyCheque?

    @Query("SELECT * FROM weekly_cheques")
    fun getAllCheques(): Flow<List<WeeklyCheque>>

    @Query("SELECT * FROM weekly_cheques")
    suspend fun getAllChequesSync(): List<WeeklyCheque>

    @Query("UPDATE weekly_cheques SET is_paid = 1, paid_at = :paidAt WHERE week_id = :weekId")
    suspend fun markAsPaid(weekId: String, paidAt: Long = System.currentTimeMillis())

    @Query("UPDATE weekly_cheques SET is_paid = 0, paid_at = NULL WHERE week_id = :weekId")
    suspend fun markAsUnpaid(weekId: String)

    @Query("DELETE FROM weekly_cheques")
    suspend fun deleteAllCheques()
}
