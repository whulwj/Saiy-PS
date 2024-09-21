# Useful - https://github.com/krschultz/android-proguard-snippets/tree/master/libraries

# Suppress warnings from gRPC dependencies
-dontwarn com.google.common.**
-dontwarn com.google.api.client.**
-dontwarn com.google.protobuf.**
-dontwarn io.grpc.**
-dontwarn okio.**
-keepclassmembers class com.google.cloud.speech.v2.** extends com.google.protobuf.GeneratedMessageV3 { <fields>; }

##---------------Begin: proguard configuration for ads  ----------
# https://github.com/dandar3/android-google-play-services-ads-lite/blob/main/proguard-project.txt

# Keep implementations of the AdMob mediation adapter interfaces. Adapters for
# third party ad networks implement these interfaces and are invoked by the
# AdMob SDK via reflection.
-keep class * implements com.google.android.gms.ads.mediation.MediationAdapter {
  public *;
}
-keep class * implements com.google.ads.mediation.MediationAdapter {
  public *;
}
-keep class * implements com.google.android.gms.ads.mediation.customevent.CustomEvent {
  public *;
}
-keep class * implements com.google.ads.mediation.customevent.CustomEvent {
  public *;
}
-keep class * extends com.google.android.gms.ads.mediation.UnifiedNativeAdMapper {
  public *;
}
-keep class * extends com.google.android.gms.ads.mediation.Adapter {
  public *;
}

# Keep classes used for offline ads created by reflection. WorkManagerUtil is
# created reflectively by callers within GMSCore and OfflineNotificationPoster
# is created reflectively by WorkManager.
-keep class com.google.android.gms.ads.internal.util.WorkManagerUtil {
  public *;
}
-keep class com.google.android.gms.ads.internal.offline.buffering.OfflineNotificationPoster {
  public *;
}
-keep class com.google.android.gms.ads.internal.offline.buffering.OfflinePingSender {
  public *;
}

# We keep all fields for every generated proto file as the runtime uses
# reflection over them that ProGuard cannot detect. Without this keep
# rule, fields may be removed that would cause runtime failures.
-keepclassmembers class * extends com.google.android.gms.internal.ads.zzgeq {
  <fields>;
}

# https://github.com/googleads/googleads-mobile-android-examples/issues/545
-keep class com.google.android.gms.internal.ads.** { public *; }
-keep interface com.google.android.gms.internal.ads.** { public *; }

# https://ads-developers.googleblog.com/2015/10/proguard-and-admob-mediation.html
-keep class com.google.ads.mediation.admob.AdMobAdapter {
   public *;
}
-keep class com.google.ads.mediation.AdUrlAdapter {
   public *;
}
##---------------End: proguard configuration for ads  ----------

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