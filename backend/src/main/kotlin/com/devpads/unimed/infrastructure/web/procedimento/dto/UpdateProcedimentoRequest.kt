package com.devpads.unimed.infrastructure.web.procedimento.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class UpdateProcedimentoRequest(
    @field:NotNull(message = "Atendimento é obrigatório")
    @field:Positive(message = "Atendimento inválido")
    val atendimentoId: Long,

    @field:NotBlank(message = "Nome é obrigatório")
    val nome: String,

    @field:NotNull(message = "Valor é obrigatório")
    @field:Positive(message = "Valor deve ser maior que zero")
    val valor: BigDecimal,
)