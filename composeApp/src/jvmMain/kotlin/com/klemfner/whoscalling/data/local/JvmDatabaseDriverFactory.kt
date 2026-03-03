package com.klemfner.whoscalling.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.klemfner.whoscalling.data.local.db.WhosCallingDatabase

class JvmDatabaseDriverFactory(
    private val dbPath: String = "whoscalling.db"
) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")
        WhosCallingDatabase.Schema.create(driver)
        return driver
    }
}
