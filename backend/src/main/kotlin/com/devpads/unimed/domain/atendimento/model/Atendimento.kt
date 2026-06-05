package com.devpads.unimed.domain.atendimento.model

import java.time.Instant

data class Atendimento(
    val id: Long? = null,
    val pacienteId: Long,
    val dataAtendimento: Instant,
    val medico: String,
    val observacoes: String,
)