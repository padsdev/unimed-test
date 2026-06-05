package com.devpads.unimed.application.paciente.service

import com.devpads.unimed.application.atendimento.port.out.AtendimentoVinculoPort
import com.devpads.unimed.application.paciente.port.out.PacienteRepositoryPort
import com.devpads.unimed.application.shared.exception.ConflictException
import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.application.shared.exception.UnimedViolation
import com.devpads.unimed.application.shared.exception.UnprocessableEntityException
import com.devpads.unimed.domain.paciente.model.Paciente
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PacienteService(
    private val pacienteRepository: PacienteRepositoryPort,
    private val atendimentoVinculoPort: AtendimentoVinculoPort,
) {
    fun findById(id: Long): Paciente? = pacienteRepository.findById(id)

    fun findAll(pageable: Pageable): Page<Paciente> = pacienteRepository.findAll(pageable)

    fun create(command: CreatePacienteCommand): Paciente {
        val violations = mutableListOf<UnimedViolation>()

        if (command.nome.isBlank()) {
            violations.add(UnimedViolation("nome", "Nome é obrigatório", "required"))
        }

        if (!command.cpf.matches(Regex("^\\d{11}$"))) {
            violations.add(UnimedViolation("cpf", "CPF deve conter exatamente 11 dígitos", "invalid"))
        }

        if (command.dataNascimento.isAfter(LocalDate.now())) {
            violations.add(UnimedViolation("dataNascimento", "Data de nascimento não pode ser futura", "future_date"))
        }

        if (command.telefone.isBlank()) {
            violations.add(UnimedViolation("telefone", "Telefone é obrigatório", "required"))
        }

        if (command.email.isBlank()) {
            violations.add(UnimedViolation("email", "Email é obrigatório", "required"))
        }

        if (violations.isNotEmpty()) {
            throw UnprocessableEntityException("Dados inválidos", violations = violations)
        }

        if (pacienteRepository.existsByCpf(command.cpf)) {
            throw ConflictException(
                "CPF já cadastrado",
                violations = listOf(UnimedViolation("cpf", "CPF já cadastrado", "conflict")),
            )
        }

        if (pacienteRepository.existsByEmailIgnoreCase(command.email)) {
            throw ConflictException(
                "Email já cadastrado",
                violations = listOf(UnimedViolation("email", "Email já cadastrado", "conflict")),
            )
        }

        val paciente = Paciente(
            nome = command.nome,
            cpf = command.cpf,
            dataNascimento = command.dataNascimento,
            telefone = command.telefone,
            email = command.email,
        )

        return pacienteRepository.save(paciente)
    }

    fun update(id: Long, command: UpdatePacienteCommand): Paciente {
        val existing = pacienteRepository.findById(id)
            ?: throw NotFoundException("Paciente com id=$id não encontrado")

        val violations = mutableListOf<UnimedViolation>()

        if (command.nome.isBlank()) {
            violations.add(UnimedViolation("nome", "Nome é obrigatório", "required"))
        }

        if (command.dataNascimento.isAfter(LocalDate.now())) {
            violations.add(UnimedViolation("dataNascimento", "Data de nascimento não pode ser futura", "future_date"))
        }

        if (command.telefone.isBlank()) {
            violations.add(UnimedViolation("telefone", "Telefone é obrigatório", "required"))
        }

        if (command.email.isBlank()) {
            violations.add(UnimedViolation("email", "Email é obrigatório", "required"))
        }

        if (violations.isNotEmpty()) {
            throw UnprocessableEntityException("Dados inválidos", violations = violations)
        }

        if (pacienteRepository.existsByEmailIgnoreCaseAndIdNot(command.email, id)) {
            throw ConflictException(
                "Email já cadastrado",
                violations = listOf(UnimedViolation("email", "Email já cadastrado", "conflict")),
            )
        }

        val paciente = Paciente(
            id = existing.id,
            nome = command.nome,
            cpf = existing.cpf,
            dataNascimento = command.dataNascimento,
            telefone = command.telefone,
            email = command.email,
        )

        return pacienteRepository.save(paciente)
    }

    fun delete(id: Long) {
        val existing = pacienteRepository.findById(id)
            ?: throw NotFoundException("Paciente com id=$id não encontrado")

        if (atendimentoVinculoPort.existsByPacienteId(id)) {
            throw ConflictException(
                "Paciente possui atendimentos vinculados",
                violations = listOf(UnimedViolation("id", "Paciente possui atendimentos vinculados", "conflict")),
            )
        }

        pacienteRepository.deleteById(id)
    }
}

data class CreatePacienteCommand(
    val nome: String,
    val cpf: String,
    val dataNascimento: LocalDate,
    val telefone: String,
    val email: String,
)

data class UpdatePacienteCommand(
    val nome: String,
    val dataNascimento: LocalDate,
    val telefone: String,
    val email: String,
)
