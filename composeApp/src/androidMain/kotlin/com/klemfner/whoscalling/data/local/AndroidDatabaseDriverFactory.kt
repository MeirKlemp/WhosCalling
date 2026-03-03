package com.klemfner.whoscalling.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.klemfner.whoscalling.data.local.db.WhoCallingDatabase

class AndroidDatabaseDriverFactory(private val context: Context) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(WhoCallingDatabase.Schema, context, "whoscalling.db")
    }
}
