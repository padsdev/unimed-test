package com.devpads.unimed

import com.devpads.unimed.application.atendimento.port.out.AtendimentoRepositoryPort
import com.devpads.unimed.application.atendimento.port.out.AtendimentoVinculoPort
import com.devpads.unimed.application.paciente.port.out.PacienteRepositoryPort
import com.devpads.unimed.application.procedimento.port.out.ProcedimentoRepositoryPort
import com.devpads.unimed.application.shared.PagedResult
import com.devpads.unimed.domain.procedimento.model.Procedimento
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
import java.math.BigDecimal

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "infra.mysql.enabled=false",
        "spring.flyway.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
    ],
)
class ProcedimentoControllerIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @MockitoBean
    private lateinit var pacienteRepository: PacienteRepositoryPort

    @MockitoBean
    private lateinit var atendimentoRepository: AtendimentoRepositoryPort

    @MockitoBean
    private lateinit var atendimentoVinculoPort: AtendimentoVinculoPort

    @MockitoBean
    private lateinit var procedimentoRepository: ProcedimentoRepositoryPort

    private fun url(path: String) = "http://localhost:$port/api$path"

    @Test
    fun list_shouldReturnEmptyPage_whenNoProcedimentos() {
        whenever(procedimentoRepository.findAll(any(), any(), any(), any(), isNull()))
            .thenReturn(PagedResult(emptyList(), 0, 10, 0L, 0))

        val response = restTemplate.getForEntity(url("/procedimentos"), Map::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(0, response.body!!["totalItems"])
    }

    @Test
    fun list_shouldReturn400_whenNegativePage() {
        val response = restTemplate.getForEntity(url("/procedimentos?page=-1"), ProblemDetail::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun create_shouldReturnCreated_whenValid() {
        val saved = Procedimento(id = 1L, atendimentoId = 1L, nome = "Eletrocardiograma", valor = BigDecimal("150.00"))
        whenever(procedimentoRepository.save(any())).thenReturn(saved)

        val payload = mapOf(
            "atendimentoId" to 1,
            "nome" to "Eletrocardiograma",
            "valor" to 150.00,
        )

        val response = restTemplate.postForEntity(url("/procedimentos"), payload, Map::class.java)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body!!["id"])
        assertEquals("Eletrocardiograma", response.body!!["nome"])
        assertEquals(150.0, response.body!!["valor"])
    }

    @Test
    fun create_shouldReturn400_whenValorNegative() {
        val payload = mapOf(
            "atendimentoId" to 1,
            "nome" to "Exame",
            "valor" to -10.0,
        )

        val response = restTemplate.postForEntity(url("/procedimentos"), payload, ProblemDetail::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun create_shouldReturn400_whenNomeBlank() {
        val payload = mapOf(
            "atendimentoId" to 1,
            "nome" to "",
            "valor" to 100.0,
        )

        val response = restTemplate.postForEntity(url("/procedimentos"), payload, ProblemDetail::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun getById_shouldReturn200_whenExists() {
        val procedimento = Procedimento(id = 1L, atendimentoId = 1L, nome = "Raio-X", valor = BigDecimal("250.00"))
        whenever(procedimentoRepository.findById(1L)).thenReturn(procedimento)

        val response = restTemplate.getForEntity(url("/procedimentos/1"), Map::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Raio-X", response.body!!["nome"])
    }

    @Test
    fun getById_shouldReturn404_whenNotExists() {
        whenever(procedimentoRepository.findById(999L)).thenReturn(null)

        val response = restTemplate.getForEntity(url("/procedimentos/999"), ProblemDetail::class.java)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun update_shouldReturn200_whenValid() {
        val existing = Procedimento(id = 1L, atendimentoId = 1L, nome = "Raio-X", valor = BigDecimal("250.00"))
        whenever(procedimentoRepository.findById(1L)).thenReturn(existing)

        val updated = existing.copy(atendimentoId = 2L, nome = "Tomografia", valor = BigDecimal("500.00"))
        whenever(procedimentoRepository.save(any())).thenReturn(updated)

        val updatePayload = mapOf(
            "atendimentoId" to 2,
            "nome" to "Tomografia",
            "valor" to 500.00,
        )

        val response = restTemplate.exchange(
            url("/procedimentos/1"),
            HttpMethod.PUT,
            HttpEntity(updatePayload),
            Map::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Tomografia", response.body!!["nome"])
    }

    @Test
    fun update_shouldReturn404_whenNotExists() {
        whenever(procedimentoRepository.findById(999L)).thenReturn(null)

        val updatePayload = mapOf(
            "atendimentoId" to 1,
            "nome" to "Exame",
            "valor" to 100.0,
        )

        val response = restTemplate.exchange(
            url("/procedimentos/999"),
            HttpMethod.PUT,
            HttpEntity(updatePayload),
            ProblemDetail::class.java,
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun delete_shouldReturn204_whenExists() {
        val existing = Procedimento(id = 1L, atendimentoId = 1L, nome = "Exame", valor = BigDecimal("100.00"))
        whenever(procedimentoRepository.findById(1L)).thenReturn(existing)

        val response = restTemplate.exchange(
            url("/procedimentos/1"),
            HttpMethod.DELETE,
            null,
            Void::class.java,
        )

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun delete_shouldReturn404_whenNotExists() {
        whenever(procedimentoRepository.findById(999L)).thenReturn(null)

        val response = restTemplate.exchange(
            url("/procedimentos/999"),
            HttpMethod.DELETE,
            null,
            ProblemDetail::class.java,
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun findByAtendimento_shouldReturnList() {
        val procedimentos = listOf(
            Procedimento(id = 1L, atendimentoId = 1L, nome = "Proc A", valor = BigDecimal("100.00")),
            Procedimento(id = 2L, atendimentoId = 1L, nome = "Proc B", valor = BigDecimal("200.00")),
        )
        whenever(procedimentoRepository.findByAtendimentoId(1L)).thenReturn(procedimentos)

        val response = restTemplate.getForEntity(url("/procedimentos/por-atendimento/1"), List::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, (response.body as List<*>).size)
    }
}