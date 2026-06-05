package com.devpads.unimed.infrastructure.web.historico.dto

import com.devpads.unimed.domain.atendimento.model.Atendimento
import com.devpads.unimed.infrastructure.web.atendimento.dto.AtendimentoResponse
import com.devpads.unimed.infrastructure.web.atendimento.dto.toResponse
import com.devpads.unimed.infrastructure.web.paciente.dto.PacienteResponse
import com.devpads.unimed.infrastructure.web.procedimento.dto.ProcedimentoResponse
import java.time.Instant

data class HistoricoPacienteResponse(
    val paciente: PacienteResponse,
    val atendimentos: List<AtendimentoComProcedimentosResponse>,
)

data class AtendimentoComProcedimentosResponse(
    val id: Long,
    val pacienteId: Long,
    val dataAtendimento: Instant,
    val medico: String,
    val observacoes: String,
    val procedimentos: List<ProcedimentoResponse>,
)

fun Atendimento.toComProcedimentosResponse(procedimentos: List<ProcedimentoResponse>): AtendimentoComProcedimentosResponse =
    AtendimentoComProcedimentosResponse(
        id = requireNotNull(id),
        pacienteId = pacienteId,
        dataAtendimento = dataAtendimento,
        medico = medico,
        observacoes = observacoes,
        procedimentos = procedimentos,
    )
