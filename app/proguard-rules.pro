# Preserve the line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Preserve the original source file name
-renamesourcefileattribute SourceFile

# Keep all annotations
-keepattributes *Annotation*

# Keep all public classes, methods, and fields
-keep public class * {
    public protected *;
}

# Keep all classes that extend Android's Activity, Service, BroadcastReceiver, and ContentProvider
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider

# Keep all Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep all View classes
-keep class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep all custom Application classes
-keep class * extends android.app.Application

# Keep all classes used in data binding
-keep class **.databinding.* {
    *;
}

# Keep all classes used in view binding
-keep class **.viewbinding.* {
    *;
}

# Keep all classes used by Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep all classes used by Gson
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Keep all classes used by Retrofit
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Keep all classes used by Picasso
-keep class com.squareup.picasso.** { *; }
-dontwarn com.squareup.picasso.**

# Keep all classes used by AnyChart
-keep class com.anychart.** { *; }
-dontwarn com.anychart.**

# Keep all classes used by Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep all classes used by AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**

# Keep all classes used by Biometric
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Keep all classes used by Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep all classes used by Android Credentials
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**

# Keep all classes used by JUnit
-keep class org.junit.** { *; }
-dontwarn org.junit.**

# Keep all classes used by MockK
-keep class io.mockk.** { *; }
-dontwarn io.mockk.**

# Keep all classes used by Truth
-keep class com.google.common.truth.** { *; }
-dontwarn com.google.common.truth.**

# Keep all classes used by Robolectric
-keep class org.robolectric.** { *; }
-dontwarn org.robolectric.**

# Keep all classes used by AndroidX Test
-keep class androidx.test.** { *; }
-dontwarn androidx.test.**

# Keep all classes used by ViewPager2
-keep class androidx.viewpager2.** { *; }
-dontwarn androidx.viewpager2.**

# Keep all classes used by Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**