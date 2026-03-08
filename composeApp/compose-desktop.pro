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

# SQLDelight
-dontwarn app.cash.sqldelight.**
-keep class app.cash.sqldelight.** { *; }

# SQLite JDBC Driver
-dontwarn org.sqlite.**
-keep class org.sqlite.** { *; }

# Koin
-dontwarn org.koin.**
-keep class org.koin.** { *; }

# App ViewModels (Koin instantiates these via reflection with viewModelOf)
-keep class com.klemfner.whoscalling.ui.calllogs.CallLogsViewModel { *; }
-keep class com.klemfner.whoscalling.ui.contacts.ContactsViewModel { *; }
-keep class com.klemfner.whoscalling.ui.user.UserViewModel { *; }
-keep class com.klemfner.whoscalling.ui.settings.SettingsViewModel { *; }

# App DI modules, repositories, data sources, and models (resolved by Koin via reflection)
-keep class com.klemfner.whoscalling.di.** { *; }
-keep class com.klemfner.whoscalling.domain.repository.** { *; }
-keep class com.klemfner.whoscalling.domain.model.** { *; }
-keep class com.klemfner.whoscalling.data.repository.** { *; }
-keep class com.klemfner.whoscalling.data.local.** { *; }
-keep class com.klemfner.whoscalling.data.remote.** { *; }
