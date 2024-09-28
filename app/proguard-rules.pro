# Useful - https://github.com/krschultz/android-proguard-snippets/tree/master/libraries

# Suppress warnings from gRPC dependencies
-dontwarn com.google.common.**
-dontwarn com.google.api.client.**
-dontwarn com.google.protobuf.**
-dontwarn io.grpc.**
-dontwarn okio.**
-keepclassmembers class com.google.cloud.speech.v2.** extends com.google.protobuf.GeneratedMessageV3 { <fields>; }

##---------------Begin: proguard configuration for ads  ----------
# https://ads-developers.googleblog.com/2015/10/proguard-and-admob-mediation.html
-keep class com.google.ads.mediation.admob.AdMobAdapter {
   public *;
}
-keep class com.google.ads.mediation.AdUrlAdapter {
   public *;
}
##---------------End: proguard configuration for ads  ----------

# https://github.com/firebase/firebase-android-sdk/issues/1562
-keepclassmembers enum io.opencensus.trace.** {
  public static **[] $VALUES;
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

-keepattributes *Annotation*
-keepattributes InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,EnclosingMethod,Attribute,Element,Root
-keepclassmembers class * {
    @org.simpleframework.xml.* *;
}

##---------------Begin: proguard configuration for Guava  ----------
#https://github.com/google/guava/wiki/UsingProGuardWithGuava

-dontwarn javax.lang.model.element.Modifier

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
# https://github.com/google/gson/blob/main/gson/src/main/resources/META-INF/proguard/gson.pro

# Keep generic signatures; needed for correct type resolution
-keepattributes Signature

# Keep Gson annotations
# Note: Cannot perform finer selection here to only cover Gson annotations, see also https://stackoverflow.com/q/47515093
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

### The following rules are needed for R8 in "full mode" which only adheres to `-keepattribtues` if
### the corresponding class or field is matches by a `-keep` rule as well, see
### https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#r8-full-mode

# Keep class TypeToken (respectively its generic signature) if present
-if class com.google.gson.reflect.TypeToken
-keep,allowobfuscation class com.google.gson.reflect.TypeToken

# Keep any (anonymous) classes extending TypeToken
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken

# Keep classes with @JsonAdapter annotation
-keep,allowobfuscation,allowoptimization @com.google.gson.annotations.JsonAdapter class *

# Keep fields with any other Gson annotation
# Also allow obfuscation, assuming that users will additionally use @SerializedName or
# other means to preserve the field names
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.Expose <fields>;
  @com.google.gson.annotations.JsonAdapter <fields>;
  @com.google.gson.annotations.Since <fields>;
  @com.google.gson.annotations.Until <fields>;
}

# Keep no-args constructor of classes which can be used with @JsonAdapter
# By default their no-args constructor is invoked to create an adapter instance
-keepclassmembers class * extends com.google.gson.TypeAdapter {
  <init>();
}
-keepclassmembers class * implements com.google.gson.TypeAdapterFactory {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonSerializer {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonDeserializer {
  <init>();
}

# Keep fields annotated with @SerializedName for classes which are referenced.
# If classes with fields annotated with @SerializedName have a no-args
# constructor keep that as well. Based on
# https://issuetracker.google.com/issues/150189783#comment11.
# See also https://github.com/google/gson/pull/2420#discussion_r1241813541
# for a more detailed explanation.
-if class *
-keepclasseswithmembers,allowobfuscation class <1> {
  @com.google.gson.annotations.SerializedName <fields>;
}
-if class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers,allowobfuscation,allowoptimization class <1> {
  <init>();
}

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class ai.saiy.android.nlu.nuance.NLUNuance
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
-keep public class okhttp3.** { public *; }
-keep interface okhttp3.** { public *; }
-dontwarn okhttp3.**

# apache commons codec
-dontwarn org.apache.commons.codec.binary.**

# firebase database
-keepclassmembers public class ai.saiy.android.firebase.database.read.** {
    public *;
}
-keepclassmembers public class ai.saiy.android.firebase.database.write.** {
    public *;
}

# Twitter4j.
-dontwarn javax.management.**
-dontwarn java.lang.management.**

##---------------Begin: proguard configuration for firebase  ----------
#https://github.com/firebase/firebase-android-sdk/blob/master/default-preguard.txt
-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,
                SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

# Keep the classes/members we need for client functionality.
-keep @interface androidx.annotation.Keep
-keep @androidx.annotation.Keep class *
-keepclasseswithmembers class * {
  @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
  @androidx.annotation.Keep <methods>;
}

# Keep the classes/members we need for client functionality.
-keep @interface com.google.android.gms.common.annotation.KeepForSdk
-keep @com.google.android.gms.common.annotation.KeepForSdk class *
-keepclasseswithmembers class * {
  @com.google.android.gms.common.annotation.KeepForSdk <fields>;
}
-keepclasseswithmembers class * {
  @com.google.android.gms.common.annotation.KeepForSdk <methods>;
}

# Keep the public API
-keep @interface com.google.firebase.annotations.PublicApi
-keep @com.google.firebase.annotations.PublicApi class *
-keepclasseswithmembers class * {
  @com.google.firebase.annotations.PublicApi <fields>;
}
-keepclasseswithmembers class * {
  @com.google.firebase.annotations.PublicApi <methods>;
}

# Keep Enum members implicitly
-keepclassmembers @androidx.annotation.Keep public class * extends java.lang.Enum {
    public <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers @com.google.android.gms.common.annotation.KeepForSdk class * extends java.lang.Enum {
    public <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers @com.google.firebase.annotations.PublicApi class * extends java.lang.Enum {
    public <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Implicitly keep methods inside annotations
-keepclassmembers @interface * {
    public <methods>;
}
##---------------End: proguard configuration for firebase  ----------