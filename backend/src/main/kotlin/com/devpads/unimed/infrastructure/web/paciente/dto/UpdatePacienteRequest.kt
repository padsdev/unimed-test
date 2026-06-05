package com.devpads.unimed.infrastructure.web.paciente.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past

data class UpdatePacienteRequest(
    @field:NotBlank(message = "Nome é obrigatório")
    val nome: String,

    @field:NotBlank(message = "Data de nascimento é obrigatória")
    @field:Past(message = "Data de nascimento não pode ser futura")
    val dataNascimento: String,

    @field:NotBlank(message = "Telefone é obrigatório")
    val telefone: String,

    @field:NotBlank(message = "Email é obrigatório")
    @field:Email(message = "Email inválido")
    val email: String,
)
