package com.devpads.unimed.atendimento.service

import com.devpads.unimed.application.atendimento.port.out.AtendimentoRepositoryPort
import com.devpads.unimed.application.atendimento.service.AtendimentoService
import com.devpads.unimed.application.atendimento.service.CreateAtendimentoCommand
import com.devpads.unimed.application.atendimento.service.UpdateAtendimentoCommand
import com.devpads.unimed.application.paciente.port.out.PacienteRepositoryPort
import com.devpads.unimed.application.shared.exception.ConflictException
import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.application.shared.exception.UnprocessableEntityException
import com.devpads.unimed.domain.atendimento.model.Atendimento
import com.devpads.unimed.domain.paciente.model.Paciente
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class AtendimentoServiceTest {

    @Mock
    private lateinit var atendimentoRepository: AtendimentoRepositoryPort

    @Mock
    private lateinit var pacienteRepository: PacienteRepositoryPort

    private lateinit var atendimentoService: AtendimentoService

    @BeforeEach
    fun setup() {
        atendimentoService = AtendimentoService(atendimentoRepository, pacienteRepository)
    }

    @Test
    fun create_shouldSaveAtendimento_whenValid() {
        val paciente = Paciente(
            id = 1L,
            nome = "João Silva",
            cpf = "12345678901",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        whenever(pacienteRepository.findById(1L)).thenReturn(paciente)
        whenever(atendimentoRepository.save(any())).thenAnswer { invocation ->
            val a = invocation.arguments[0] as Atendimento
            a.copy(id = 1L)
        }

        val command = CreateAtendimentoCommand(
            pacienteId = 1L,
            dataAtendimento = Instant.parse("2024-05-15T14:30:00Z"),
            medico = "Dr. Carlos",
            observacoes = "Paciente com sintomas de gripe",
        )

        val result = atendimentoService.create(command)

        assertEquals(1L, result.id)
        assertEquals(1L, result.pacienteId)
        assertEquals("Dr. Carlos", result.medico)
    }

    @Test
    fun create_shouldThrowUnprocessable_whenMedicoBlank() {
        val command = CreateAtendimentoCommand(
            pacienteId = 1L,
            dataAtendimento = Instant.parse("2024-05-15T14:30:00Z"),
            medico = "",
            observacoes = "",
        )

        assertThrows(UnprocessableEntityException::class.java) {
            atendimentoService.create(command)
        }
    }

    @Test
    fun create_shouldThrowConflict_whenPacienteNotFound() {
        whenever(pacienteRepository.findById(999L)).thenReturn(null)

        val command = CreateAtendimentoCommand(
            pacienteId = 999L,
            dataAtendimento = Instant.parse("2024-05-15T14:30:00Z"),
            medico = "Dr. Carlos",
            observacoes = "",
        )

        assertThrows(ConflictException::class.java) {
            atendimentoService.create(command)
        }
    }

    @Test
    fun update_shouldUpdateAtendimento_whenValid() {
        val existing = Atendimento(
            id = 1L,
            pacienteId = 1L,
            dataAtendimento = Instant.parse("2024-05-15T14:30:00Z"),
            medico = "Dr. Carlos",
            observacoes = "Paciente com sintomas de gripe",
        )

        val paciente = Paciente(
            id = 2L,
            nome = "Maria Santos",
            cpf = "00000000002",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "maria@email.com",
        )

        whenever(atendimentoRepository.findById(1L)).thenReturn(existing)
        whenever(pacienteRepository.findById(2L)).thenReturn(paciente)
        whenever(atendimentoRepository.save(any())).thenAnswer { invocation ->
            invocation.arguments[0] as Atendimento
        }

        val command = UpdateAtendimentoCommand(
            pacienteId = 2L,
            dataAtendimento = Instant.parse("2024-05-16T10:00:00Z"),
            medico = "Dra. Ana",
            observacoes = "Retorno",
        )

        val result = atendimentoService.update(1L, command)

        assertEquals(1L, result.id)
        assertEquals(2L, result.pacienteId)
        assertEquals("Dra. Ana", result.medico)
    }

    @Test
    fun update_shouldThrowNotFound_whenAtendimentoNotExists() {
        whenever(atendimentoRepository.findById(999L)).thenReturn(null)

        val command = UpdateAtendimentoCommand(
            pacienteId = 1L,
            dataAtendimento = Instant.parse("2024-05-15T14:30:00Z"),
            medico = "Dr. Carlos",
            observacoes = "",
        )

        assertThrows(NotFoundException::class.java) {
            atendimentoService.update(999L, command)
        }
    }

    @Test
    fun update_shouldThrowUnprocessable_whenMedicoBlank() {
        val existing = Atendimento(
            id = 1L,
            pacienteId = 1L,
            dataAtendimento = Instant.parse("2024-05-15T14:30:00Z"),
            medico = "Dr. Carlos",
            observacoes = "",
        )

        whenever(atendimentoRepository.findById(1L)).thenReturn(existing)

        val command = UpdateAtendimentoCommand(
            pacienteId = 1L,
            dataAtendimento = Instant.parse("2024-05-15T14:30:00Z"),
            medico = "",
            observacoes = "",
        )

        assertThrows(UnprocessableEntityException::class.java) {
            atendimentoService.update(1L, command)
        }
    }

    @Test
    fun delete_shouldDeleteAtendimento_whenExists() {
        val existing = Atendimento(
            id = 1L,
            pacienteId = 1L,
            dataAtendimento = Instant.parse("2024-05-15T14:30:00Z"),
            medico = "Dr. Carlos",
            observacoes = "",
        )

        whenever(atendimentoRepository.findById(1L)).thenReturn(existing)

        atendimentoService.delete(1L)
    }

    @Test
    fun delete_shouldThrowNotFound_whenAtendimentoNotExists() {
        whenever(atendimentoRepository.findById(999L)).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            atendimentoService.delete(999L)
        }
    }

    @Test
    fun findById_shouldReturnAtendimento_whenExists() {
        val atendimento = Atendimento(
            id = 1L,
            pacienteId = 1L,
            dataAtendimento = Instant.parse("2024-05-15T14:30:00Z"),
            medico = "Dr. Carlos",
            observacoes = "",
        )

        whenever(atendimentoRepository.findById(1L)).thenReturn(atendimento)

        val result = atendimentoService.findById(1L)

        assertEquals(1L, result?.id)
        assertEquals("Dr. Carlos", result?.medico)
    }

    @Test
    fun findById_shouldReturnNull_whenNotExists() {
        whenever(atendimentoRepository.findById(999L)).thenReturn(null)

        val result = atendimentoService.findById(999L)

        assertEquals(null, result)
    }
}