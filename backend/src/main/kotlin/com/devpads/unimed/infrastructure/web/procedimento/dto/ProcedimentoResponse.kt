package com.devpads.unimed.infrastructure.web.procedimento.dto

import com.devpads.unimed.domain.procedimento.model.Procedimento
import java.math.BigDecimal

data class ProcedimentoResponse(
    val id: Long,
    val atendimentoId: Long,
    val nome: String,
    val valor: BigDecimal,
)

fun Procedimento.toResponse(): ProcedimentoResponse = ProcedimentoResponse(
    id = requireNotNull(id),
    atendimentoId = atendimentoId,
    nome = nome,
    valor = valor,
)