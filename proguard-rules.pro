-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-dontusemixedcaseclassnames
-ignorewarnings
-verbose

-keepattributes *Annotation*,EnclosingMethod, InnerClasses, Exceptions, Signature, SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

################################################

-dontnote android.**
-dontwarn android.**

-dontnote com.sun.**
-dontwarn com.sun.**

-dontnote sun.**
-dontwarn sun.**

-dontnote java.**
-dontwarn java.**

-dontnote javax.**
-dontwarn javax.**

-dontnote org.slf4j.**
-dontwarn org.slf4j.**

-assumenosideeffects interface org.slf4j.Logger {
    public void trace(...);
    public void debug(...);
    public void info(...);
    public void warn(...);
    public void error(...);

    public boolean isTraceEnabled(...);
    public boolean isDebugEnabled(...);
    public boolean isWarnEnabled(...);
}

-assumenosideeffects class org.slf4j.LoggerFactory {
    public static ** getLogger(...);
}

-adaptresourcefilecontents **.fxml,**.properties,META-INF/MANIFEST.MF

-keepclassmembernames class * {
    @javafx.fxml.FXML *;
}


# Keep - Applications. Keep all application classes, along with their 'main'
# methods.
-keepclasseswithmembers public class com.javafx.main.Main, obfuscationexample.ObfuscationExample {
    public static void main(java.lang.String[]);
}

# keep all public classes in main package
-keep class at.favre.tools.dconvert.Main { *; }
-keep class at.favre.tools.dconvert.ui.** { *; }


-keep class com.twelvemonkeys.imagio.plugins.jpeg.JPEGImageReaderSpi { *;}