# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/almayce/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.android.gms.auth.api.signin.internal.SignInConfiguration
-keep class com.google.android.gms.common.api.Scope

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
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

-keep interface com.google.** { *;}

-keep class * extends com.google.api.client.json.GenericJson {
*;
}
-dontwarn com.google.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.android.gms.R**
-dontnote com.google.android.gms.cast.**
-dontnote com.google.android.gms.common.**
-dontnote com.google.android.gms.dynamic.**
-dontnote com.google.android.gms.internal.**
# Keep all enum values and valueOf methods. See
# http://proguard.sourceforge.net/index.html#manual/examples.html
# for the reason for this. Also, see http://crbug.com/248037.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# Preserve sufficient information for simple Serializable classes.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class com.android.vending.billing.**
-keep public class com.google.android.gms.**


-keep class com.google.** {*;}