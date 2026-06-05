package com.devpads.unimed.application.procedimento.service

import com.devpads.unimed.application.procedimento.port.out.ProcedimentoRepositoryPort
import com.devpads.unimed.application.shared.exception.ConflictException
import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.application.shared.exception.UnimedViolation
import com.devpads.unimed.application.shared.exception.UnprocessableEntityException
import com.devpads.unimed.domain.procedimento.model.Procedimento
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProcedimentoService(
    private val procedimentoRepository: ProcedimentoRepositoryPort,
) {
    fun findById(id: Long): Procedimento? = procedimentoRepository.findById(id)

    fun findAll(page: Int, size: Int, sortField: String, sortDirection: String, atendimentoId: Long? = null) =
        procedimentoRepository.findAll(page, size, sortField, sortDirection, atendimentoId)

    fun findByAtendimentoId(atendimentoId: Long): List<Procedimento> =
        procedimentoRepository.findByAtendimentoId(atendimentoId)

    fun create(command: CreateProcedimentoCommand): Procedimento {
        val violations = mutableListOf<UnimedViolation>()

        if (command.nome.isBlank()) {
            violations.add(UnimedViolation("nome", "Nome é obrigatório", "required"))
        }

        if (command.valor <= BigDecimal.ZERO) {
            violations.add(UnimedViolation("valor", "Valor deve ser maior que zero", "invalid"))
        }

        if (violations.isNotEmpty()) {
            throw UnprocessableEntityException("Dados inválidos", violations = violations)
        }

        val procedimento = Procedimento(
            atendimentoId = command.atendimentoId,
            nome = command.nome,
            valor = command.valor,
        )

        return procedimentoRepository.save(procedimento)
    }

    fun update(id: Long, command: UpdateProcedimentoCommand): Procedimento {
        val existing = procedimentoRepository.findById(id)
            ?: throw NotFoundException("Procedimento com id=$id não encontrado")

        val violations = mutableListOf<UnimedViolation>()

        if (command.nome.isBlank()) {
            violations.add(UnimedViolation("nome", "Nome é obrigatório", "required"))
        }

        if (command.valor <= BigDecimal.ZERO) {
            violations.add(UnimedViolation("valor", "Valor deve ser maior que zero", "invalid"))
        }

        if (violations.isNotEmpty()) {
            throw UnprocessableEntityException("Dados inválidos", violations = violations)
        }

        val procedimento = Procedimento(
            id = existing.id,
            atendimentoId = command.atendimentoId,
            nome = command.nome,
            valor = command.valor,
        )

        return procedimentoRepository.save(procedimento)
    }

    fun delete(id: Long) {
        val existing = procedimentoRepository.findById(id)
            ?: throw NotFoundException("Procedimento com id=$id não encontrado")

        procedimentoRepository.deleteById(id)
    }
}

data class CreateProcedimentoCommand(
    val atendimentoId: Long,
    val nome: String,
    val valor: BigDecimal,
)

data class UpdateProcedimentoCommand(
    val atendimentoId: Long,
    val nome: String,
    val valor: BigDecimal,
)