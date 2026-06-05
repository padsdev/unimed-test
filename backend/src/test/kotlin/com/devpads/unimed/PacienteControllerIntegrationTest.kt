package com.devpads.unimed

import com.devpads.unimed.application.atendimento.port.out.AtendimentoRepositoryPort
import com.devpads.unimed.application.atendimento.port.out.AtendimentoVinculoPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.springframework.boot.test.web.server.LocalServerPort
import kotlin.test.assertContains

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["infra.mysql.enabled=false"],
)
@Testcontainers(disabledWithoutDocker = true)
class PacienteControllerIntegrationTest {

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

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @MockitoBean
    private lateinit var atendimentoVinculoPort: AtendimentoVinculoPort

    @MockitoBean
    private lateinit var atendimentoRepositoryPort: AtendimentoRepositoryPort

    private fun url(path: String) = "http://localhost:$port/api$path"

    private val createPayload: Map<String, String>
        get() = mapOf(
            "nome" to "Novo Paciente",
            "cpf" to "99988877766",
            "dataNascimento" to "1995-06-20",
            "telefone" to "11911112222",
            "email" to "novo.paciente@email.com",
        )

    @BeforeEach
    fun cleanUp() {
        jdbcTemplate.execute("DELETE FROM pacientes")
    }

    @Test
    fun list_shouldReturnEmptyPage_whenNoPacientes() {
        val response = restTemplate.getForEntity(url("/pacientes"), Map::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        assertNotNull(body)
        assertEquals(0, body!!["totalItems"])
        assertTrue((body["items"] as List<*>).isEmpty())
    }

    @Test
    fun create_shouldReturn201_whenValid() {
        val response = restTemplate.postForEntity(url("/pacientes"), createPayload, Map::class.java)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        val body = response.body
        assertNotNull(body)
        assertEquals("Novo Paciente", body!!["nome"])
        assertEquals("99988877766", body["cpf"])
        assertNotNull(body["id"])
    }

    @Test
    fun create_shouldReturn422_whenNomeBlank() {
        val payload = createPayload.toMutableMap().apply { put("nome", "") }
        val response = restTemplate.postForEntity(url("/pacientes"), payload, ProblemDetail::class.java)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
    }

    @Test
    fun create_shouldReturn400_whenCpfInvalid() {
        val payload = createPayload.toMutableMap().apply { put("cpf", "123") }
        val response = restTemplate.postForEntity(url("/pacientes"), payload, ProblemDetail::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun create_shouldReturn409_whenCpfDuplicated() {
        restTemplate.postForEntity(url("/pacientes"), createPayload, Map::class.java)

        val response = restTemplate.postForEntity(url("/pacientes"), createPayload, ProblemDetail::class.java)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
    }

    @Test
    fun getById_shouldReturn200_whenExists() {
        val created = restTemplate.postForEntity(url("/pacientes"), createPayload, Map::class.java).body!!
        val id = created["id"]

        val response = restTemplate.getForEntity(url("/pacientes/$id"), Map::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Novo Paciente", response.body!!["nome"])
    }

    @Test
    fun getById_shouldReturn404_whenNotExists() {
        val response = restTemplate.getForEntity(url("/pacientes/9999"), ProblemDetail::class.java)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun list_shouldReturnCreated_whenDataExists() {
        restTemplate.postForEntity(url("/pacientes"), createPayload, Map::class.java)

        val response = restTemplate.getForEntity(url("/pacientes"), Map::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body!!["totalItems"])
    }

    @Test
    fun update_shouldReturn200_whenValid() {
        val created = restTemplate.postForEntity(url("/pacientes"), createPayload, Map::class.java).body!!
        val id = created["id"]
        val updatePayload = mapOf(
            "nome" to "Nome Atualizado",
            "dataNascimento" to "1995-06-20",
            "telefone" to "11933334444",
            "email" to "atualizado@email.com",
        )

        val response = restTemplate.exchange(
            url("/pacientes/$id"),
            HttpMethod.PUT,
            HttpEntity(updatePayload),
            Map::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Nome Atualizado", response.body!!["nome"])
    }

    @Test
    fun update_shouldReturn404_whenNotExists() {
        val updatePayload = mapOf(
            "nome" to "Nome",
            "dataNascimento" to "1995-06-20",
            "telefone" to "11933334444",
            "email" to "nao.existe@email.com",
        )

        val response = restTemplate.exchange(
            url("/pacientes/9999"),
            HttpMethod.PUT,
            HttpEntity(updatePayload),
            ProblemDetail::class.java,
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun update_shouldReturn409_whenEmailConflict() {
        restTemplate.postForEntity(url("/pacientes"), createPayload, Map::class.java)

        val secondPayload = createPayload.toMutableMap().apply { put("cpf", "11122233344") }
        val created2 = restTemplate.postForEntity(url("/pacientes"), secondPayload, Map::class.java).body!!
        val id2 = created2["id"]

        val updatePayload = mapOf(
            "nome" to "Outro",
            "dataNascimento" to "1995-06-20",
            "telefone" to "11933334444",
            "email" to "novo.paciente@email.com",
        )

        val response = restTemplate.exchange(
            url("/pacientes/$id2"),
            HttpMethod.PUT,
            HttpEntity(updatePayload),
            ProblemDetail::class.java,
        )

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
    }

    @Test
    fun delete_shouldReturn204_whenNoVinculos() {
        val created = restTemplate.postForEntity(url("/pacientes"), createPayload, Map::class.java).body!!
        val id = created["id"]

        val response = restTemplate.exchange(
            url("/pacientes/$id"),
            HttpMethod.DELETE,
            null,
            Void::class.java,
        )

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun delete_shouldReturn404_whenNotExists() {
        val response = restTemplate.exchange(
            url("/pacientes/9999"),
            HttpMethod.DELETE,
            null,
            ProblemDetail::class.java,
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun delete_shouldReturn409_whenVinculosExist() {
        val created = restTemplate.postForEntity(url("/pacientes"), createPayload, Map::class.java).body!!
        val id = created["id"] as Int

        org.mockito.kotlin.whenever(atendimentoVinculoPort.existsByPacienteId(id.toLong())).thenReturn(true)

        val response = restTemplate.exchange(
            url("/pacientes/$id"),
            HttpMethod.DELETE,
            null,
            ProblemDetail::class.java,
        )

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
    }

    @Test
    fun create_shouldReturn400_whenDataNascimentoInvalid() {
        val payload = createPayload.toMutableMap().apply { put("dataNascimento", "invalida") }
        val response = restTemplate.postForEntity(url("/pacientes"), payload, ProblemDetail::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}
