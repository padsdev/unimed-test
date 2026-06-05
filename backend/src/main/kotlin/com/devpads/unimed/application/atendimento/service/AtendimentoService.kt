package com.devpads.unimed.application.atendimento.service

import com.devpads.unimed.application.atendimento.port.out.AtendimentoRepositoryPort
import com.devpads.unimed.application.paciente.port.out.PacienteRepositoryPort
import com.devpads.unimed.application.shared.exception.ConflictException
import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.application.shared.exception.UnimedViolation
import com.devpads.unimed.application.shared.exception.UnprocessableEntityException
import com.devpads.unimed.domain.atendimento.model.Atendimento
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AtendimentoService(
    private val atendimentoRepository: AtendimentoRepositoryPort,
    private val pacienteRepository: PacienteRepositoryPort,
) {
    fun findById(id: Long): Atendimento? = atendimentoRepository.findById(id)

    fun findAll(page: Int, size: Int, sortField: String, sortDirection: String, pacienteId: Long? = null) =
        atendimentoRepository.findAll(page, size, sortField, sortDirection, pacienteId)

    fun create(command: CreateAtendimentoCommand): Atendimento {
        val violations = mutableListOf<UnimedViolation>()

        if (command.medico.isBlank()) {
            violations.add(UnimedViolation("medico", "Médico é obrigatório", "required"))
        }

        if (violations.isNotEmpty()) {
            throw UnprocessableEntityException("Dados inválidos", violations = violations)
        }

        if (pacienteRepository.findById(command.pacienteId) == null) {
            throw ConflictException(
                "Paciente não encontrado",
                violations = listOf(UnimedViolation("pacienteId", "Paciente não encontrado", "not_found")),
            )
        }

        val atendimento = Atendimento(
            pacienteId = command.pacienteId,
            dataAtendimento = command.dataAtendimento,
            medico = command.medico,
            observacoes = command.observacoes,
        )

        return atendimentoRepository.save(atendimento)
    }

    fun update(id: Long, command: UpdateAtendimentoCommand): Atendimento {
        val existing = atendimentoRepository.findById(id)
            ?: throw NotFoundException("Atendimento com id=$id não encontrado")

        val violations = mutableListOf<UnimedViolation>()

        if (command.medico.isBlank()) {
            violations.add(UnimedViolation("medico", "Médico é obrigatório", "required"))
        }

        if (violations.isNotEmpty()) {
            throw UnprocessableEntityException("Dados inválidos", violations = violations)
        }

        if (pacienteRepository.findById(command.pacienteId) == null) {
            throw ConflictException(
                "Paciente não encontrado",
                violations = listOf(UnimedViolation("pacienteId", "Paciente não encontrado", "not_found")),
            )
        }

        val atendimento = Atendimento(
            id = existing.id,
            pacienteId = command.pacienteId,
            dataAtendimento = command.dataAtendimento,
            medico = command.medico,
            observacoes = command.observacoes,
        )

        return atendimentoRepository.save(atendimento)
    }

    fun delete(id: Long) {
        val existing = atendimentoRepository.findById(id)
            ?: throw NotFoundException("Atendimento com id=$id não encontrado")

        atendimentoRepository.deleteById(id)
    }
}

data class CreateAtendimentoCommand(
    val pacienteId: Long,
    val dataAtendimento: Instant,
    val medico: String,
    val observacoes: String,
)

data class UpdateAtendimentoCommand(
    val pacienteId: Long,
    val dataAtendimento: Instant,
    val medico: String,
    val observacoes: String,
)