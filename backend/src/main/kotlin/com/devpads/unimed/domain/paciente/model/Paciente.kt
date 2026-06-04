package com.devpads.unimed.domain.paciente.model

import java.time.LocalDate

data class Paciente(
    val id: Long? = null,
    val nome: String,
    val cpf: String,
    val dataNascimento: LocalDate,
    val telefone: String,
    val email: String,
)
