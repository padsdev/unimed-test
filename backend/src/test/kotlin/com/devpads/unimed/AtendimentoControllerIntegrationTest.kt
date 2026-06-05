package com.devpads.unimed

import com.devpads.unimed.application.atendimento.port.out.AtendimentoRepositoryPort
import com.devpads.unimed.application.atendimento.port.out.AtendimentoVinculoPort
import com.devpads.unimed.application.paciente.port.out.PacienteRepositoryPort
import com.devpads.unimed.application.procedimento.port.out.ProcedimentoRepositoryPort
import com.devpads.unimed.application.shared.PagedResult
import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.domain.atendimento.model.Atendimento
import com.devpads.unimed.domain.paciente.model.Paciente
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.mockito.kotlin.any
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "infra.mysql.enabled=false",
        "spring.flyway.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
    ],
)
class AtendimentoControllerIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @MockitoBean
    private lateinit var atendimentoRepository: AtendimentoRepositoryPort

    @MockitoBean
    private lateinit var pacienteRepository: PacienteRepositoryPort

    @MockitoBean
    private lateinit var atendimentoVinculoPort: AtendimentoVinculoPort

    @MockitoBean
    private lateinit var procedimentoRepositoryPort: ProcedimentoRepositoryPort

    private fun url(path: String) = "http://localhost:$port/api$path"

    @Test
    fun list_shouldReturnEmptyPage_whenNoAtendimentos() {
        whenever(atendimentoRepository.findAll(any(), any(), any(), any(), isNull()))
            .thenReturn(PagedResult(emptyList(), 0, 10, 0L, 0))

        val response = restTemplate.getForEntity(url("/atendimentos"), Map::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(0, response.body!!["totalItems"])
    }

    @Test
    fun list_shouldReturn400_whenNegativePage() {
        val response = restTemplate.getForEntity(url("/atendimentos?page=-1"), ProblemDetail::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun create_shouldReturnCreated_whenValid() {
        val paciente = Paciente(
            id = 1L, nome = "João", cpf = "12345678901",
            dataNascimento = LocalDate.of(1990, 1, 1), telefone = "11999999999", email = "joao@email.com",
        )
        whenever(pacienteRepository.findById(1L)).thenReturn(paciente)

        val saved = Atendimento(
            id = 1L, pacienteId = 1L,
            dataAtendimento = Instant.parse("2026-05-15T14:30:00Z"),
            medico = "Dr. Carlos", observacoes = "Consulta",
        )
        whenever(atendimentoRepository.save(org.mockito.kotlin.any())).thenReturn(saved)

        val payload = mapOf(
            "pacienteId" to 1,
            "dataAtendimento" to "2026-05-15T14:30:00Z",
            "medico" to "Dr. Carlos",
            "observacoes" to "Consulta",
        )

        val response = restTemplate.postForEntity(url("/atendimentos"), payload, Map::class.java)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body!!["id"])
        assertEquals("Dr. Carlos", response.body!!["medico"])
    }

    @Test
    fun create_shouldReturn409_whenPacienteNotFound() {
        whenever(pacienteRepository.findById(999L)).thenReturn(null)

        val payload = mapOf(
            "pacienteId" to 999,
            "dataAtendimento" to "2026-05-15T14:30:00Z",
            "medico" to "Dr. Carlos",
            "observacoes" to "",
        )

        val response = restTemplate.postForEntity(url("/atendimentos"), payload, ProblemDetail::class.java)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
    }

    @Test
    fun create_shouldReturn400_whenDataAtendimentoInvalid() {
        val payload = mapOf(
            "pacienteId" to 1,
            "dataAtendimento" to "invalida",
            "medico" to "Dr. Carlos",
            "observacoes" to "",
        )

        val response = restTemplate.postForEntity(url("/atendimentos"), payload, ProblemDetail::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun getById_shouldReturn200_whenExists() {
        val atendimento = Atendimento(
            id = 1L, pacienteId = 1L,
            dataAtendimento = Instant.parse("2026-05-15T14:30:00Z"),
            medico = "Dr. Carlos", observacoes = "Consulta",
        )
        whenever(atendimentoRepository.findById(1L)).thenReturn(atendimento)

        val response = restTemplate.getForEntity(url("/atendimentos/1"), Map::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Dr. Carlos", response.body!!["medico"])
    }

    @Test
    fun getById_shouldReturn404_whenNotExists() {
        whenever(atendimentoRepository.findById(999L)).thenReturn(null)

        val response = restTemplate.getForEntity(url("/atendimentos/999"), ProblemDetail::class.java)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun update_shouldReturn200_whenValid() {
        val existing = Atendimento(
            id = 1L, pacienteId = 1L,
            dataAtendimento = Instant.parse("2026-05-15T14:30:00Z"),
            medico = "Dr. Carlos", observacoes = "Consulta",
        )
        whenever(atendimentoRepository.findById(1L)).thenReturn(existing)

        val paciente = Paciente(
            id = 2L, nome = "Maria", cpf = "00000000002",
            dataNascimento = LocalDate.of(1990, 1, 1), telefone = "11999999999", email = "maria@email.com",
        )
        whenever(pacienteRepository.findById(2L)).thenReturn(paciente)

        val updated = existing.copy(pacienteId = 2L, medico = "Dra. Ana")
        whenever(atendimentoRepository.save(org.mockito.kotlin.any())).thenReturn(updated)

        val updatePayload = mapOf(
            "pacienteId" to 2,
            "dataAtendimento" to "2026-05-15T14:30:00Z",
            "medico" to "Dra. Ana",
            "observacoes" to "Retorno",
        )

        val response = restTemplate.exchange(
            url("/atendimentos/1"),
            HttpMethod.PUT,
            HttpEntity(updatePayload),
            Map::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Dra. Ana", response.body!!["medico"])
    }

    @Test
    fun update_shouldReturn404_whenNotExists() {
        whenever(atendimentoRepository.findById(999L)).thenReturn(null)

        val updatePayload = mapOf(
            "pacienteId" to 1,
            "dataAtendimento" to "2026-05-15T14:30:00Z",
            "medico" to "Dr. Carlos",
            "observacoes" to "",
        )

        val response = restTemplate.exchange(
            url("/atendimentos/999"),
            HttpMethod.PUT,
            HttpEntity(updatePayload),
            ProblemDetail::class.java,
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun delete_shouldReturn204_whenExists() {
        val existing = Atendimento(
            id = 1L, pacienteId = 1L,
            dataAtendimento = Instant.parse("2026-05-15T14:30:00Z"),
            medico = "Dr. Carlos", observacoes = "",
        )
        whenever(atendimentoRepository.findById(1L)).thenReturn(existing)

        val response = restTemplate.exchange(
            url("/atendimentos/1"),
            HttpMethod.DELETE,
            null,
            Void::class.java,
        )

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun delete_shouldReturn404_whenNotExists() {
        whenever(atendimentoRepository.findById(999L)).thenReturn(null)

        val response = restTemplate.exchange(
            url("/atendimentos/999"),
            HttpMethod.DELETE,
            null,
            ProblemDetail::class.java,
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}