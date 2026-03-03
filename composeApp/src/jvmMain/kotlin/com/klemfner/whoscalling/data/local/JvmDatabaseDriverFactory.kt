package com.klemfner.whoscalling.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.klemfner.whoscalling.data.local.db.WhoCallingDatabase

class JvmDatabaseDriverFactory(
    private val dbPath: String = "whoscalling.db"
) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")
        WhoCallingDatabase.Schema.create(driver)
        return driver
    }
}
