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
}

private fun PacienteEntity.toDomain() = Paciente(
    id = id,
    nome = nome,
    cpf = cpf,
    dataNascimento = dataNascimento,
    telefone = telefone,
    email = email,
    status = status,
)
