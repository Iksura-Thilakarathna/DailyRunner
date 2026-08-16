package com.dailyrunner.drivertracker.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_cheques")
data class WeeklyCheque(
    @PrimaryKey
    @ColumnInfo(name = "week_id")
    val weekId: String, // Format: YYYY-MM-DD_YYYY-MM-DD (Start Fri to End Thu)

    @ColumnInfo(name = "start_date")
    val startDate: String, // ISO Friday YYYY-MM-DD

    @ColumnInfo(name = "end_date")
    val endDate: String, // ISO Thursday YYYY-MM-DD

    @ColumnInfo(name = "is_paid")
    val isPaid: Boolean = false,

    @ColumnInfo(name = "paid_at")
    val paidAt: Long? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
