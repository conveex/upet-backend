package com.upet.data.db

import com.upet.data.db.tables.ClientPaymentMethodsTable
import com.upet.data.db.tables.MediaFilesTable
import com.upet.data.db.tables.PaymentMethodsTable
import com.upet.data.db.tables.PetsTable
import com.upet.data.db.tables.UsersTable
import com.upet.data.db.tables.WalkPaymentMethodsTable
import com.upet.data.db.tables.WalkPetsTable
import com.upet.data.db.tables.WalkTrackSummariesTable
import com.upet.data.db.tables.WalkerPaymentMethodsTable
import com.upet.data.db.tables.WalkerProfilesTable
import com.upet.data.db.tables.WalksTable
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
            SchemaUtils.createMissingTablesAndColumns(
                UsersTable,
                WalkerProfilesTable,
                PetsTable,
                PaymentMethodsTable,
                ClientPaymentMethodsTable,
                WalkerPaymentMethodsTable,
                WalksTable,
                WalkPaymentMethodsTable,
                WalkPetsTable,
                MediaFilesTable,
                WalkTrackSummariesTable
            )
        }
    }
}