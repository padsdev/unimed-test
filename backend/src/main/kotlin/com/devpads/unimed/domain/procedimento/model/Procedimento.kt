package com.devpads.unimed.domain.procedimento.model

import java.math.BigDecimal

data class Procedimento(
    val id: Long? = null,
    val atendimentoId: Long,
    val nome: String,
    val valor: BigDecimal,
)