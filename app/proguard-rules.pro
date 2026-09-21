# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep readable stack traces in Play Console crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Gson uses generic type information at runtime (TypeToken<ArrayList<HierbasBean>>, etc.)
-keepattributes Signature
-keepattributes *Annotation*
-keep class * extends com.google.gson.reflect.TypeToken

# Model classes are (de)serialized by Gson via reflection and passed through Intents
# as java.io.Serializable, so their field names must survive obfuscation.
-keep class com.javadoh.plantasmedicinales.io.beans.** { <fields>; }

# android-gif-drawable calls into native code by class/method name via JNI.
-keep class pl.droidsonroids.gif.** { *; }

# okhttp (pulled in transitively) optionally supports Conscrypt, which isn't on the
# classpath here; these classes are only referenced, never actually loaded.
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.OpenSSLProvider
