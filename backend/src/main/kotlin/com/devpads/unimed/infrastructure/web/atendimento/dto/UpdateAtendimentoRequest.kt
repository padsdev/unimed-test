package com.devpads.unimed.infrastructure.web.atendimento.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class UpdateAtendimentoRequest(
    @field:NotNull(message = "Paciente é obrigatório")
    @field:Positive(message = "Paciente inválido")
    val pacienteId: Long,

    @field:NotBlank(message = "Data do atendimento é obrigatória")
    val dataAtendimento: String,

    @field:NotBlank(message = "Médico é obrigatório")
    val medico: String,

    val observacoes: String = "",
)