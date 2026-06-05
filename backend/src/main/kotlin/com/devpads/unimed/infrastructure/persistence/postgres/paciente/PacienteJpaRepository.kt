package com.devpads.unimed.infrastructure.persistence.postgres.paciente

import org.springframework.data.jpa.repository.JpaRepository

interface PacienteJpaRepository : JpaRepository<PacienteEntity, Long> {
    fun existsByCpf(cpf: String): Boolean
    fun existsByCpfAndIdNot(cpf: String, id: Long): Boolean
    fun existsByEmailIgnoreCase(email: String): Boolean
    fun existsByEmailIgnoreCaseAndIdNot(email: String, id: Long): Boolean
}
