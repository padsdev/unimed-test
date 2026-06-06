package com.devpads.unimed.infrastructure.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.sql.Connection
import java.sql.DriverManager

class MysqlJdbcConnectionFactory(
    private val url: String,
    private val user: String,
    private val password: String,
) {
    fun getConnection(): Connection = DriverManager.getConnection(url, user, password)
}

@Configuration
@ConditionalOnProperty(name = ["infra.mysql.enabled"], havingValue = "true", matchIfMissing = false)
class MysqlJdbcConnectionFactoryConfig {

    @Bean
    fun mysqlJdbcConnectionFactory(): MysqlJdbcConnectionFactory {
        val url = System.getenv("MYSQL_URL") ?: "jdbc:mysql://localhost:3306/unimed_operacional"
        val user = System.getenv("MYSQL_USER") ?: "unimed"
        val password = System.getenv("MYSQL_PASSWORD") ?: "unimed"
        Class.forName("com.mysql.cj.jdbc.Driver")
        return MysqlJdbcConnectionFactory(url, user, password)
    }
}
