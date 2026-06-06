package com.devpads.unimed.application.historico.service

import com.devpads.unimed.application.historico.port.out.HistoricoRepositoryPort
import com.devpads.unimed.application.paciente.port.out.PacienteRepositoryPort
import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.domain.atendimento.model.Atendimento
import com.devpads.unimed.domain.paciente.model.Paciente
import com.devpads.unimed.domain.procedimento.model.Procedimento
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class HistoricoServiceTest {

    @Mock
    private lateinit var pacienteRepository: PacienteRepositoryPort

    @Mock
    private lateinit var historicoRepository: HistoricoRepositoryPort

    private lateinit var service: HistoricoService

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

    @BeforeEach
    fun setup() {
        service = HistoricoService(pacienteRepository, historicoRepository)
    }

    @Test
    fun `should return historico when paciente exists with atendimentos and procedimentos`() {
        whenever(pacienteRepository.findById(pacienteId)).thenReturn(paciente)
        whenever(historicoRepository.findAtendimentosByPacienteId(pacienteId, "DESC")).thenReturn(listOf(atendimento))
        whenever(historicoRepository.findProcedimentosByAtendimentoIds(listOf(10L))).thenReturn(listOf(procedimento))

        val result = service.getHistorico(pacienteId, "desc")

        assertNotNull(result)
        assertEquals(pacienteId, result.paciente.id)
        assertEquals(1, result.atendimentos.size)
        assertEquals(1, result.procedimentos.size)
        assertEquals(atendimento.id, result.atendimentos[0].id)
        assertEquals(procedimento.id, result.procedimentos[0].id)
    }

    @Test
    fun `should return historico when paciente exists with atendimentos and procedimentos ASC`() {
        val atendimento2 = Atendimento(
            id = 20L,
            pacienteId = pacienteId,
            dataAtendimento = Instant.parse("2026-06-01T14:30:00Z"),
            medico = "Dr. Carlos",
            observacoes = "Retorno",
        )
        whenever(pacienteRepository.findById(pacienteId)).thenReturn(paciente)
        whenever(historicoRepository.findAtendimentosByPacienteId(pacienteId, "ASC"))
            .thenReturn(listOf(atendimento, atendimento2))
        whenever(historicoRepository.findProcedimentosByAtendimentoIds(listOf(10L, 20L))).thenReturn(listOf(procedimento))

        val result = service.getHistorico(pacienteId, "asc")

        assertNotNull(result)
        assertEquals(2, result.atendimentos.size)
        assertTrue(result.atendimentos[0].dataAtendimento <= result.atendimentos[1].dataAtendimento)
    }

    @Test
    fun `should return historico when atendimento exists without procedimentos`() {
        whenever(pacienteRepository.findById(pacienteId)).thenReturn(paciente)
        whenever(historicoRepository.findAtendimentosByPacienteId(pacienteId, "DESC")).thenReturn(listOf(atendimento))
        whenever(historicoRepository.findProcedimentosByAtendimentoIds(listOf(10L))).thenReturn(emptyList())

        val result = service.getHistorico(pacienteId, "desc")

        assertNotNull(result)
        assertEquals(1, result.atendimentos.size)
        assertEquals(0, result.procedimentos.size)
    }

    @Test
    fun `should return historico with empty atendimentos when paciente has none`() {
        whenever(pacienteRepository.findById(pacienteId)).thenReturn(paciente)
        whenever(historicoRepository.findAtendimentosByPacienteId(pacienteId, "ASC")).thenReturn(emptyList())

        val result = service.getHistorico(pacienteId, "asc")

        assertNotNull(result)
        assertEquals(pacienteId, result.paciente.id)
        assertEquals(0, result.atendimentos.size)
        assertEquals(0, result.procedimentos.size)
    }

    @Test
    fun `should throw NotFoundException when paciente does not exist`() {
        whenever(pacienteRepository.findById(pacienteId)).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            service.getHistorico(pacienteId, "desc")
        }
    }
}
