package com.devpads.unimed.infrastructure.web.paciente.dto

import com.devpads.unimed.domain.paciente.model.Paciente
import java.time.LocalDate

data class PacienteResponse(
    val id: Long,
    val nome: String,
    val cpf: String,
    val dataNascimento: LocalDate,
    val telefone: String,
    val email: String,
)

fun Paciente.toResponse(): PacienteResponse = PacienteResponse(
    id = requireNotNull(id),
    nome = nome,
    cpf = cpf,
    dataNascimento = dataNascimento,
    telefone = telefone,
    email = email,
)
