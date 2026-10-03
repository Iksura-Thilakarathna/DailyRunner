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
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            gson.toJson(payload, writer)
            writer.flush()
        }
    }

    fun importFromJson(inputStream: InputStream): BackupDataPayload? {
        return try {
            inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                gson.fromJson(reader, BackupDataPayload::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportToCsv(trips: List<DailyTrip>, outputStream: OutputStream) {
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            // Write UTF-8 BOM for Excel compatibility
            writer.write("\uFEFF")
            writer.write("Date,Day of Week,Start KM,End KM,Total KM,Rate Per KM (LKR),Total Earnings (LKR),Destinations / Route,Is Off Day,Notes\n")
            
            var sumKm = 0.0
            var sumEarnings = 0.0

            for (t in trips) {
                val dayOfWeek = PayPeriodUtils.formatDateForDisplay(t.date)
                val dest = t.destinations.replace("\"", "\"\"")
                val notes = (t.notes ?: "").replace("\"", "\"\"")
                writer.write("\"${t.date}\",\"$dayOfWeek\",${t.startKm},${t.endKm},${t.totalKm},${t.ratePerKm},${t.totalEarnings},\"$dest\",${if (t.isNoWork) "Yes" else "No"},\"$notes\"\n")
                sumKm += t.totalKm
                sumEarnings += t.totalEarnings
            }

            // Summary footer row
            writer.write("\n\"SUMMARY TOTALS\",,,\"Total Trips: ${trips.size}\",$sumKm,,$sumEarnings,,,\n")
            writer.flush()
        }
    }
}
