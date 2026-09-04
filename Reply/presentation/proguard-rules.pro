# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Retrofit's Gson converter reads these API/domain models by their JSON field names. Keep the
# classes and fields when R8 minifies release builds; otherwise Gson creates objects whose
# Kotlin non-null properties were never populated or mapping fails.
-keep class com.app.tintuccongnghe.domain.models.** { *; }
-keep class com.app.tintuccongnghe.data.models.** { *; }
-keep class com.app.tintuccongnghe.base.** { *; }

# Keep annotations for Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.annotations.** { *; }
