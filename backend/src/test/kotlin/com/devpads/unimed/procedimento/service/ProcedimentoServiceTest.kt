package com.devpads.unimed.procedimento.service

import com.devpads.unimed.application.procedimento.port.out.ProcedimentoRepositoryPort
import com.devpads.unimed.application.procedimento.service.CreateProcedimentoCommand
import com.devpads.unimed.application.procedimento.service.ProcedimentoService
import com.devpads.unimed.application.procedimento.service.UpdateProcedimentoCommand
import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.application.shared.exception.UnprocessableEntityException
import com.devpads.unimed.domain.procedimento.model.Procedimento
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class ProcedimentoServiceTest {

    @Mock
    private lateinit var procedimentoRepository: ProcedimentoRepositoryPort

    private lateinit var procedimentoService: ProcedimentoService

    @BeforeEach
    fun setup() {
        procedimentoService = ProcedimentoService(procedimentoRepository)
    }

    @Test
    fun create_shouldSaveProcedimento_whenValid() {
        whenever(procedimentoRepository.save(any())).thenAnswer { invocation ->
            val p = invocation.arguments[0] as Procedimento
            p.copy(id = 1L)
        }

        val command = CreateProcedimentoCommand(
            atendimentoId = 1L,
            nome = "Eletrocardiograma",
            valor = BigDecimal("150.00"),
        )

        val result = procedimentoService.create(command)

        assertEquals(1L, result.id)
        assertEquals("Eletrocardiograma", result.nome)
        assertEquals(BigDecimal("150.00"), result.valor)
    }

    @Test
    fun create_shouldThrowUnprocessable_whenNomeBlank() {
        val command = CreateProcedimentoCommand(
            atendimentoId = 1L,
            nome = "",
            valor = BigDecimal("150.00"),
        )

        assertThrows(UnprocessableEntityException::class.java) {
            procedimentoService.create(command)
        }
    }

    @Test
    fun create_shouldThrowUnprocessable_whenValorZero() {
        val command = CreateProcedimentoCommand(
            atendimentoId = 1L,
            nome = "Exame",
            valor = BigDecimal.ZERO,
        )

        assertThrows(UnprocessableEntityException::class.java) {
            procedimentoService.create(command)
        }
    }

    @Test
    fun create_shouldThrowUnprocessable_whenValorNegative() {
        val command = CreateProcedimentoCommand(
            atendimentoId = 1L,
            nome = "Exame",
            valor = BigDecimal("-10.00"),
        )

        assertThrows(UnprocessableEntityException::class.java) {
            procedimentoService.create(command)
        }
    }

    @Test
    fun update_shouldUpdateProcedimento_whenValid() {
        val existing = Procedimento(
            id = 1L,
            atendimentoId = 1L,
            nome = "Eletrocardiograma",
            valor = BigDecimal("150.00"),
        )

        whenever(procedimentoRepository.findById(1L)).thenReturn(existing)
        whenever(procedimentoRepository.save(any())).thenAnswer { invocation ->
            invocation.arguments[0] as Procedimento
        }

        val command = UpdateProcedimentoCommand(
            atendimentoId = 2L,
            nome = "Raio-X Torácico",
            valor = BigDecimal("250.00"),
        )

        val result = procedimentoService.update(1L, command)

        assertEquals("Raio-X Torácico", result.nome)
        assertEquals(BigDecimal("250.00"), result.valor)
    }

    @Test
    fun update_shouldThrowNotFound_whenNotExists() {
        whenever(procedimentoRepository.findById(999L)).thenReturn(null)

        val command = UpdateProcedimentoCommand(
            atendimentoId = 1L,
            nome = "Exame",
            valor = BigDecimal("100.00"),
        )

        assertThrows(NotFoundException::class.java) {
            procedimentoService.update(999L, command)
        }
    }

    @Test
    fun update_shouldThrowUnprocessable_whenNomeBlank() {
        val existing = Procedimento(
            id = 1L,
            atendimentoId = 1L,
            nome = "Eletrocardiograma",
            valor = BigDecimal("150.00"),
        )

        whenever(procedimentoRepository.findById(1L)).thenReturn(existing)

        val command = UpdateProcedimentoCommand(
            atendimentoId = 1L,
            nome = "",
            valor = BigDecimal("150.00"),
        )

        assertThrows(UnprocessableEntityException::class.java) {
            procedimentoService.update(1L, command)
        }
    }

    @Test
    fun update_shouldThrowUnprocessable_whenValorZero() {
        val existing = Procedimento(
            id = 1L,
            atendimentoId = 1L,
            nome = "Eletrocardiograma",
            valor = BigDecimal("150.00"),
        )

        whenever(procedimentoRepository.findById(1L)).thenReturn(existing)

        val command = UpdateProcedimentoCommand(
            atendimentoId = 1L,
            nome = "Exame",
            valor = BigDecimal.ZERO,
        )

        assertThrows(UnprocessableEntityException::class.java) {
            procedimentoService.update(1L, command)
        }
    }

    @Test
    fun delete_shouldDeleteProcedimento_whenExists() {
        val existing = Procedimento(
            id = 1L,
            atendimentoId = 1L,
            nome = "Eletrocardiograma",
            valor = BigDecimal("150.00"),
        )

        whenever(procedimentoRepository.findById(1L)).thenReturn(existing)

        procedimentoService.delete(1L)
    }

    @Test
    fun delete_shouldThrowNotFound_whenNotExists() {
        whenever(procedimentoRepository.findById(999L)).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            procedimentoService.delete(999L)
        }
    }

    @Test
    fun findById_shouldReturnProcedimento_whenExists() {
        val procedimento = Procedimento(
            id = 1L,
            atendimentoId = 1L,
            nome = "Eletrocardiograma",
            valor = BigDecimal("150.00"),
        )

        whenever(procedimentoRepository.findById(1L)).thenReturn(procedimento)

        val result = procedimentoService.findById(1L)

        assertEquals("Eletrocardiograma", result?.nome)
    }

    @Test
    fun findByAtendimentoId_shouldReturnList() {
        val procedimentos = listOf(
            Procedimento(id = 1L, atendimentoId = 1L, nome = "Proc A", valor = BigDecimal("100.00")),
            Procedimento(id = 2L, atendimentoId = 1L, nome = "Proc B", valor = BigDecimal("200.00")),
        )

        whenever(procedimentoRepository.findByAtendimentoId(1L)).thenReturn(procedimentos)

        val result = procedimentoService.findByAtendimentoId(1L)

        assertEquals(2, result.size)
    }
}