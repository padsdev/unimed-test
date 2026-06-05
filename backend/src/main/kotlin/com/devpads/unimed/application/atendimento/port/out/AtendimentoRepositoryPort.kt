package com.devpads.unimed.application.atendimento.port.out

import com.devpads.unimed.application.shared.PagedResult
import com.devpads.unimed.domain.atendimento.model.Atendimento

interface AtendimentoRepositoryPort {
    fun findById(id: Long): Atendimento?
    fun findAll(page: Int, size: Int, sortField: String, sortDirection: String, pacienteId: Long? = null): PagedResult<Atendimento>
    fun save(atendimento: Atendimento): Atendimento
    fun deleteById(id: Long)
    fun existsById(id: Long): Boolean
}