package com.devpads.unimed.application.paciente.port.out

import com.devpads.unimed.domain.paciente.model.Paciente
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PacienteRepositoryPort {
    fun findById(id: Long): Paciente?
    fun findAll(pageable: Pageable): Page<Paciente>
    fun save(paciente: Paciente): Paciente
    fun deleteById(id: Long)
    fun existsByCpf(cpf: String): Boolean
    fun existsByCpfAndIdNot(cpf: String, id: Long): Boolean
    fun existsByEmailIgnoreCase(email: String): Boolean
    fun existsByEmailIgnoreCaseAndIdNot(email: String, id: Long): Boolean
}
