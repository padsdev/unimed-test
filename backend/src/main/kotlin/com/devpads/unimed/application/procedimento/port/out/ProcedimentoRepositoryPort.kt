package com.devpads.unimed.application.procedimento.port.out

import com.devpads.unimed.application.shared.PagedResult
import com.devpads.unimed.domain.procedimento.model.Procedimento

interface ProcedimentoRepositoryPort {
    fun findById(id: Long): Procedimento?
    fun findAll(page: Int, size: Int, sortField: String, sortDirection: String, atendimentoId: Long? = null): PagedResult<Procedimento>
    fun save(procedimento: Procedimento): Procedimento
    fun deleteById(id: Long)
    fun existsById(id: Long): Boolean
    fun findByAtendimentoId(atendimentoId: Long): List<Procedimento>
}