# Proguard rules for Symbolic Connection

# Keep all Kotlin data classes
-keep class com.glyphos.symbolic.**.data.** { *; }

# Keep Hilt-generated classes
-keep class dagger.hilt.** { *; }
-keep class com.glyphos.symbolic.** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }

# Keep Room classes
-keep class androidx.room.** { *; }

# Keep Retrofit and OkHttp
-keep class com.squareup.okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }

# Keep ML Kit
-keep class com.google.mlkit.** { *; }

# Keep WebRTC
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**

# Keep Socket.IO
-keep class io.socket.** { *; }
-keep class io.socket.client.** { *; }
-dontwarn io.socket.**
-dontwarn javax.xml.stream.**

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Security
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
