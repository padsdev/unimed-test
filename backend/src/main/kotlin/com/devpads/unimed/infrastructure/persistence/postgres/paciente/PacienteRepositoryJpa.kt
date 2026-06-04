package com.devpads.unimed.infrastructure.persistence.postgres.paciente

import com.devpads.unimed.application.paciente.port.out.PacienteRepositoryPort
import com.devpads.unimed.domain.paciente.model.Paciente
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class PacienteRepositoryJpa(
    private val jpaRepository: PacienteJpaRepository,
) : PacienteRepositoryPort {

    override fun findById(id: Long): Paciente? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findAll(pageable: Pageable): Page<Paciente> =
        jpaRepository.findAll(pageable).map { it.toDomain() }

    override fun save(paciente: Paciente): Paciente =
        jpaRepository.save(paciente.toEntity()).toDomain()

    override fun deleteById(id: Long) =
        jpaRepository.deleteById(id)

    override fun existsByCpf(cpf: String): Boolean =
        jpaRepository.existsByCpf(cpf)

    override fun existsByCpfAndIdNot(cpf: String, id: Long): Boolean =
        jpaRepository.existsByCpfAndIdNot(cpf, id)

    override fun existsByEmailIgnoreCase(email: String): Boolean =
        jpaRepository.existsByEmailIgnoreCase(email)

    override fun existsByEmailIgnoreCaseAndIdNot(email: String, id: Long): Boolean =
        jpaRepository.existsByEmailIgnoreCaseAndIdNot(email, id)
}

private fun PacienteEntity.toDomain() = Paciente(
    id = id,
    nome = nome,
    cpf = cpf,
    dataNascimento = dataNascimento,
    telefone = telefone,
    email = email,
)

private fun Paciente.toEntity() = PacienteEntity(
    id = id,
    nome = nome,
    cpf = cpf,
    dataNascimento = dataNascimento,
    telefone = telefone,
    email = email,
)
