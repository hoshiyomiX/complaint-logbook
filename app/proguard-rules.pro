# ── Project-specific rules ──
-keepattributes *Annotation*, Signature

# ── Room ──
-keep class com.hoshiyomix.complaintlogbook.data.local.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ── Kotlin ──
-dontwarn kotlin.**
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# ── Coroutines ──
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ── Compose ──
# NOTE: Compose Gradle plugin generates proper R8/ProGuard rules automatically.
# Do NOT add "-keep class androidx.compose.** { *; }" as it defeats R8 shrinking.
-dontwarn androidx.compose.**

# ── AndroidX / Lifecycle ──
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel { <init>(android.app.Application); }

# ── Aggressive optimization ──
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively
-repackageclasses ''
-dontwarn javax.annotation.**
-dontwarn java.lang.invoke.StringConcatFactory
