package com.upet.data.db

import com.upet.data.db.tables.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(dbUrl: String, dbUser: String, dbPassword: String) {
        val hikari = HikariConfig().apply {
            jdbcUrl = dbUrl
            username = dbUser
            password = dbPassword
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(hikari)

        /*
        No queria, pero tuvo que ser sacrificado, estaba dando muchos problemas con el fatjar para el docker
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()

        flyway.migrate()
        */

        Database.connect(dataSource)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(UsersTable)
        }
    }
}