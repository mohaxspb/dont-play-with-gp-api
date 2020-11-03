package ru.kuchanov.gp.configuration

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.stereotype.Component

@Component
class FlywayMigrationStrategyImpl : FlywayMigrationStrategy {
    override fun migrate(flyway: Flyway) {
        flyway.migrate()
    }
}