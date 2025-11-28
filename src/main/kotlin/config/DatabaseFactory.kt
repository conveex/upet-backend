package com.cnvx.config

import com.cnvx.db.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(dbUrl: String) {
        val dataSource = hikari(dbUrl)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                OwnerTable,
                WalkerTable,
                PetTable,
                AvailabilityTable,
                RouteTable,
                RoutePointTable,
                WalkTable,
                WalkPetTable,
                CheckPhotoTable,
                RatingTable,
                PaymentTable
            )
        }
    }

    private fun hikari(dbUrl: String): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = dbUrl
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }
}