package com.devpads.unimed

import com.devpads.unimed.application.atendimento.port.out.AtendimentoRepositoryPort
import com.devpads.unimed.application.atendimento.port.out.AtendimentoVinculoPort
import com.devpads.unimed.application.historico.port.out.HistoricoRepositoryPort
import com.devpads.unimed.application.paciente.port.out.PacienteRepositoryPort
import com.devpads.unimed.application.procedimento.port.out.ProcedimentoRepositoryPort
import com.devpads.unimed.domain.atendimento.model.Atendimento
import com.devpads.unimed.domain.paciente.model.Paciente
import com.devpads.unimed.domain.procedimento.model.Procedimento
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.boot.test.web.server.LocalServerPort
import java.math.BigDecimal
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
class HistoricoControllerIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @MockitoBean
    private lateinit var pacienteRepository: PacienteRepositoryPort

    @MockitoBean
    private lateinit var historicoRepository: HistoricoRepositoryPort

    @MockitoBean
    private lateinit var atendimentoRepository: AtendimentoRepositoryPort

    @MockitoBean
    private lateinit var atendimentoVinculoPort: AtendimentoVinculoPort

    @MockitoBean
    private lateinit var procedimentoRepository: ProcedimentoRepositoryPort

    private val pacienteId = 1L
    private val paciente = Paciente(
        id = pacienteId,
        nome = "Jo\u00e3o Silva",
        cpf = "00000000001",
        dataNascimento = LocalDate.of(1980, 5, 15),
        telefone = "11999999999",
        email = "joao.silva@email.com",
    )
    private val atendimento = Atendimento(
        id = 10L,
        pacienteId = pacienteId,
        dataAtendimento = Instant.parse("2026-01-15T09:00:00Z"),
        medico = "Dra. Mariana Costa",
        observacoes = "Paciente relata dores de cabe\u00e7a frequentes",
    )
    private val procedimento = Procedimento(
        id = 100L,
        atendimentoId = 10L,
        nome = "Consulta cl\u00ednica geral",
        valor = BigDecimal("150.00"),
    )

    @Test
    fun `getHistorico should return 200 with paciente and atendimentos`() {
        whenever(pacienteRepository.findById(pacienteId)).thenReturn(paciente)
        whenever(historicoRepository.findAtendimentosByPacienteId(any(), any())).thenReturn(listOf(atendimento))
        whenever(historicoRepository.findProcedimentosByAtendimentoIds(any())).thenReturn(listOf(procedimento))

        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/pacientes/$pacienteId/historico",
            Map::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        assertNotNull(body)
        @Suppress("UNCHECKED_CAST")
        val bodyMap = body as Map<String, Any>
        val pacienteMap = bodyMap["paciente"] as Map<String, Any>
        assertEquals(pacienteId, (pacienteMap["id"] as Int).toLong())
        val atendimentos = bodyMap["atendimentos"] as List<*>
        assertEquals(1, atendimentos.size)
    }

    @Test
    fun `getHistorico should return 200 with empty list when paciente has no atendimentos`() {
        whenever(pacienteRepository.findById(pacienteId)).thenReturn(paciente)
        whenever(historicoRepository.findAtendimentosByPacienteId(any(), any())).thenReturn(emptyList())

        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/pacientes/$pacienteId/historico",
            Map::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        assertNotNull(body)
        @Suppress("UNCHECKED_CAST")
        val bodyMap = body as Map<String, Any>
        val atendimentos = bodyMap["atendimentos"] as List<*>
        assertEquals(0, atendimentos.size)
    }

    @Test
    fun `getHistorico should return 404 when paciente not found`() {
        whenever(pacienteRepository.findById(pacienteId)).thenReturn(null)

        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/pacientes/$pacienteId/historico",
            Map::class.java,
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}
