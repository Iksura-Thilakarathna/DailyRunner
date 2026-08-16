package com.dailyrunner.drivertracker.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "daily_trips",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailyTrip(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "date")
    val date: String, // ISO date format YYYY-MM-DD

    @ColumnInfo(name = "start_km")
    val startKm: Double,

    @ColumnInfo(name = "end_km")
    val endKm: Double,

    @ColumnInfo(name = "total_km")
    val totalKm: Double = (endKm - startKm).coerceAtLeast(0.0),

    @ColumnInfo(name = "rate_per_km")
    val ratePerKm: Double = 104.0,

    @ColumnInfo(name = "total_earnings")
    val totalEarnings: Double = totalKm * ratePerKm,

    @ColumnInfo(name = "destinations")
    val destinations: String = "",

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "is_no_work")
    val isNoWork: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
