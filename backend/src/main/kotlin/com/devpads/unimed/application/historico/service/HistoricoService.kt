package com.devpads.unimed.application.historico.service

import com.devpads.unimed.application.shared.exception.NotFoundException
import com.devpads.unimed.application.historico.port.out.HistoricoRepositoryPort
import com.devpads.unimed.application.paciente.port.out.PacienteRepositoryPort
import com.devpads.unimed.domain.atendimento.model.Atendimento
import com.devpads.unimed.domain.paciente.model.Paciente
import com.devpads.unimed.domain.procedimento.model.Procedimento
import org.springframework.stereotype.Service

data class HistoricoData(
    val paciente: Paciente,
    val atendimentos: List<Atendimento>,
    val procedimentos: List<Procedimento>,
)

@Service
class HistoricoService(
    private val pacienteRepository: PacienteRepositoryPort,
    private val historicoRepository: HistoricoRepositoryPort,
) {

    fun getHistorico(pacienteId: Long, sortOrder: String): HistoricoData {
        val paciente = pacienteRepository.findById(pacienteId)
            ?: throw NotFoundException("Paciente n\u00e3o encontrado")

        val safeDir = if (sortOrder.lowercase() == "asc") "ASC" else "DESC"
        val atendimentos = historicoRepository.findAtendimentosByPacienteId(pacienteId, safeDir)

        if (atendimentos.isEmpty()) {
            return HistoricoData(paciente, emptyList(), emptyList())
        }

        val atendimentoIds = atendimentos.mapNotNull { it.id }
        val procedimentos = historicoRepository.findProcedimentosByAtendimentoIds(atendimentoIds)

        return HistoricoData(paciente, atendimentos, procedimentos)
    }
}
