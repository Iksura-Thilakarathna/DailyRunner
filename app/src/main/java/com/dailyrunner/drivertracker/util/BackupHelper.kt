package com.dailyrunner.drivertracker.util

import com.dailyrunner.drivertracker.data.model.DailyTrip
import com.dailyrunner.drivertracker.data.model.WeeklyCheque
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.InputStream
import java.io.OutputStream

data class BackupDataPayload(
    val version: Int = 1,
    val exportedAt: String = PayPeriodUtils.getTodayIso(),
    val trips: List<DailyTrip>,
    val cheques: List<WeeklyCheque>,
    val ratePerKm: Double = 104.0
)

object BackupHelper {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun exportToJson(payload: BackupDataPayload, outputStream: OutputStream) {
        val json = gson.toJson(payload)
        outputStream.write(json.toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }

    fun importFromJson(inputStream: InputStream): BackupDataPayload? {
        return try {
            val json = inputStream.bufferedReader().use { it.readText() }
            gson.fromJson(json, BackupDataPayload::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportToCsv(trips: List<DailyTrip>, outputStream: OutputStream) {
        val sb = StringBuilder()
        sb.append("Date,Start KM,End KM,Total KM,Rate Per KM,Total Earnings (Rs),Destinations,Is No Work,Notes\n")
        for (t in trips) {
            val dest = t.destinations.replace("\"", "\"\"")
            val notes = (t.notes ?: "").replace("\"", "\"\"")
            sb.append("\"${t.date}\",${t.startKm},${t.endKm},${t.totalKm},${t.ratePerKm},${t.totalEarnings},\"$dest\",${t.isNoWork},\"$notes\"\n")
        }
        outputStream.write(sb.toString().toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }
}
