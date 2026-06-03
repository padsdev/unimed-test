package com.devpads.unimed.application.paciente.service

import com.devpads.unimed.application.paciente.port.out.PacienteRepositoryPort
import com.devpads.unimed.domain.paciente.model.Paciente
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class PacienteService(
    private val pacienteRepository: PacienteRepositoryPort,
) {
    fun findById(id: Long): Paciente? = pacienteRepository.findById(id)

    fun findAll(pageable: Pageable): Page<Paciente> = pacienteRepository.findAll(pageable)
}
