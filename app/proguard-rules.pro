# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializable classes
-keep,includedescriptorclasses class com.encryptpad.app.data.model.**$$serializer { *; }
-keepclassmembers class com.encryptpad.app.data.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.encryptpad.app.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep navigation routes
-keep class com.encryptpad.app.ui.navigation.Screen { *; }
-keep class com.encryptpad.app.ui.navigation.Screen$* { *; }
