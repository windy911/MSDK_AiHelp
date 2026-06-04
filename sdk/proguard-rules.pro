# MSDK AiHelp SDK ProGuard Rules

# Keep public API
-keep class com.msdk.aihelp.MSDKAiHelp { *; }
-keep class com.msdk.aihelp.config.** { *; }
-keep class com.msdk.aihelp.model.** { *; }
-keep class com.msdk.aihelp.callback.** { *; }

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
