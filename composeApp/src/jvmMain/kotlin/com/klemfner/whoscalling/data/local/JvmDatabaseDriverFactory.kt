package com.klemfner.whoscalling.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.klemfner.whoscalling.data.local.db.WhosCallingDatabase
import java.io.File

class JvmDatabaseDriverFactory(
    private val dbPath: String = "whoscalling.db"
) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        File(dbPath).parentFile.mkdirs()
        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")
        WhosCallingDatabase.Schema.create(driver)
        return driver
    }
}
