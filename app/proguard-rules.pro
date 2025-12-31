# Add project specific ProGuard rules here.

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Data models - keep all fields for serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.skyzonebd.android.data.model.** { *; }
-keepclassmembers class com.skyzonebd.android.data.model.** { *; }

# Keep enum methods for custom deserializers
-keepclassmembers enum com.skyzonebd.android.data.model.UserType {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public static ** fromString(java.lang.String);
}
-keepclassmembers enum com.skyzonebd.android.data.model.UserRole {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public static ** fromString(java.lang.String);
}

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Keep ApiService interface - CRITICAL for release builds
-keep interface com.skyzonebd.android.data.remote.ApiService { *; }
-keep class com.skyzonebd.android.data.remote.ApiService { *; }
-keep class com.skyzonebd.android.data.remote.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items)
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Keep all API response models - CRITICAL for network calls
-keep class com.skyzonebd.android.data.remote.ApiResponse { *; }
-keep class com.skyzonebd.android.data.remote.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Keep AuthInterceptor for authenticated requests
-keep class com.skyzonebd.android.data.remote.AuthInterceptor { *; }

# Keep SSL/TLS classes for HTTPS
-keep class javax.net.ssl.** { *; }
-keep class org.conscrypt.** { *; }
-dontwarn org.conscrypt.**

# Hilt
-dontwarn com.google.errorprone.annotations.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}

# Keep Repository classes - they're injected by Hilt
-keep class com.skyzonebd.android.data.repository.** { *; }
-keepclassmembers class com.skyzonebd.android.data.repository.** { *; }

# Keep sealed classes and their subclasses
-keep class com.skyzonebd.android.util.Resource { *; }
-keep class com.skyzonebd.android.util.Resource$* { *; }

# Keep Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }
-keepclassmembers class ** {
    @kotlin.Metadata <fields>;
}

# Jetpack Compose
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.compose.material3.** { *; }

# Keep ViewModels - injected by Hilt
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class com.skyzonebd.android.ui.**.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# Keep Kotlin suspend functions - used in Repository and ViewModel
-keepclassmembers class * {
    *** *Async(...);
    *** *Suspended(...);
}
-keep class kotlin.coroutines.Continuation

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# DataStore
-keep class androidx.datastore.*.** { *; }

# Cart functionality - CRITICAL for cart to work in release builds
-keep class com.skyzonebd.android.data.model.CartItem { *; }
-keep class com.skyzonebd.android.data.model.Cart { *; }
-keep class com.skyzonebd.android.data.local.CartPreferences { *; }
-keepclassmembers class com.skyzonebd.android.data.model.CartItem { *; }
-keepclassmembers class com.skyzonebd.android.data.model.Cart { *; }

# Coil Image Loading
-dontwarn coil.**
-keep class coil.** { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Preserve crash reporting info
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Security: Keep BuildConfig for version info
-keep class com.skyzonebd.android.BuildConfig { *; }
-keepclassmembers class com.skyzonebd.android.BuildConfig {
    public static <fields>;
}

