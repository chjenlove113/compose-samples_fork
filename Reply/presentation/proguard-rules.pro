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
-keep class com.app.tintuccongnghe.data.local.entities.** { *; }
-keep class com.app.tintuccongnghe.data.repository.Next** { *; }
-keep class com.app.tintuccongnghe.base.** { *; }

# Keep annotations for Gson
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-keep class com.google.gson.annotations.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }

# Suppress missing classes warnings for Jetpack XR extensions
-dontwarn com.android.extensions.xr.**

# ROME (RomeTools) ProGuard / R8 rules to prevent ClassNotFoundException for feed converters (e.g. ConverterForAtom10)
-keep class com.rometools.** { *; }
-keep class * implements com.rometools.rome.feed.synd.impl.Converter { *; }
-keep class * implements com.rometools.rome.io.WireFeedParser { *; }
-keep class * implements com.rometools.rome.io.WireFeedGenerator { *; }
-keep class * implements com.rometools.rome.feed.module.Module { *; }
-keepattributes *Annotation*,Signature,EnclosingMethod,InnerClasses,SourceFile,LineNumberTable
-dontwarn com.rometools.**
