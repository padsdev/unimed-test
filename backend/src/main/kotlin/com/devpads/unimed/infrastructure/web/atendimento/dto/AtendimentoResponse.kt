package com.devpads.unimed.infrastructure.web.atendimento.dto

import com.devpads.unimed.domain.atendimento.model.Atendimento
import java.time.Instant

data class AtendimentoResponse(
    val id: Long,
    val pacienteId: Long,
    val dataAtendimento: Instant,
    val medico: String,
    val observacoes: String,
)

fun Atendimento.toResponse(): AtendimentoResponse = AtendimentoResponse(
    id = requireNotNull(id),
    pacienteId = pacienteId,
    dataAtendimento = dataAtendimento,
    medico = medico,
    observacoes = observacoes,
)