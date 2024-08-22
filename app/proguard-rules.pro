# Useful - https://github.com/krschultz/android-proguard-snippets/tree/master/libraries

# Suppress warnings from gRPC dependencies
-dontwarn com.google.common.**
-dontwarn com.google.api.client.**
-dontwarn com.google.protobuf.**
-dontwarn io.grpc.**
-dontwarn okio.**
-keepclassmembers class com.google.cloud.speech.** extends com.google.protobuf.GeneratedMessageV3 { <fields>; }

# ads
-keep public class com.google.android.gms.ads.** {
   public *;
}

-keep public class com.google.ads.** {
   public *;
}

# Simple XML
-keep interface org.simpleframework.xml.core.Label { public *;}
-keep class * implements org.simpleframework.xml.core.Label { public *;}
-keep interface org.simpleframework.xml.core.Parameter { public *;}
-keep class * implements org.simpleframework.xml.core.Parameter { public *;}
-keep interface org.simpleframework.xml.core.Extractor { public *;}
-keep class * implements org.simpleframework.xml.core.Extractor { public *;}
-keep interface org.simpleframework.xml.convert.Convert { public *;}
-keep class * implements org.simpleframework.xml.convert.Converter { public *;}

-dontwarn com.bea.xml.stream.**
-dontwarn org.simpleframework.xml.stream.**
-keepclassmembers,allowobfuscation class * {
    @org.simpleframework.xml.* <fields>;
    @org.simpleframework.xml.* <init>(...);
}

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
# You can comment this out if your library doesn't use serialization.
# If your code contains serializable classes that have to be backward
# compatible, please refer to the manual.

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepattributes *Annotation*
-keepattributes InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,EnclosingMethod,Attribute,Element,Root
-keepclassmembers class * {
    @org.simpleframework.xml.* *;
}

# Volley
-keep class com.android.volley.** { *; }

##---------------Begin: proguard configuration for Guava  ----------
#https://github.com/google/guava/wiki/UsingProGuardWithGuava

-dontwarn javax.lang.model.element.Modifier

# Note: We intentionally don't add the flags we'd need to make Enums work.
# That's because the Proguard configuration required to make it work on
# optimized code would preclude lots of optimization, like converting enums
# into ints.

# FinalizableReferenceQueue calls this reflectively
# Proguard is intelligent enough to spot the use of reflection onto this, so we
# only need to keep the names, and allow it to be stripped out if
# FinalizableReferenceQueue is unused.
-keepnames class com.google.common.base.internal.Finalizer {
  *** startFinalizer(...);
}
# However, it cannot "spot" that this method needs to be kept IF the class is.
-keepclassmembers class com.google.common.base.internal.Finalizer {
  *** startFinalizer(...);
}
-keepnames class com.google.common.base.FinalizableReference {
  void finalizeReferent();
}
-keepclassmembers class com.google.common.base.FinalizableReference {
  void finalizeReferent();
}

# Striped64, LittleEndianByteArray, UnsignedBytes, AbstractFuture
-dontwarn sun.misc.Unsafe

# Striped64 appears to make some assumptions about object layout that
# really might not be safe. This should be investigated.
-keepclassmembers class com.google.common.cache.Striped64 {
  *** base;
  *** busy;
}
-keepclassmembers class com.google.common.cache.Striped64$Cell {
  <fields>;
}

-dontwarn java.lang.SafeVarargs

-keep class java.lang.Throwable {
  *** addSuppressed(...);
}

# Futures.getChecked, in both of its variants, is incompatible with proguard.

# Used by AtomicReferenceFieldUpdater and sun.misc.Unsafe
-keepclassmembers class com.google.common.util.concurrent.AbstractFuture** {
  *** waiters;
  *** value;
  *** listeners;
  *** thread;
  *** next;
}
-keepclassmembers class com.google.common.util.concurrent.AtomicDouble {
  *** value;
}
-keepclassmembers class com.google.common.util.concurrent.AggregateFutureState {
  *** remaining;
  *** seenExceptions;
}

# Since Unsafe is using the field offsets of these inner classes, we don't want
# to have class merging or similar tricks applied to these classes and their
# fields. It's safe to allow obfuscation, since the by-name references are
# already preserved in the -keep statement above.
-keep,allowshrinking,allowobfuscation class com.google.common.util.concurrent.AbstractFuture** {
  <fields>;
}

# Futures.getChecked (which often won't work with Proguard anyway) uses this. It
# has a fallback, but again, don't use Futures.getChecked on Android regardless.
-dontwarn java.lang.ClassValue
##---------------End: proguard configuration for Guava  ----------

# cardview
# http://stackoverflow.com/questions/29679177/cardview-shadow-not-appearing-in-lollipop-after-obfuscate-with-proguard/29698051
-keep class androidx.cardview.widget.RoundRectDrawable { *; }

##---------------Begin: proguard configuration for Gson  ----------
# Gson specific classes
-dontwarn sun.misc.**

# GSON TypeAdapters are only referenced in annotations so ProGuard doesn't find their method usage
-keepclassmembers,allowobfuscation,includedescriptorclasses class * extends com.google.gson.TypeAdapter {
    public <methods>;
}

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep, allowobfuscation, includedescriptorclasses class * implements com.google.gson.TypeAdapterFactory
-keep, allowobfuscation, includedescriptorclasses class * implements com.google.gson.JsonSerializer
-keep, allowobfuscation, includedescriptorclasses class * implements com.google.gson.JsonDeserializer

# Ensure that all fields annotated with SerializedName will be kept
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
# Prevent R8 from leaving Data object members always null ()
-keepclasseswithmembers, allowobfuscation, includedescriptorclasses class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
##---------------End: proguard configuration for Gson  ----------

# Needed by google-api-client to keep generic types and @Key annotations accessed via reflection

-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}

-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault

## Google Play Services 4.3.23 specific rules ##
## https://developer.android.com/google/play-services/setup.html#Proguard ##
-keep class * extends java.util.ListResourceBundle {
    protected java.lang.Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

# apache commons
-keep class org.apache.commons.logging.**
-dontwarn org.apache.commons.logging.impl.**

# apache commons codec
-dontwarn org.apache.commons.codec.binary.**

# firebase database
-keepclassmembers class ai.saiy.android.firebase.database.read.** {
    public *;
}
-keepclassmembers class ai.saiy.android.firebase.database.write.** {
    public *;
}