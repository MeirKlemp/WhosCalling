# Sqldelight & JDBC
-keep class org.sqlite.JDBC { *; }
-keep interface org.sqlite.** { *; }
-keepclassmembers class org.sqlite.** { *; }

# kotlinx-datetime
-dontwarn kotlinx.datetime.**
-keep class kotlinx.datetime.** { *; }

# Ktor
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# SLF4J
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }

# JNA
-dontwarn com.sun.jna.**
-keep class com.sun.jna.** { *; }
