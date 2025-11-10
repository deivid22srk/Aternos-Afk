# Add project specific ProGuard rules here.
-keep class com.aternoscontroller.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
