# R8 / ProGuard rules for DailyRunner lightweight build optimization

# Gson serialization rules
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.dailyrunner.drivertracker.util.BackupDataPayload { *; }

# Room & Database models
-keep class com.dailyrunner.drivertracker.data.model.** { *; }
-keep class com.dailyrunner.drivertracker.data.dao.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}

# Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.android.HandlerDispatcher {
    <init>(...);
}
