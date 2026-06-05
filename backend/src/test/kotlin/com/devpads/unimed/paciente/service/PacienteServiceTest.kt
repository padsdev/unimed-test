package com.devpads.unimed.paciente.service

import com.devpads.unimed.application.atendimento.port.out.AtendimentoVinculoPort
import com.devpads.unimed.application.paciente.port.out.PacienteRepositoryPort
import com.devpads.unimed.application.paciente.service.CreatePacienteCommand
import com.devpads.unimed.application.paciente.service.PacienteService
import com.devpads.unimed.application.paciente.service.UpdatePacienteCommand
import com.devpads.unimed.application.shared.exception.ConflictException
import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.application.shared.exception.UnprocessableEntityException
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
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PacienteServiceTest {

    @Mock
    private lateinit var pacienteRepository: PacienteRepositoryPort

    @Mock
    private lateinit var atendimentoVinculoPort: AtendimentoVinculoPort

    private lateinit var pacienteService: PacienteService

    @BeforeEach
    fun setup() {
        pacienteService = PacienteService(pacienteRepository, atendimentoVinculoPort)
    }

    @Test
    fun create_shouldSavePaciente_whenValid() {
        val command = CreatePacienteCommand(
            nome = "João Silva",
            cpf = "12345678901",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        whenever(pacienteRepository.existsByCpf("12345678901")).thenReturn(false)
        whenever(pacienteRepository.existsByEmailIgnoreCase("joao@email.com")).thenReturn(false)
        whenever(pacienteRepository.save(any())).thenAnswer { invocation ->
            invocation.arguments[0] as Paciente
        }

        val result = pacienteService.create(command)

        assertEquals("João Silva", result.nome)
        assertEquals("12345678901", result.cpf)
    }

    @Test
    fun create_shouldThrowConflict_whenCpfExists() {
        val command = CreatePacienteCommand(
            nome = "João Silva",
            cpf = "12345678901",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        whenever(pacienteRepository.existsByCpf("12345678901")).thenReturn(true)

        assertThrows(ConflictException::class.java) {
            pacienteService.create(command)
        }
    }

    @Test
    fun create_shouldThrowConflict_whenEmailExists() {
        val command = CreatePacienteCommand(
            nome = "João Silva",
            cpf = "12345678901",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        whenever(pacienteRepository.existsByCpf("12345678901")).thenReturn(false)
        whenever(pacienteRepository.existsByEmailIgnoreCase("joao@email.com")).thenReturn(true)

        assertThrows(ConflictException::class.java) {
            pacienteService.create(command)
        }
    }

    @Test
    fun create_shouldThrowUnprocessable_whenNomeBlank() {
        val command = CreatePacienteCommand(
            nome = "",
            cpf = "12345678901",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        assertThrows(UnprocessableEntityException::class.java) {
            pacienteService.create(command)
        }
    }

    @Test
    fun create_shouldThrowUnprocessable_whenCpfInvalid() {
        val command = CreatePacienteCommand(
            nome = "João Silva",
            cpf = "123",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        assertThrows(UnprocessableEntityException::class.java) {
            pacienteService.create(command)
        }
    }

    @Test
    fun create_shouldThrowUnprocessable_whenDataNascimentoFuture() {
        val command = CreatePacienteCommand(
            nome = "João Silva",
            cpf = "12345678901",
            dataNascimento = LocalDate.now().plusDays(1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        assertThrows(UnprocessableEntityException::class.java) {
            pacienteService.create(command)
        }
    }

    @Test
    fun update_shouldUpdatePaciente_whenValid() {
        val existingPaciente = Paciente(
            id = 1L,
            nome = "João Silva",
            cpf = "12345678901",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        val command = UpdatePacienteCommand(
            nome = "João Atualizado",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11988888888",
            email = "joao.novo@email.com",
        )

        whenever(pacienteRepository.findById(1L)).thenReturn(existingPaciente)
        whenever(pacienteRepository.existsByEmailIgnoreCaseAndIdNot("joao.novo@email.com", 1L)).thenReturn(false)
        whenever(pacienteRepository.save(any<Paciente>())).thenAnswer { invocation ->
            invocation.arguments[0] as Paciente
        }

        val result = pacienteService.update(1L, command)

        assertEquals("João Atualizado", result.nome)
        assertEquals("12345678901", result.cpf)
    }

    @Test
    fun update_shouldThrowNotFound_whenPacienteNotExists() {
        val command = UpdatePacienteCommand(
            nome = "João Silva",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        whenever(pacienteRepository.findById(999L)).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            pacienteService.update(999L, command)
        }
    }

    @Test
    fun update_shouldThrowConflict_whenEmailExistsForOther() {
        val existingPaciente = Paciente(
            id = 1L,
            nome = "João Silva",
            cpf = "12345678901",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        val command = UpdatePacienteCommand(
            nome = "João Silva",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "outro@email.com",
        )

        whenever(pacienteRepository.findById(1L)).thenReturn(existingPaciente)
        whenever(pacienteRepository.existsByEmailIgnoreCaseAndIdNot("outro@email.com", 1L)).thenReturn(true)

        assertThrows(ConflictException::class.java) {
            pacienteService.update(1L, command)
        }
    }

    @Test
    fun update_shouldThrowUnprocessable_whenNomeBlank() {
        val existingPaciente = Paciente(
            id = 1L,
            nome = "João Silva",
            cpf = "12345678901",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        val command = UpdatePacienteCommand(
            nome = "",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        whenever(pacienteRepository.findById(1L)).thenReturn(existingPaciente)

        assertThrows(UnprocessableEntityException::class.java) {
            pacienteService.update(1L, command)
        }
    }

    @Test
    fun delete_shouldDeletePaciente_whenNoVinculos() {
        val existingPaciente = Paciente(
            id = 1L,
            nome = "João Silva",
            cpf = "12345678901",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        whenever(pacienteRepository.findById(1L)).thenReturn(existingPaciente)
        whenever(atendimentoVinculoPort.existsByPacienteId(1L)).thenReturn(false)

        pacienteService.delete(1L)
    }

    @Test
    fun delete_shouldThrowNotFound_whenPacienteNotExists() {
        whenever(pacienteRepository.findById(999L)).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            pacienteService.delete(999L)
        }
    }

    @Test
    fun delete_shouldThrowConflict_whenVinculosExist() {
        val existingPaciente = Paciente(
            id = 1L,
            nome = "João Silva",
            cpf = "12345678901",
            dataNascimento = LocalDate.of(1990, 1, 1),
            telefone = "11999999999",
            email = "joao@email.com",
        )

        whenever(pacienteRepository.findById(1L)).thenReturn(existingPaciente)
        whenever(atendimentoVinculoPort.existsByPacienteId(1L)).thenReturn(true)

        assertThrows(ConflictException::class.java) {
            pacienteService.delete(1L)
        }
    }
}
