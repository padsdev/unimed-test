package com.devpads.unimed.infrastructure.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

@Configuration
@ConditionalOnProperty(name = ["infra.mysql.enabled"], havingValue = "true", matchIfMissing = false)
class MysqlDatasourceConfig {

    @Bean
    fun mysqlDataSource(): DataSource {
        val url = System.getenv("MYSQL_URL") ?: "jdbc:mysql://localhost:3306/unimed_operacional"
        val user = System.getenv("MYSQL_USER") ?: "unimed"
        val password = System.getenv("MYSQL_PASSWORD") ?: "unimed"
        return DriverManagerDataSource(url, user, password).apply {
            setDriverClassName("com.mysql.cj.jdbc.Driver")
        }
    }
}
