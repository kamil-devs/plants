# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Gson
-keep class com.google.gson.** { *; }
-keep class com.example.pruningapp.data.JsonImporter$** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
