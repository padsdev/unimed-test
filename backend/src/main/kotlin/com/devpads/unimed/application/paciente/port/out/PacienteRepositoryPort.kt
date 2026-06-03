package com.devpads.unimed.application.paciente.port.out

import com.devpads.unimed.domain.paciente.model.Paciente
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PacienteRepositoryPort {
    fun findById(id: Long): Paciente?
    fun findAll(pageable: Pageable): Page<Paciente>
}
