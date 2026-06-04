package com.devpads.unimed.atendimento

import com.devpads.unimed.infrastructure.persistence.mysql.atendimento.AtendimentoVinculoMysqlAdapter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import javax.sql.DataSource

@Testcontainers(disabledWithoutDocker = true)
class AtendimentoVinculoMysqlAdapterTest {

    companion object {
        @Container
        val mysql = MySQLContainer("mysql:8.4")
    }

    private lateinit var dataSource: DataSource
    private lateinit var adapter: AtendimentoVinculoMysqlAdapter

    @BeforeEach
    fun setup() {
        dataSource = DriverManagerDataSource(mysql.jdbcUrl, mysql.username, mysql.password).apply {
            setDriverClassName("com.mysql.cj.jdbc.Driver")
        }

        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS atendimentos (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        paciente_id BIGINT NOT NULL,
                        data_atendimento DATETIME NOT NULL,
                        medico VARCHAR(150) NOT NULL,
                        observacoes TEXT
                    )
                    """.trimIndent(),
                )
            }
        }

        adapter = AtendimentoVinculoMysqlAdapter(dataSource)
    }

    @AfterEach
    fun cleanUp() {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("TRUNCATE TABLE atendimentos")
            }
        }
    }

    @Test
    fun existsByPacienteId_shouldReturnTrue_whenVinculoExists() {
        dataSource.connection.use { conn ->
            conn.prepareStatement("INSERT INTO atendimentos (paciente_id, data_atendimento, medico) VALUES (?, NOW(), ?)")
                .use { stmt ->
                    stmt.setLong(1, 1L)
                    stmt.setString(2, "Dr. Test")
                    stmt.executeUpdate()
                }
        }

        assertTrue(adapter.existsByPacienteId(1L))
    }

    @Test
    fun existsByPacienteId_shouldReturnFalse_whenNoVinculo() {
        assertFalse(adapter.existsByPacienteId(999L))
    }

    @Test
    fun existsByPacienteId_shouldReturnTrue_whenMultipleVinculosExist() {
        dataSource.connection.use { conn ->
            val stmt1 = conn.prepareStatement("INSERT INTO atendimentos (paciente_id, data_atendimento, medico) VALUES (?, NOW(), ?)")
            stmt1.setLong(1, 5L)
            stmt1.setString(2, "Dr. A")
            stmt1.executeUpdate()

            val stmt2 = conn.prepareStatement("INSERT INTO atendimentos (paciente_id, data_atendimento, medico) VALUES (?, NOW(), ?)")
            stmt2.setLong(1, 5L)
            stmt2.setString(2, "Dr. B")
            stmt2.executeUpdate()
        }

        assertTrue(adapter.existsByPacienteId(5L))
    }

    @Test
    fun existsByPacienteId_shouldNotConfuseDifferentPacientes() {
        dataSource.connection.use { conn ->
            conn.prepareStatement("INSERT INTO atendimentos (paciente_id, data_atendimento, medico) VALUES (?, NOW(), ?)")
                .use { stmt ->
                    stmt.setLong(1, 10L)
                    stmt.setString(2, "Dr. Test")
                    stmt.executeUpdate()
                }
        }

        assertFalse(adapter.existsByPacienteId(99L))
    }
}
