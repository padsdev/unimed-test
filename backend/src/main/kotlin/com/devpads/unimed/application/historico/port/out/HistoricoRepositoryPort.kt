package com.devpads.unimed.application.historico.port.out

import com.devpads.unimed.domain.atendimento.model.Atendimento
import com.devpads.unimed.domain.procedimento.model.Procedimento

interface HistoricoRepositoryPort {
    fun findAtendimentosByPacienteId(pacienteId: Long, sortDirection: String): List<Atendimento>
    fun findProcedimentosByAtendimentoIds(atendimentoIds: List<Long>): List<Procedimento>
}
