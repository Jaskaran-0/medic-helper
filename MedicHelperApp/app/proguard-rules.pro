# Retrofit
-keep class retrofit2.** { *; }
-keepclassmembers class ** {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Gson
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
-dontwarn com.google.gson.**

# Firebase Messaging
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# AndroidX (General)
-keep class androidx.** { *; }
-dontwarn androidx.**

# Prevent stripping of models used in API responses (DTOs)
-keepclassmembers class data_models.** { *; }

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Keep Log statements in debug mode (Optional for Release)
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
