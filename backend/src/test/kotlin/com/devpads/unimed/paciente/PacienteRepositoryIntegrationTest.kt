package com.devpads.unimed.paciente

import com.devpads.unimed.application.atendimento.port.out.AtendimentoRepositoryPort
import com.devpads.unimed.application.paciente.port.out.PacienteRepositoryPort
import com.devpads.unimed.application.procedimento.port.out.ProcedimentoRepositoryPort
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate

@SpringBootTest(properties = ["infra.mysql.enabled=false"])
@Testcontainers(disabledWithoutDocker = true)
class PacienteRepositoryIntegrationTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16.3")

        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }
    }

    @Autowired
    private lateinit var repository: PacienteRepositoryPort

    @MockitoBean
    private lateinit var atendimentoRepositoryPort: AtendimentoRepositoryPort

    @MockitoBean
    private lateinit var procedimentoRepositoryPort: ProcedimentoRepositoryPort

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun cleanUp() {
        jdbcTemplate.execute("DELETE FROM pacientes")
    }

    @Test
    fun existsByCpf_shouldReturnTrue_whenCpfExists() {
        jdbcTemplate.update(
            "INSERT INTO pacientes (nome, cpf, data_nascimento, telefone, email) VALUES (?, ?, ?, ?, ?)",
            "João", "12345678901", LocalDate.of(1990, 1, 1), "11999999999", "joao@email.com",
        )

        assertTrue(repository.existsByCpf("12345678901"))
    }

    @Test
    fun existsByCpf_shouldReturnFalse_whenCpfDoesNotExist() {
        assertFalse(repository.existsByCpf("99999999999"))
    }

    @Test
    fun existsByEmailIgnoreCase_shouldMatchCaseInsensitively() {
        jdbcTemplate.update(
            "INSERT INTO pacientes (nome, cpf, data_nascimento, telefone, email) VALUES (?, ?, ?, ?, ?)",
            "Maria", "98765432100", LocalDate.of(1992, 7, 22), "11977777777", "Maria@Email.com",
        )

        assertTrue(repository.existsByEmailIgnoreCase("maria@email.com"))
        assertTrue(repository.existsByEmailIgnoreCase("MARIA@EMAIL.COM"))
    }

    @Test
    fun existsByEmailIgnoreCase_shouldReturnFalse_whenEmailDoesNotExist() {
        assertFalse(repository.existsByEmailIgnoreCase("nobody@email.com"))
    }

    @Test
    fun existsByCpfAndIdNot_shouldReturnTrue_whenOtherPatientHasCpf() {
        val id1 = jdbcTemplate.queryForObject(
            "INSERT INTO pacientes (nome, cpf, data_nascimento, telefone, email) VALUES (?, ?, ?, ?, ?) RETURNING id",
            Long::class.java, "João", "12345678901", LocalDate.of(1990, 1, 1), "11999999999", "joao@email.com",
        )!!
        jdbcTemplate.update(
            "INSERT INTO pacientes (nome, cpf, data_nascimento, telefone, email) VALUES (?, ?, ?, ?, ?)",
            "Pedro", "12345678901", LocalDate.of(1985, 5, 10), "11988888888", "pedro@email.com",
        )

        assertTrue(repository.existsByCpfAndIdNot("12345678901", id1))
    }

    @Test
    fun existsByCpfAndIdNot_shouldReturnFalse_whenOnlyOwnPatientHasCpf() {
        val id = jdbcTemplate.queryForObject(
            "INSERT INTO pacientes (nome, cpf, data_nascimento, telefone, email) VALUES (?, ?, ?, ?, ?) RETURNING id",
            Long::class.java, "João", "12345678901", LocalDate.of(1990, 1, 1), "11999999999", "joao@email.com",
        )!!

        assertFalse(repository.existsByCpfAndIdNot("12345678901", id))
    }

    @Test
    fun existsByEmailIgnoreCaseAndIdNot_shouldReturnTrue_whenOtherPatientHasEmail() {
        val id1 = jdbcTemplate.queryForObject(
            "INSERT INTO pacientes (nome, cpf, data_nascimento, telefone, email) VALUES (?, ?, ?, ?, ?) RETURNING id",
            Long::class.java, "João", "11111111111", LocalDate.of(1990, 1, 1), "11999999999", "joao@email.com",
        )!!
        jdbcTemplate.update(
            "INSERT INTO pacientes (nome, cpf, data_nascimento, telefone, email) VALUES (?, ?, ?, ?, ?)",
            "Pedro", "22222222222", LocalDate.of(1985, 5, 10), "11988888888", "Joao@Email.com",
        )

        assertTrue(repository.existsByEmailIgnoreCaseAndIdNot("joao@email.com", id1))
    }

    @Test
    fun existsByEmailIgnoreCaseAndIdNot_shouldReturnFalse_whenOnlyOwnPatientHasEmail() {
        val id = jdbcTemplate.queryForObject(
            "INSERT INTO pacientes (nome, cpf, data_nascimento, telefone, email) VALUES (?, ?, ?, ?, ?) RETURNING id",
            Long::class.java, "João", "11111111111", LocalDate.of(1990, 1, 1), "11999999999", "joao@email.com",
        )!!

        assertFalse(repository.existsByEmailIgnoreCaseAndIdNot("joao@email.com", id))
    }
}
